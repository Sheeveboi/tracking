package net.altosheeve.tracking.client.Core;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.altosheeve.tracking.client.Waypoints.Waypoint;
import net.altosheeve.tracking.client.Shapes.Layer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import java.util.ArrayList;

public class Rendering {

    public static MinecraftClient client = MinecraftClient.getInstance();
    public static WorldRenderContext renderContext;
    public static Matrix4fStack modelViewStack;
    public static int renderTick = 0;
    public static int maxRenderTick = 100000;

    public static final RenderPipeline fillUnnocluded = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of("tracking", "pipeline/positive"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build()
    );

    public static final RenderPipeline fillOccluded = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of("tracking", "pipeline/positive"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST)
            .withCull(false)
            .build()
    );

    public static final RenderPipeline lineUnoccluded = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of("tracking", "pipeline/line"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build()
    );

    public static final RenderPipeline lineOccluded = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of("tracking", "pipeline/line"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
            .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
            .withCull(false)
            .build()
    );

    private static final BufferAllocator allocator = new BufferAllocator(RenderLayer.CUTOUT_BUFFER_SIZE);
    private static MappableRingBuffer vertexBuffer;

    private static GpuBuffer upload3d(BuiltBuffer.DrawParameters drawParameters, VertexFormat format, BuiltBuffer builtBuffer) {
        // Calculate the size needed for the vertex buffer
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        // Initialize or resize the vertex buffer as needed
        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }

            vertexBuffer = new MappableRingBuffer(() -> "tracking unoccluded pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        // Copy vertex data into the vertex buffer
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.getBlocking().slice(0, builtBuffer.getBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.getBuffer(), mappedView.data());
        }

        return vertexBuffer.getBlocking();
    }

    public static void draw3d(MinecraftClient client, RenderPipeline pipeline, BuiltBuffer builtBuffer, BuiltBuffer.DrawParameters drawParameters, GpuBuffer vertices, VertexFormat format) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.DrawMode.QUADS) {
            // Sort the quads if there is translucency
            builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().getVertexSorter());
            // Upload the index buffer
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.getSortedBuffer());
            indexType = builtBuffer.getDrawParameters().indexType();
        } else {
            // Use the general shape index buffer for non-quad draw modes
            RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeIndexBuffer.getIndexBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.getIndexType();
        }

        // Actually execute the draw
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .write(RenderSystem.getModelViewMatrix(), new Vector4f(1f, 1f, 1f, 1f), RenderSystem.getModelOffset(), RenderSystem.getTextureMatrix(), 1f);
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "tracking unoccluded pipeline", client.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), client.getFramebuffer().getDepthAttachmentView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            // Bind texture if applicable:
            // Sampler0 is used for texture inputs in vertices
            // renderPass.bindSampler("Sampler0", textureView);

            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);

            // The base vertex is the starting index when we copied the data into the vertex buffer divided by vertex size
            //noinspection ConstantValue
            renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    public static void renderWaypoints(@SuppressWarnings("SameParameterValue") RenderPipeline pipeline) {

        if (!Waypoint.waypoints.isEmpty()) {

            BufferBuilder waypointBuffer = new BufferBuilder(allocator, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
            VertexConsumerProvider.Immediate textBuffer = client.getBufferBuilders().getEntityVertexConsumers();

            for (Waypoint waypoint : new ArrayList<>(Waypoint.waypoints)) {
                waypoint.drawPoint(waypointBuffer);
                if (waypoint.importance <= 0) Waypoint.waypoints.remove(waypoint);
            }

            Waypoint.drawText(textBuffer);
            textBuffer.draw(); //maybe change where this calls in the future

            assert waypointBuffer != null;
            BuiltBuffer builtWaypointBuffer = waypointBuffer.end();

            BuiltBuffer.DrawParameters waypointParameters = builtWaypointBuffer.getDrawParameters();
            VertexFormat waypointFormat = waypointParameters.format();

            GpuBuffer vertices = upload3d(waypointParameters, waypointFormat, builtWaypointBuffer);

            draw3d(client, pipeline, builtWaypointBuffer, waypointParameters, vertices, waypointFormat);

            vertexBuffer.rotate();
            waypointBuffer = null;

        }

    }

    public static void render3d(WorldRenderContext context) {

        ClientPlayerEntity player = client.player;
        if (player == null) return;
        if (client.world == null) return;

        renderTick ++;
        renderTick %= maxRenderTick;

        renderContext = context;
        modelViewStack = RenderSystem.getModelViewStack();

        //create view matrix stack
        Vec3d camPos = renderContext.camera().getPos();
        modelViewStack.pushMatrix();
        modelViewStack.translate((float) -camPos.x, (float) -camPos.y, (float) -camPos.z);

        Waypoint.updateWaypoint(0, -58, 0, Waypoint.Type.GOOD_GUY,    "uuid 1", "good guy");
        Waypoint.updateWaypoint(1, -58, 0, Waypoint.Type.NORMAL,      "uuid 2", "normal");
        Waypoint.updateWaypoint(2, -58, 0, Waypoint.Type.SHITTER,     "uuid 3", "shitter");
        Waypoint.updateWaypoint(3, -58, 0, Waypoint.Type.HITLER,      "uuid 4", "hitler");
        Waypoint.updateWaypoint(4, -58, 0, Waypoint.Type.SNITCH,      "uuid 5", "snitch");
        Waypoint.updateWaypoint(5, -58, 0, Waypoint.Type.SNITCH_ALERT,"uuid 6", "snitch alert");
        Waypoint.updateWaypoint(6, -58, 0, Waypoint.Type.PING,        "uuid 7", "ping");
        Waypoint.updateWaypoint(7, -58, 0, Waypoint.Type.ALERT,       "uuid 8", "panic");
        Waypoint.updateWaypoint(8, -58, 0, Waypoint.Type.PERMANENT,   "uuid 18","permanent");

        //TODO: all rendering should be through shapes

        renderWaypoints(fillUnnocluded);

        //TODO: Implement Positive and Negative drawing modes
        for (Layer layer : Layer.layers) layer.prepare();
        for (Layer layer : Layer.layers) layer.render();

        modelViewStack.popMatrix();

    }

    public void close() {
        allocator.close();

        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }


}

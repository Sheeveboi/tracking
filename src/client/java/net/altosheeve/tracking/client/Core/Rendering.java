package net.altosheeve.tracking.client.Core;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.altosheeve.tracking.client.Mapping.Waypoint;
import net.altosheeve.tracking.client.Shapes.Layer;
import net.altosheeve.tracking.client.Shapes.Shape;
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

    public static final RenderPipeline Positive = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of("tracking", "pipeline/positive"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build()
    );

    private static final BufferAllocator allocator = new BufferAllocator(RenderLayer.CUTOUT_BUFFER_SIZE);
    private static MappableRingBuffer vertexBuffer;
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);

    public static float scalingFunction(float scale, Waypoint.Type type, float x, float y, float z) {
        float originalScale = scale * Values.scaleRegistry(type);
        return originalScale * (float) (0.005f * (Rendering.client.player.getEyePos().distanceTo(new Vec3d(x + .5, y - .5, z + .5)) / (Math.E)));
    }

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

    private static void draw3d(MinecraftClient client, RenderPipeline pipeline, BuiltBuffer builtBuffer, BuiltBuffer.DrawParameters drawParameters, GpuBuffer vertices, VertexFormat format) {
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
                .write(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, RenderSystem.getModelOffset(), RenderSystem.getTextureMatrix(), 1f);
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

        // TODO: do not construct all of this every frame! that's wasteful..
        // TODO: Implement dynamic reconstruction so the buffer is only rebuilt when a change is made. this is here because its easy

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

//    public static void renderShapes() {
//
//        //TODO: Move waypoint rendering into more general Shapes
//        //TODO: Implement Positive and Negative drawing modes
//
//        if (!Shape.shapes.isEmpty()) {
//
//            BufferBuilder shapesBuffer = new BufferBuilder(allocator, Positive.getVertexFormatMode(), Positive.getVertexFormat());
//
//            for (Shape shape : Shape.shapes) shape.set(shapesBuffer);
//
//            //TODO: map all these values to an object and have draw3d accept the object
//            BuiltBuffer builtMapBuffer = shapesBuffer.end();
//
//            BuiltBuffer.DrawParameters shapesParameters = builtMapBuffer.getDrawParameters();
//            VertexFormat shapesFormat = shapesParameters.format();
//
//            GpuBuffer vertices = upload3d(shapesParameters, shapesFormat, builtMapBuffer);
//
//            draw3d(client, Rendering.Positive, builtMapBuffer, shapesParameters, vertices, shapesFormat);
//
//            vertexBuffer.rotate();
//            shapesBuffer = null;
//
//        }
//
//    }

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

        Waypoint.updateWaypoint(0, -59, 0, Waypoint.Type.GOOD_GUY, "testuuid", "testusername");

        //TODO: all rendering should be through shapes
        renderWaypoints(Positive);
        //renderShapes();

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

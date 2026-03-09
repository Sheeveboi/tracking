package net.altosheeve.tracking.client.Core;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.altosheeve.tracking.client.Shapes.Shape;
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

        //TODO: Implement Positive and Negative drawing modes
        for (Layer layer : Layer.layers) layer.prepare();
        for (Layer layer : Layer.layers) layer.render();

        //TODO: implement text layers
        VertexConsumerProvider.Immediate textBuffer = Rendering.client.getBufferBuilders().getEntityVertexConsumers();
        Waypoint.drawText(textBuffer);
        textBuffer.draw();

        Waypoint.cleanWaypoints();

        modelViewStack.popMatrix();

    }


}

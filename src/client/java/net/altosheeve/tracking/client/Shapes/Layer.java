package net.altosheeve.tracking.client.Shapes;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.altosheeve.tracking.client.Core.Rendering;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.Objects;


public class Layer {

    public enum Method {
        FILL_UNOCCLUDED,
        FILL_OCCLUDED,
        LINE_UNOCCLUDED,
        LINE_OCCLUDED
    }

    public BuiltBuffer builtBuffer;
    public MappableRingBuffer vertexBuffer;
    public GpuBuffer gpuBuffer;
    public BuiltBuffer.DrawParameters parameters;
    public VertexFormat format;

    private final String name;
    public float drawPriority = 0;
    private boolean visible = true;

    public RenderPipeline pipeline;
    public Method pipelineName;

    private static final BufferAllocator allocator = new BufferAllocator(RenderLayer.CUTOUT_BUFFER_SIZE);

    public static ArrayList<Layer> layers = new ArrayList<>();
    public ArrayList<Shape> shapes = new ArrayList<>();

    public Layer(String name, Method pipeline) {
        for (Layer layer : layers)
            if (Objects.equals(layer.name, name)) throw new RuntimeException("Two layers may not share the same name");

        this.name = name;

        switch (pipeline) {
            case FILL_OCCLUDED -> this.pipeline = Rendering.fillOccluded;
            case FILL_UNOCCLUDED -> this.pipeline = Rendering.fillUnnocluded;
            case LINE_OCCLUDED -> this.pipeline = Rendering.lineOccluded;
            case LINE_UNOCCLUDED -> this.pipeline = Rendering.lineUnoccluded;
        }

        this.pipelineName = pipeline;

        this.update();

    }

    public void addShape(Shape shape) {
        shape.setParentLayer(this);
        this.shapes.add(shape);
        this.update();
    }

    public void removeShape(Shape shape) {
        this.shapes.remove(shape);
        this.update();
    }

    public void setDrawPriority(float priority) {
        this.drawPriority = priority;
        this.update();
    }

    public void setLineWidth(float width) {
        this.lineWidth = width;
        this.update();
    }

    public void update() {

        for (Layer layer : layers) {
            if (Objects.equals(layer.name, this.name)) {
                layers.remove(layer);
                break;
            }
        }

        for (Layer layer : layers) {
            if (layer.drawPriority > this.drawPriority) {
                layers.add(layers.indexOf(layer), this);
                return;
            }
        }

        //sometimes the layer removed might have been the only one to exist and the second for loop wont enter
        layers.add(this);

    }

    public void prepare() {

        if (this.shapes.isEmpty() || !this.visible) return;
        System.out.println("preparing");
        System.out.println(this.name);

        BufferBuilder shapesBuffer = new BufferBuilder(allocator, this.pipeline.getVertexFormatMode(), this.pipeline.getVertexFormat());

        for (Shape shape : this.shapes) shape.set(shapesBuffer);

        this.builtBuffer = shapesBuffer.end();

        this.parameters = this.builtBuffer.getDrawParameters();
        this.format = this.parameters.format();

        // Calculate the size needed for the vertex buffer
        int vertexBufferSize = this.parameters.vertexCount() * this.format.getVertexSize();

        try {
            this.vertexBuffer = new MappableRingBuffer(() -> this.name, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Copy vertex data into the vertex buffer
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(this.vertexBuffer.getBlocking().slice(0, this.builtBuffer.getBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(this.builtBuffer.getBuffer(), mappedView.data());
        }

        this.gpuBuffer = this.vertexBuffer.getBlocking();

    }

    public void render() {

        if (this.shapes.isEmpty() || !this.visible) return;

        Rendering.draw3d(Rendering.client, this.pipeline, this.builtBuffer, parameters, this.gpuBuffer, this.format);

        this.vertexBuffer.rotate();
        this.builtBuffer = null;

    }

    public void invisible() {
        this.visible = false;
        this.update();
    }

    public void visible() {
        this.visible = true;
        this.update();
    }

}

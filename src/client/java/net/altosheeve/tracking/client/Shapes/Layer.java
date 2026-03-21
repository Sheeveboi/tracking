package net.altosheeve.tracking.client.Shapes;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.altosheeve.tracking.client.Core.Rendering;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;


public class Layer {

    public enum Method {
        FILL_UNOCCLUDED,
        FILL_OCCLUDED,
        LINE_UNOCCLUDED,
        LINE_OCCLUDED
    }

    public BufferBuilder shapesBuffer;
    public BuiltBuffer builtBuffer;
    public MappableRingBuffer vertexBuffer;
    public GpuBuffer gpuBuffer;
    public BuiltBuffer.DrawParameters parameters;
    public VertexFormat format;

    private final String name;
    public float drawPriority = 0;
    public float lineWidth = 1;
    private boolean visible = true;

    public RenderPipeline pipeline;
    public Method pipelineName;

    public static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);

    public static final BufferAllocator allocator = new BufferAllocator(RenderLayer.CUTOUT_BUFFER_SIZE);

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
            if (layer.drawPriority < this.drawPriority) {
                layers.add(layers.indexOf(layer), this);
                return;
            }
        }

        //sometimes the layer removed might have been the only one to exist and the second for loop won't enter
        layers.add(this);

    }

    public void prepare() {

        if (this.shapes.isEmpty() || !this.visible) return;

        if (this.shapesBuffer == null) this.shapesBuffer = new BufferBuilder(allocator, this.pipeline.getVertexFormatMode(), this.pipeline.getVertexFormat());

        RenderSystem.lineWidth(this.lineWidth);

        for (Shape shape : new ArrayList<>(this.shapes)) if (shape != null) {
            shape.parentLayer = this;
            shape.set(this.shapesBuffer);
        }

        this.builtBuffer = this.shapesBuffer.end();
        this.parameters = this.builtBuffer.getDrawParameters();
        this.format = this.parameters.format();

        this.gpuBuffer = this.upload(this.parameters, this.format, this.builtBuffer);

    }

    public void render() {

        if (this.shapes.isEmpty() || !this.visible) return;

        this.draw3d(Rendering.client, this.pipeline, this.builtBuffer, parameters, this.gpuBuffer, this.format);

        this.vertexBuffer.rotate();
        this.shapesBuffer = null;
        this.builtBuffer = null;

    }

    public GpuBuffer upload(BuiltBuffer.DrawParameters drawParameters, VertexFormat format, BuiltBuffer builtBuffer) {
        // Calculate the size needed for the vertex buffer
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        // Initialize or resize the vertex buffer as needed
        if (this.vertexBuffer == null || this.vertexBuffer.size() < vertexBufferSize) {
            if (this.vertexBuffer != null) {
                this.vertexBuffer.close();
            }

            this.vertexBuffer = new MappableRingBuffer(() -> "test" + " example render pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        // Copy vertex data into the vertex buffer
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(this.vertexBuffer.getBlocking().slice(0, builtBuffer.getBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.getBuffer(), mappedView.data());
        }

        com.mojang.blaze3d.buffers.GpuBuffer out = this.vertexBuffer.getBlocking();
        return out;
    }

    public void draw3d(MinecraftClient client, RenderPipeline pipeline, BuiltBuffer builtBuffer, BuiltBuffer.DrawParameters drawParameters, GpuBuffer vertices, VertexFormat format) {
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
                .createRenderPass(() -> "test" + " example render pipeline rendering", client.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), client.getFramebuffer().getDepthAttachmentView(), OptionalDouble.empty())) {
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

    public void invisible() {
        this.visible = false;
        this.update();
    }

    public void visible() {
        this.visible = true;
        this.update();
    }

    public void updateShape(String UUID, Shape newShape) {
        for (Shape shape : this.shapes) {
            if (Objects.equals(shape.UUID, UUID)) {

                newShape.parentLayer = this;
                newShape.parentShape = shape.parentShape;
                newShape.children = shape.children;

                shape = newShape;
                newShape = null;

                break;
            }
        }
    }

}

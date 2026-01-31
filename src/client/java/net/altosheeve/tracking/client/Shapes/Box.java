package net.altosheeve.tracking.client.Shapes;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.render.BufferBuilder;
import org.joml.Matrix4f;

public class Box extends Shape{


    public Box(float x, float y, float z, RenderPipeline method) {
        super(x, y, z, method);
    }

    public Box(float x, float y, float z, float w, float h, float d, RenderPipeline method) {
        super(x, y, z, w, h, d, method);
    }

    public Box(float x, float y, float z, float w, float h, float d, float r, float g, float b, float a, RenderPipeline method) {
        super(x, y, z, w, h, d, r, g, b, a, method);
    }

    @Override
    public void draw(BufferBuilder buffer, Matrix4f transform) {

        super.draw(buffer, transform);

        //left face
        buffer.vertex(transform, this.finalX, this.finalY, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX + this.w, this.finalY, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX + this.w, this.finalY + this.h, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX, this.finalY + this.w, this.finalZ).color(this.r, this.g, this.b, this.a);

        //right face
        buffer.vertex(transform, this.finalX, this.finalY, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX + this.w, this.finalY, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX + this.w, this.finalY + this.h, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX, this.finalY + this.w, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);

        //bottom face
        buffer.vertex(transform, this.finalX, this.finalY, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX, this.finalY, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX + this.w, this.finalY, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX + this.w, this.finalY, this.finalZ).color(this.r, this.g, this.b, this.a);

        //top face
        buffer.vertex(transform, this.finalX, this.finalY + this.h, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX, this.finalY + this.h, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX + this.w, this.finalY + this.h, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX + this.w, this.finalY + this.h, this.finalZ).color(this.r, this.g, this.b, this.a);

        //front face
        buffer.vertex(transform, this.finalX, this.finalY, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX, this.finalY, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX, this.finalY + this.h, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX, this.finalY + this.h, this.finalZ).color(this.r, this.g, this.b, this.a);

        //back face
        buffer.vertex(transform, this.finalX + this.w, this.finalY, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX + this.w, this.finalY, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX + this.w, this.finalY + this.h, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.finalX + this.w, this.finalY + this.h, this.finalZ).color(this.r, this.g, this.b, this.a);
    }
}

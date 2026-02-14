package net.altosheeve.tracking.client.Shapes;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.render.BufferBuilder;
import org.joml.Matrix4f;

public class Box extends Shape{


    public Box(float x, float y, float z) {
        super(x, y, z);
    }

    @Override
    public void draw(BufferBuilder buffer) {

        //left face
        buffer.vertex(this.activeTransform, this.finalX, this.finalY, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX + this.w, this.finalY, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX + this.w, this.finalY + this.h, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX, this.finalY + this.w, this.finalZ).color(this.r, this.g, this.b, this.a);

        //right face
        buffer.vertex(this.activeTransform, this.finalX, this.finalY, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX + this.w, this.finalY, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX + this.w, this.finalY + this.h, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX, this.finalY + this.w, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);

        //bottom face
        buffer.vertex(this.activeTransform, this.finalX, this.finalY, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX, this.finalY, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX + this.w, this.finalY, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX + this.w, this.finalY, this.finalZ).color(this.r, this.g, this.b, this.a);

        //top face
        buffer.vertex(this.activeTransform, this.finalX, this.finalY + this.h, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX, this.finalY + this.h, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX + this.w, this.finalY + this.h, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX + this.w, this.finalY + this.h, this.finalZ).color(this.r, this.g, this.b, this.a);

        //front face
        buffer.vertex(this.activeTransform, this.finalX, this.finalY, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX, this.finalY, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX, this.finalY + this.h, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX, this.finalY + this.h, this.finalZ).color(this.r, this.g, this.b, this.a);

        //back face
        buffer.vertex(this.activeTransform, this.finalX + this.w, this.finalY, this.finalZ).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX + this.w, this.finalY, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX + this.w, this.finalY + this.h, this.finalZ + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.activeTransform, this.finalX + this.w, this.finalY + this.h, this.finalZ).color(this.r, this.g, this.b, this.a);

    }
}

package net.altosheeve.tracking.client.Render.Util;

import net.minecraft.client.render.BufferBuilder;
import org.joml.Matrix4f;

public class RenderBox{
    public int x;
    public int y;
    public int z;

    public float w;
    public float h;
    public float d;

    public float r;
    public float g;
    public float b;
    public float a;

    public RenderBox(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.w = 1;
        this.h = 1;
        this.d = 1;

        this.r = 1f;
        this.g = 0f;
        this.b = 1f;
        this.a = 1f;
    }

    public RenderBox(int x, int y, int z, float w, float h, float d) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.w = w;
        this.h = h;
        this.d = d;

        this.r = 1f;
        this.g = 0f;
        this.b = 1f;
        this.a = 1f;
    }

    public RenderBox(int x, int y, int z, float w, float h, float d, float r, float g, float b, float a) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.w = w;
        this.h = h;
        this.d = d;

        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void draw(BufferBuilder buffer) {
        //left face
        buffer.vertex(this.x, this.y, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x + this.w, this.y, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x + this.w, this.y + this.h, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x, this.y + this.w, this.z).color(this.r, this.g, this.b, this.a);

        //right face
        buffer.vertex(this.x, this.y, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x + this.w, this.y, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x + this.w, this.y + this.h, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x, this.y + this.w, this.z + this.d).color(this.r, this.g, this.b, this.a);

        //bottom face
        buffer.vertex(this.x, this.y, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x, this.y, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x + this.w, this.y, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x + this.w, this.y, this.z).color(this.r, this.g, this.b, this.a);

        //top face
        buffer.vertex(this.x, this.y + this.h, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x, this.y + this.h, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x + this.w, this.y + this.h, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x + this.w, this.y + this.h, this.z).color(this.r, this.g, this.b, this.a);

        //front face
        buffer.vertex(this.x, this.y, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x, this.y, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x, this.y + this.h, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x, this.y + this.h, this.z).color(this.r, this.g, this.b, this.a);

        //back face
        buffer.vertex(this.x + this.w, this.y, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x + this.w, this.y, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x + this.w, this.y + this.h, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(this.x + this.w, this.y + this.h, this.z).color(this.r, this.g, this.b, this.a);
    }

    public void draw(BufferBuilder buffer, Matrix4f transform) {
        //left face
        buffer.vertex(transform, this.x, this.y, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x + this.w, this.y, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x + this.w, this.y + this.h, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x, this.y + this.w, this.z).color(this.r, this.g, this.b, this.a);

        //right face
        buffer.vertex(transform, this.x, this.y, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x + this.w, this.y, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x + this.w, this.y + this.h, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x, this.y + this.w, this.z + this.d).color(this.r, this.g, this.b, this.a);

        //bottom face
        buffer.vertex(transform, this.x, this.y, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x, this.y, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x + this.w, this.y, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x + this.w, this.y, this.z).color(this.r, this.g, this.b, this.a);

        //top face
        buffer.vertex(transform, this.x, this.y + this.h, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x, this.y + this.h, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x + this.w, this.y + this.h, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x + this.w, this.y + this.h, this.z).color(this.r, this.g, this.b, this.a);

        //front face
        buffer.vertex(transform, this.x, this.y, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x, this.y, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x, this.y + this.h, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x, this.y + this.h, this.z).color(this.r, this.g, this.b, this.a);

        //back face
        buffer.vertex(transform, this.x + this.w, this.y, this.z).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x + this.w, this.y, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x + this.w, this.y + this.h, this.z + this.d).color(this.r, this.g, this.b, this.a);
        buffer.vertex(transform, this.x + this.w, this.y + this.h, this.z).color(this.r, this.g, this.b, this.a);
    }
}

package net.altosheeve.tracking.client.Shapes;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.render.BufferBuilder;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class Shape {

    public float x;
    public float y;
    public float z;
    
    public float finalX = 0;
    public float finalY = 0;
    public float finalZ = 0;

    public float w;
    public float h;
    public float d;

    public float r;
    public float g;
    public float b;
    public float a;

    public RenderPipeline method;

    public Shape parent;

    public ArrayList<Shape> children = new ArrayList<>();
    public static ArrayList<Shape> shapes = new ArrayList<>();

    public Shape(float x, float y, float z, RenderPipeline method) {
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

        this.method = method;
    }

    public Shape(float x, float y, float z, float w, float h, float d, RenderPipeline method) {
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

        this.method = method;
    }

    public Shape(float x, float y, float z, float w, float h, float d, float r, float g, float b, float a, RenderPipeline method) {
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

        this.method = method;
    }

    public void addShape(Shape shape) {
        shape.parent = this;
        this.children.add(shape);
    }

    public void draw(BufferBuilder buffer, Matrix4f transform) {

        if (this.parent != null) {

            this.finalX = this.x + this.parent.finalX;
            this.finalY = this.y + this.parent.finalY;
            this.finalZ = this.z + this.parent.finalZ;

        }

        else {

            this.finalX = this.x;
            this.finalY = this.y;
            this.finalZ = this.z;

        }

        for (Shape shape : this.children) shape.draw(buffer, transform);
    }
}

package net.altosheeve.tracking.client.Shapes;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.render.BufferBuilder;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import java.util.ArrayList;

public class Shape {

    public float x;
    public float y;
    public float z;
    
    public float finalX = 0;
    public float finalY = 0;
    public float finalZ = 0;

    public AxisAngle4f rotationX = new AxisAngle4f(0, 1, 0, 0);
    public AxisAngle4f rotationY = new AxisAngle4f(0, 0, 1, 0);
    public AxisAngle4f rotationZ = new AxisAngle4f(0, 0, 0, 1);

    public float w;
    public float h;
    public float d;

    public float scalex = 1;
    public float scaley = 1;
    public float scalez = 1;

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

    public void position(float x, float y, float z) {

        this.x = x;
        this.y = y;
        this.z = z;

    }

    public void size(float w, float h, float d) {

        this.w = w;
        this.h = h;
        this.d = d;

    }

    public void color(float r, float g, float b, float a) {

        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;

    }

    public void rotation(float rx, float ry, float rz) {

        this.rotationX.angle = rx;
        this.rotationY.angle = ry;
        this.rotationZ.angle = rz;

    }

    public void scale(float x, float y, float z) {

        this.scalex = x;
        this.scaley = y;
        this.scalez = z;

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

        //TODO: set translation to be carried through the transform as well

        transform = transform.rotateAround(new Quaternionf(this.rotationX), this.finalX, this.finalY, this.finalZ);
        transform = transform.rotateAround(new Quaternionf(this.rotationY), this.finalX, this.finalY, this.finalZ);
        transform = transform.rotateAround(new Quaternionf(this.rotationZ), this.finalX, this.finalY, this.finalZ);

        transform = transform.scaleAround(scalex, scaley, scalez, this.finalX, this.finalY, this.finalZ);

        for (Shape shape : this.children) shape.draw(buffer, transform);
    }
}

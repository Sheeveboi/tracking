package net.altosheeve.tracking.client.Shapes;

import net.minecraft.client.render.BufferBuilder;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

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

    public float ox;
    public float oy;
    public float oz;

    public float r;
    public float g;
    public float b;
    public float a;

    public boolean fillVisible = true;
    public boolean lineVisible = true;

    public Shape parentShape;
    public Layer parentLayer;

    public ArrayList<Shape> children = new ArrayList<>();
    public Matrix4f startingTransform;
    public Matrix4f activeTransform;

    public Shape(float x, float y, float z) {

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

    public void origin(float x, float y, float z) {

        this.ox = x;
        this.oy = y;
        this.oz = z;

    }

    public void transform(Matrix4f transform) {
        this.startingTransform = transform;
    }

    public void setFillInvisible() {
        this.fillVisible = false;
    }

    public void setFillVisible() {
        this.fillVisible = true;
    }

    public void setLineInvisible() {
        this.lineVisible = false;
    }

    public void setLineVisible() {
        this.lineVisible = true;
    }

    public void setParentLayer(Layer layer) {
        this.parentLayer = layer;
        for (Shape child : this.children) child.setParentLayer(layer);
    }

    public void addShape(Shape shape) {
        shape.parentShape = this;
        shape.setParentLayer(shape.parentLayer);
        this.children.add(shape);
    }

    public void set(BufferBuilder buffer, Matrix4f transform) {

        transform = transform.translate(this.x, this.y, this.z);

        transform = transform.scaleAround(scalex, scaley, scalez, this.x  + this.ox, this.y + this.oy, this.z + this.oz);

        transform = transform.rotateAround(new Quaternionf(this.rotationX), this.x  + this.ox, this.y + this.oy, this.z + this.oz);
        transform = transform.rotateAround(new Quaternionf(this.rotationY), this.x  + this.ox, this.y + this.oy, this.z + this.oz);
        transform = transform.rotateAround(new Quaternionf(this.rotationZ), this.x  + this.ox, this.y + this.oy, this.z + this.oz);

        this.activeTransform = transform;

        for (Shape shape : this.children) shape.set(buffer, transform);

        if (this.fillVisible && (this.parentLayer.pipelineName == Layer.Method.FILL_UNOCCLUDED || this.parentLayer.pipelineName == Layer.Method.FILL_OCCLUDED)) this.fill(buffer);
        if (this.lineVisible && (this.parentLayer.pipelineName == Layer.Method.LINE_UNOCCLUDED || this.parentLayer.pipelineName == Layer.Method.LINE_OCCLUDED)) this.line(buffer);

        this.activeTransform = null;

    }

    public void set(BufferBuilder buffer) {

        this.activeTransform = new Matrix4f();

        if (this.startingTransform != null)
            this.activeTransform = this.startingTransform;

        else if (this.parentShape != null && this.parentShape.activeTransform != null)
            this.activeTransform = this.parentShape.activeTransform;

        this.activeTransform = this.activeTransform.translate(this.x, this.y, this.z);

        this.activeTransform = this.activeTransform.scaleAround(scalex, scaley, scalez, this.x  + this.ox, this.y + this.oy, this.z + this.oz);

        this.activeTransform = this.activeTransform.rotateAround(new Quaternionf(this.rotationX), this.x  + this.ox, this.y + this.oy, this.z + this.oz);
        this.activeTransform = this.activeTransform.rotateAround(new Quaternionf(this.rotationY), this.x  + this.ox, this.y + this.oy, this.z + this.oz);
        this.activeTransform = this.activeTransform.rotateAround(new Quaternionf(this.rotationZ), this.x  + this.ox, this.y + this.oy, this.z + this.oz);

        for (Shape shape : this.children) shape.set(buffer);

        if (this.fillVisible && (this.parentLayer.pipelineName == Layer.Method.FILL_UNOCCLUDED || this.parentLayer.pipelineName == Layer.Method.FILL_OCCLUDED)) this.fill(buffer);
        if (this.lineVisible && (this.parentLayer.pipelineName == Layer.Method.LINE_UNOCCLUDED || this.parentLayer.pipelineName == Layer.Method.LINE_OCCLUDED)) this.line(buffer);

        this.activeTransform = null;

    }

    public void fill(BufferBuilder buffer) {};
    public void line(BufferBuilder buffer) {};
}

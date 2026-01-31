package net.altosheeve.tracking.client.Shapes;

import net.minecraft.client.render.BufferBuilder;
import org.joml.Matrix4f;

public class Circle {
    public int x;
    public int y;
    public int z;

    public float innerRadius;
    public float outerRadius;

    public float r;
    public float g;
    public float b;
    public float a;

    public Circle(int x, int y, int z, float innerRadius, float outerRadius) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;

        this.r = 1f;
        this.g = 0f;
        this.b = 1f;
        this.a = 1f;
    }

    public Circle(int x, int y, int z, float r, float g, float b, float a, float innerRadius, float outerRadius) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;

        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void draw(BufferBuilder buffer) {
        for (int i = 0; i <= 359; i++) {

            float innerLeftX = (float) Math.sin(i * Math.PI / 180.0D) * this.innerRadius;
            float innerLeftY = (float) Math.cos(i * Math.PI / 180.0D) * this.innerRadius;

            float outerLeftX = (float) Math.sin(i * Math.PI / 180.0D) * this.outerRadius;
            float outerLeftY = (float) Math.cos(i * Math.PI / 180.0D) * this.outerRadius;

            float innerRightX = (float) Math.sin((i + 1) * Math.PI / 180.0D) * this.innerRadius;
            float innerRightY = (float) Math.cos((i + 1) * Math.PI / 180.0D) * this.innerRadius;

            float outerRightX = (float) Math.sin((i + 1) * Math.PI / 180.0D) * this.outerRadius;
            float outerRightY = (float) Math.cos((i + 1) * Math.PI / 180.0D) * this.outerRadius;

            buffer.vertex(this.x + innerLeftX, this.y + innerLeftY, 0).color(this.r, this.g, this.b, this.a);
            buffer.vertex(this.x + outerLeftX, this.y + outerLeftY, 0).color(this.r, this.g, this.b, this.a);
            buffer.vertex(this.x + outerRightX, this.y + outerRightY, 0).color(this.r, this.g, this.b, this.a);
            buffer.vertex(this.x + innerRightX, this.y + innerRightY, 0).color(this.r, this.g, this.b, this.a);
        }
    }

    public void draw(BufferBuilder buffer, Matrix4f transform) {
        for (int i = 0; i <= 359; i++) {

            float innerLeftX = (float) Math.sin(i * Math.PI / 180.0D) * this.innerRadius;
            float innerLeftY = (float) Math.cos(i * Math.PI / 180.0D) * this.innerRadius;

            float outerLeftX = (float) Math.sin(i * Math.PI / 180.0D) * this.outerRadius;
            float outerLeftY = (float) Math.cos(i * Math.PI / 180.0D) * this.outerRadius;

            float innerRightX = (float) Math.sin((i + 1) * Math.PI / 180.0D) * this.innerRadius;
            float innerRightY = (float) Math.cos((i + 1) * Math.PI / 180.0D) * this.innerRadius;

            float outerRightX = (float) Math.sin((i + 1) * Math.PI / 180.0D) * this.outerRadius;
            float outerRightY = (float) Math.cos((i + 1) * Math.PI / 180.0D) * this.outerRadius;

            buffer.vertex(transform, this.x + innerLeftX, this.y + innerLeftY, 0).color(this.r, this.g, this.b, this.a);
            buffer.vertex(transform, this.x + outerLeftX, this.y + outerLeftY, 0).color(this.r, this.g, this.b, this.a);
            buffer.vertex(transform, this.x + outerRightX, this.y + outerRightY, 0).color(this.r, this.g, this.b, this.a);
            buffer.vertex(transform, this.x + innerRightX, this.y + innerRightY, 0).color(this.r, this.g, this.b, this.a);
        }
    }
}

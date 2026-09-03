package net.altosheeve.tracking.client.Shapes;

import net.minecraft.client.render.BufferBuilder;
import org.joml.Matrix4f;

public class Circle extends Shape {

    public float innerRadius;
    public float outerRadius;

    public int resolution = 20;

    public Circle(float x, float y, float z, String UUID, float innerRadius, float outerRadius) {
        super(x, y, z, UUID);
        this.color(1f, 0f, 1f, 1f);

        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
    }

    public Circle(float x, float y, float z, float r, float g, float b, float a, String UUID, float innerRadius, float outerRadius) {
        super(x, y, z, UUID);
        this.color(r, g, b, a);

        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
    }

    @Override
    public void fill(BufferBuilder buffer) {

        super.fill(buffer);

        float ratio = 360f / resolution;

        for (int i = 0; i < resolution; i++) {

            float itterPosLeft = (float) (i * ratio * Math.PI / 180.0D);
            float itterPosRight = (float) ((i + 1) * ratio * Math.PI / 180.0D);

            float innerLeftX = (float) Math.sin(itterPosLeft) * this.innerRadius;
            float innerLeftY = (float) Math.cos(itterPosLeft) * this.innerRadius;

            float outerLeftX = (float) Math.sin(itterPosLeft) * this.outerRadius;
            float outerLeftY = (float) Math.cos(itterPosLeft) * this.outerRadius;

            float innerRightX = (float) Math.sin(itterPosRight) * this.innerRadius;
            float innerRightY = (float) Math.cos(itterPosRight) * this.innerRadius;

            float outerRightX = (float) Math.sin(itterPosRight) * this.outerRadius;
            float outerRightY = (float) Math.cos(itterPosRight) * this.outerRadius;

            buffer.vertex(this.activeTransform, this.finalX + innerLeftX, this.finalY + innerLeftY, this.finalZ).color(this.r, this.g, this.b, this.a);
            buffer.vertex(this.activeTransform,this.finalX + outerLeftX, this.finalY + outerLeftY, this.finalZ).color(this.r, this.g, this.b, this.a);
            buffer.vertex(this.activeTransform,this.finalX + outerRightX, this.finalY + outerRightY, this.finalZ).color(this.r, this.g, this.b, this.a);
            buffer.vertex(this.activeTransform,this.finalX + innerRightX, this.finalY + innerRightY, this.finalZ).color(this.r, this.g, this.b, this.a);

        }
    }
}

package net.altosheeve.tracking.client.Shapes;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.render.BufferBuilder;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class Grid extends Shape {

    public float xScale = 1f;
    public float zScale = 1f;
    public float margins = 0f;

    ArrayList<ArrayList<Float>> yLevels = new ArrayList<>();

    private void initYLevels() {

        for (int z = 0; z < this.h; z++) {

            ArrayList<Float> zLevel = new ArrayList<>();

            for (int x = 0; x < this.w; x++) zLevel.add(0f);

            yLevels.add(zLevel);

        }

    }

    public Grid(float x, float y, float z, float xScale, float zScale, RenderPipeline method) {
        super(x, y, z, method);
        this.initYLevels();
        this.xScale = xScale;
        this.zScale = zScale;
    }

    public Grid(float x, float y, float z, float w, float h, float d, float xScale, float zScale, RenderPipeline method) {
        super(x, y, z, w, h, d, method);
        this.initYLevels();
        this.xScale = xScale;
        this.zScale = zScale;
    }

    public Grid(float x, float y, float z, float w, float h, float d, float r, float g, float b, float a, float xScale, float zScale, RenderPipeline method) {
        super(x, y, z, w, h, d, r, g, b, a, method);
        this.initYLevels();
        this.xScale = xScale;
        this.zScale = zScale;
    }

    public Grid(float x, float y, float z, float xScale, float zScale, float margins, RenderPipeline method) {
        super(x, y, z, method);
        this.initYLevels();
        this.xScale = xScale;
        this.zScale = zScale;
        this.margins = margins;
    }

    public Grid(float x, float y, float z, float w, float h, float d, float xScale, float zScale, float margins, RenderPipeline method) {
        super(x, y, z, w, h, d, method);
        this.initYLevels();
        this.xScale = xScale;
        this.zScale = zScale;
        this.margins = margins;
    }

    public Grid(float x, float y, float z, float w, float h, float d, float r, float g, float b, float a, float xScale, float zScale, float margins, RenderPipeline method) {
        super(x, y, z, w, h, d, r, g, b, a, method);
        this.initYLevels();
        this.xScale = xScale;
        this.zScale = zScale;
        this.margins = margins;
    }

    public void addYLevel(int x, int z, float y) {
        this.yLevels.get(z).set(x, y);
    }

    @Override
    public void draw(BufferBuilder buffer, Matrix4f transform) {

        super.draw(buffer, transform);

        for (int squareZ = 0; squareZ < this.h; squareZ++) {

            for (int squareX = 0; squareX < this.w; squareX++) {

                float gridY = this.finalY + this.yLevels.get(squareZ).get(squareX);

                float gridX = this.finalX + squareX * this.xScale;
                float gridZ = this.finalZ + squareZ * this.zScale;

                buffer.vertex(transform, this.margins + gridX,               gridY, gridZ + this.margins         ).color(this.r, this.g, this.b, this.a); //bottom left corner
                buffer.vertex(transform, gridX + this.xScale - this.margins, gridY, gridZ + this.margins         ).color(this.r, this.g, this.b, this.a); //bottom right corner
                buffer.vertex(transform, gridX + this.xScale - this.margins, gridY, gridZ + this.zScale - margins).color(this.r, this.g, this.b, this.a); //top right corner
                buffer.vertex(transform, this.margins + gridX,               gridY, gridZ + this.zScale - margins).color(this.r, this.g, this.b, this.a); //top left corner

            }

        }

    }
}

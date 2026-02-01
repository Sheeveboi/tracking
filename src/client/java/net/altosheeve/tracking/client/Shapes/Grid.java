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

    public Grid(float x, float y, float z, RenderPipeline method) {
        super(x, y, z, method);
        initYLevels();
    }

    public Grid cellsize(float xScale, float zScale) {

        this.xScale = xScale;
        this.zScale = zScale;

        return this;

    }

    public Grid margins(float margins) {

        this.margins = margins;

        return this;

    }

    private void initYLevels() {

        for (int iz = 0; iz < this.h; iz++) {

            ArrayList<Float> zLevel = new ArrayList<>();

            for (int ix = 0; ix < this.w; ix++) zLevel.add(0f);

            this.yLevels.add(zLevel);

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

                //TODO: worry about this later
                //float gridY = this.finalY + this.yLevels.get(squareZ).get(squareX);
                float gridY = this.finalY;

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

package net.altosheeve.tracking.client.Mapping;

import net.altosheeve.tracking.client.Shapes.Grid;
import net.altosheeve.tracking.client.Shapes.Shape;
import net.minecraft.client.render.BufferBuilder;

import java.util.ArrayList;

public class SmartPlane extends Shape {

    public static class GridPoint {

        public float x = 0;
        public float z = 0;

        public float weight = 0;

        public ArrayList<Shape> associatedShapes = new ArrayList<>();

        public GridPoint(float x, float z) {

            this.x = x;
            this.z = z;

        }

        //this will calculate irrespective of the grids transformation. Use identical transformations when using shapes as weights
        public void calculateWeight() {

            for (Shape shape : associatedShapes) this.weight += (float) Math.sqrt(Math.pow(this.x - shape.x, 2) + Math.pow(this.z - shape.z, 2));
            this.weight /= associatedShapes.size();

        }

    }

    public ArrayList<Shape> focusPoints = new ArrayList<>();

    public ArrayList<GridPoint> largeGridPoints = new ArrayList<>();
    public ArrayList<GridPoint> smallGridPoints = new ArrayList<>();

    public float largeGridScale = 0f;
    public float smallGridScale = 0f;


    public SmartPlane(float x, float y, float z, String UUID) {
        super(x, y, z, UUID);
    }



}

package net.altosheeve.tracking.client.Shapes;

public class Triangle extends Circle {

    public Triangle(float x, float y, float z, String UUID) {
        super(x, y, z, UUID, 0, 1);
        this.resolution = 3;
    }

}

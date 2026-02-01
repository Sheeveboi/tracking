package net.altosheeve.tracking.client.Render;

import net.altosheeve.tracking.client.Shapes.Box;
import net.altosheeve.tracking.client.Shapes.Grid;
import net.altosheeve.tracking.client.Shapes.Shape;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import static java.lang.Math.pow;

public class Map extends Screen {

    public static boolean renderMap = false;
    public static boolean panning = false;

    public static int staticWidth = 0;
    public static int staticHeight = 0;

    public static int pastMouseX = 0;
    public static int pastMouseY = 0;
    public static float mouseDelta = 0;
    public static float mouseDeltaX = 0;
    public static float mouseDeltaY = 0;

    public static float panX = 0;
    public static float panY = 0;
    public static float zoom = 1f;
    public static float zoomPower = .1f;

    private static Shape mapContainer = new Shape (-8,-8f,-31f, Rendering.Positive);
    private static Box   testBox1     = new Box   (0,0,0, Rendering.Positive);
    //private static Grid  testGrid1    = new Grid  (0,0,0f, 5, 5, 0,1, 1, .1f, Rendering.Positive);

    private static Shape groundPlane  = new Shape(0, 0, 0, Rendering.Positive);

    private static Grid  bottomLayer  = new Grid(
            0,0,0,
            2, 2, 0,
            0, 0, 1, 1,
            8, 8,
            Rendering.Positive);

    private static Grid  topLayer     = new Grid(
            0,.01f,0,
            2, 2, 0,
            0, 0, 0, 1,
            8, 8, 0.05f,
            Rendering.Positive);

    static {

        groundPlane.addShape(testBox1);
        groundPlane.addShape(bottomLayer);
        groundPlane.addShape(topLayer);

        mapContainer.addShape(groundPlane);
    }

    public Map(Text title) {
        super(title);
    }

    @Override
    public void init() {
        Shape.shapes.add(mapContainer);
        renderMap = true;
    }

    @Override
    public void close() {
        super.close();
        Shape.shapes.clear();
        renderMap = false;
    }

}

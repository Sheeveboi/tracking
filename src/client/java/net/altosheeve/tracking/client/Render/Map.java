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

    private static Shape mapContainer = new Shape (-2.5f,-3,-10f, Rendering.Positive);
    private static Box   testBox1     = new Box   (0,0,0, Rendering.Positive);
    private static Grid  testGrid1    = new Grid  (0,0,0f, 5, 5, 0,1, 1, .1f, Rendering.Positive);

    static {
        mapContainer.addShape(testBox1);
        mapContainer.addShape(testGrid1);
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

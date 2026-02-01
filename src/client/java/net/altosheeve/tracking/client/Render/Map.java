package net.altosheeve.tracking.client.Render;

import net.altosheeve.tracking.client.Shapes.Box;
import net.altosheeve.tracking.client.Shapes.Grid;
import net.altosheeve.tracking.client.Shapes.Shape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.SliderWidget;
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
    public static float panZ = 0;
    public static float zoom = 1f;
    public static float zoomPower = .1f;

    public static float mapRotationX = 0;
    public static float mapRotationY = 0;
    public static float mapRotationZ = 0;

    static Shape mapContainer = new Shape (-8,-8f,-31f, Rendering.Positive);
    static Shape map          = new Shape (0, 0, 0, Rendering.Positive);

    static {

        Box   testBox1    = new Box   (0,.1f,0, Rendering.Positive);

        Shape groundPlane = new Shape(0, 0, 0, Rendering.Positive);
        Grid  bottomLayer = new Grid(0, 0, 0, Rendering.Positive);
        Grid  topLayer    = new Grid(0, .0001f, 0, Rendering.Positive);

        map.color(0,0,0,0);

        bottomLayer.size(2, 2, 0);
        bottomLayer.cellsize(8, 8);
        bottomLayer.color(0, 0, 1, 1);

        topLayer.size(2, 2, 0);
        topLayer.cellsize(8, 8);
        topLayer.margins(.05f);
        topLayer.color(0, 0, 0, 1);

        groundPlane.addShape(bottomLayer);
        groundPlane.addShape(topLayer);

        map.addShape(groundPlane);

        mapContainer.addShape(testBox1);
        mapContainer.addShape(map);

    }

    public Map(Text title) {
        super(title);
    }

    @Override
    public void init() {
        Shape.shapes.add(mapContainer);
        renderMap = true;

        SliderWidget xWidget = new SliderWidget(
                0,
                0,
                100,
                20,
                Text.literal("Rot X"),
                0
        ) {
            @Override
            protected void updateMessage() {

            }

            @Override
            protected void applyValue() {
                mapRotationX = (float) this.value;
                mapContainer.rotation(mapRotationX, mapRotationY, mapRotationZ);
            }
        };

        SliderWidget yWidget = new SliderWidget(
                0,
                20,
                100,
                20,
                Text.literal("Rot Y"),
                0
        ) {
            @Override
            protected void updateMessage() {

            }

            @Override
            protected void applyValue() {
                mapRotationY = (float) this.value;
                mapContainer.rotation(mapRotationX, mapRotationY, mapRotationZ);
            }
        };

        SliderWidget zWidget = new SliderWidget(
                0,
                40,
                100,
                20,
                Text.literal("Rot Z"),
                0
        ) {
            @Override
            protected void updateMessage() {

            }

            @Override
            protected void applyValue() {
                mapRotationZ = (float) this.value;
                mapContainer.rotation(mapRotationX, mapRotationY, mapRotationZ);
            }
        };

        SliderWidget pxWidget = new SliderWidget(
                0,
                60,
                100,
                20,
                Text.literal("Pan X"),
                0
        ) {
            @Override
            protected void updateMessage() {

            }

            @Override
            protected void applyValue() {
                panX = (float) this.value * 3;
                map.position(panX, panY, panZ);
            }
        };

        SliderWidget pyWidget = new SliderWidget(
                0,
                80,
                100,
                20,
                Text.literal("Pan Y"),
                0
        ) {
            @Override
            protected void updateMessage() {

            }

            @Override
            protected void applyValue() {
                panY = (float) this.value * 3;
                map.position(panX, panY, panZ);
            }
        };

        SliderWidget pzWidget = new SliderWidget(
                0,
                100,
                100,
                20,
                Text.literal("Pan Z"),
                0
        ) {
            @Override
            protected void updateMessage() {

            }

            @Override
            protected void applyValue() {
                panZ = (float) this.value * 3;
                map.position(panX, panY, panZ);
            }
        };

        this.addDrawableChild(xWidget);
        this.addDrawableChild(yWidget);
        this.addDrawableChild(zWidget);
        this.addDrawableChild(pxWidget);
        this.addDrawableChild(pyWidget);
        this.addDrawableChild(pzWidget);

    }

    @Override
    public void close() {
        super.close();
        Shape.shapes.clear();
        renderMap = false;
    }

}

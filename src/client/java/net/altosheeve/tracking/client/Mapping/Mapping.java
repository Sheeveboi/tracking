package net.altosheeve.tracking.client.Mapping;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.altosheeve.tracking.client.Core.Rendering;
import net.altosheeve.tracking.client.Shapes.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.text.Text;

import static java.lang.Math.log;

public class Mapping extends Screen {

    public static boolean renderMap = false;

    public static int staticWidth = 0;
    public static int staticHeight = 0;

    public static int pastMouseX = 0;
    public static int pastMouseY = 0;
    public static float mouseDelta = 0;
    public static float mouseDeltaX = 0;
    public static float mouseDeltaY = 0;

    public static float panX = -8f;
    public static float panY = -8f;
    public static float panZ = -31f;
    public static float panSlow = .5f;
    public static float panFast = 3;
    public static float panSpeed = panSlow;
    public static float zoom = 1f;
    public static float zoomPower = .1f;

    public static float mapRotationX = 0;
    public static float mapRotationY = 0;
    public static float mapRotationZ = 0;

    static Shape mapContainer = new MapContainer (panX, panY, panZ, Rendering.Positive);
    static Shape map          = new Shape        (0, 0, 0, Rendering.Positive);

    static {

        mapContainer.transform(Transforms.getHud3dTransform());
        mapLayer.invisible();
        mapLayer.setDrawPriority(1f);

        map.color(0,0,0,0);

        Box   testBox1    = new Box   (0,.1f,0, Rendering.Positive);

        Shape groundPlane = new Shape(0, 0, 0, Rendering.Positive);
        Grid  bottomLayer = new Grid(0, 0, 0, Rendering.Positive);
        Grid  topLayer    = new Grid(0, .0001f, 0, Rendering.Positive);

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

        mapLayer.addShape(mapContainer);

    }

    public Map(Text title) {
        super(title);
    }

    @Override
    public void init() {
        mapLayer.visible();
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

        ScrollableWidget clickRegion = new ScrollableWidget(
                0, 0,
                this.client.getWindow().getWidth(), this.client.getWindow().getHeight(),
                Text.of("")
        ) {
            @Override
            protected int getContentsHeightWithPadding() {
                return 0;
            }

            @Override
            protected double getDeltaYPerScroll() {
                return zoomPower;
            }

            @Override
            protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
                context.fillGradient(this.getX(), this.getY(), this.width, this.height, 0x00000011, 0x00000011);
            }

            @Override
            protected void appendClickableNarrations(NarrationMessageBuilder builder) {

            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                System.out.println(button);
                return false;
            }

            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
                if (!hasShiftDown() && !hasAltDown() && !hasControlDown()) {

                    zoom += (float) (zoomPower * log(zoom + 1) * verticalAmount);

                    System.out.println();

                    mapContainer.scale(zoom, zoom, zoom);

                    System.out.println(zoom);

                }
                return true;
            }
        };

        this.addDrawableChild(clickRegion);

        this.addDrawableChild(xWidget);
        this.addDrawableChild(yWidget);
        this.addDrawableChild(zWidget);

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);

        switch (keyCode) {

            case 87 : //w
                panZ += panSpeed / zoom;
                mapContainer.origin(panX, panY, panZ);
                break;

            case 83 : //s
                panZ -= panSpeed / zoom;
                mapContainer.origin(panX, panY, panZ);
                break;

            case 68 : //d
                panX -= panSpeed / zoom;
                mapContainer.origin(panX, panY, panZ);
                break;

            case 65 : //a
                panX += panSpeed / zoom;
                mapContainer.origin(panX, panY, panZ);
                break;
        }

        return false;
    }

    @Override
    public void close() {
        super.close();
        renderMap = false;
        mapLayer.invisible();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        if (hasShiftDown()) panSpeed = panFast;
        else                panSpeed = panSlow;

    }

}

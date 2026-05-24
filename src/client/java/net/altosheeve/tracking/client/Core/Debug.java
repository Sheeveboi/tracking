package net.altosheeve.tracking.client.Core;

import net.altosheeve.tracking.client.Shapes.Shape;
import net.altosheeve.tracking.client.Waypoints.Waypoint;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.time.Clock;
import java.util.ArrayList;

public class Debug {

    public static int totalShapes = 0;
    public static int waypointsFillShapes = 0;
    public static int waypointsLineShapes = 0;
    public static int totalWaypoints = 0;
    public static int incomingTimestamp = 0;
    public static float avgPacketDelta = 0;

    public static ArrayList<Integer> deltas = new ArrayList<>();

    public static class DebugScreen extends Screen {

        protected DebugScreen(Text title) {
            super(title);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {

            super.render(context, mouseX, mouseY, delta);

            calculateDebug();

            context.drawText(this.textRenderer, "totalShapes: "         + totalShapes,         20, 20, 0xffffffff, true);
            context.drawText(this.textRenderer, "waypointsFillShapes: " + waypointsFillShapes, 20, 40, 0xffffffff, true);
            context.drawText(this.textRenderer, "waypointsLineShapes: " + waypointsLineShapes, 20, 60, 0xffffffff, true);
            context.drawText(this.textRenderer, "totalWaypoints: "      + totalWaypoints,      20, 80, 0xffffffff, true);
            context.drawText(this.textRenderer, "incomingTimestamp: "   + incomingTimestamp,   20, 100,0xffffffff, true);
            context.drawText(this.textRenderer, "avgPacketDelta: "      + avgPacketDelta,      20, 120,0xffffffff, true);

        }

    }

    public static void calculateDebug() {

        waypointsFillShapes = 0;
        waypointsLineShapes = 0;
        totalWaypoints = 0;

        for (Waypoint ignored : Waypoint.waypoints)        totalWaypoints++;
        for (Shape    ignored : Waypoint.fillLayer.shapes) waypointsFillShapes++;
        for (Shape    ignored : Waypoint.lineLayer.shapes) waypointsLineShapes++;
        for (Shape    ignored : Waypoint.lineLayer.shapes) waypointsLineShapes++;

        Clock clock = Clock.systemDefaultZone();
        deltas.add(incomingTimestamp - clock.instant().getNano());

        int sumDeltas = 0;
        for (int delta : deltas) sumDeltas += delta;

        avgPacketDelta = (float) sumDeltas / deltas.size();

        if (deltas.size() > 40) deltas.remove(deltas.getLast());

    }

}

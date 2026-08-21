package net.altosheeve.tracking.client.Waypoints;

import net.altosheeve.tracking.client.Core.Config;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.io.IOException;

public class WaypointConfigScreen extends Screen {

    public WaypointConfigScreen() {
        super(Text.of("Waypoint config screen"));
    }

    public static int tickRate = 1;
    public static double floatrate;
    public static float sliderRange = 20;

    static {
        try {
            floatrate = Config.getPacketRate();
            tickRate = (int) Math.floor(floatrate * sliderRange);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {

        String playersText = "Players Enabled";
        String mobsText    = "Mobs Enabled";
        String blocksText  = "Blocks Enabled";
        String itemsText   = "items Enabled";

        try {
            if (!Config.getWaypointAllowedEntity("players")) playersText = "Players Disabled";
            if (!Config.getWaypointAllowedEntity("mobs"))    mobsText    = "Mobs Disabled";
            if (!Config.getWaypointAllowedEntity("blocks"))  blocksText  = "Blocks Disabled";
            if (!Config.getWaypointAllowedEntity("items"))   itemsText   = "Items Disabled";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ButtonWidget togglePlayers = ButtonWidget.builder(Text.of(playersText), (button) -> {
            try {

                boolean state = !Config.getWaypointAllowedEntity("players");
                Config.updateWaypointAllowedEntities("players", state);

                if (state) button.setMessage(Text.of("Players Enabled"));
                else       button.setMessage(Text.of("Players Disabled"));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).dimensions(20, 20, 150, 20).build();

        ButtonWidget toggleMobs = ButtonWidget.builder(Text.of(mobsText), (button) -> {
            try {

                boolean state = !Config.getWaypointAllowedEntity("mobs");
                Config.updateWaypointAllowedEntities("mobs", state);

                if (state) button.setMessage(Text.of("Mobs Enabled"));
                else       button.setMessage(Text.of("Mobs Disabled"));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).dimensions(20, 40, 150, 20).build();

        ButtonWidget toggleBlocks = ButtonWidget.builder(Text.of(blocksText), (button) -> {
            try {

                boolean state = !Config.getWaypointAllowedEntity("blocks");
                Config.updateWaypointAllowedEntities("blocks", state);

                if (state) button.setMessage(Text.of("Blocks Enabled"));
                else       button.setMessage(Text.of("Blocks Disabled"));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).dimensions(20, 60, 150, 20).build();

        ButtonWidget toggleItems = ButtonWidget.builder(Text.of(itemsText), (button) -> {
            try {

                boolean state = !Config.getWaypointAllowedEntity("items");
                Config.updateWaypointAllowedEntities("items", state);

                if (state) button.setMessage(Text.of("Items Enabled"));
                else       button.setMessage(Text.of("Items Disabled"));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).dimensions(20, 80, 150, 20).build();

        SliderWidget telemetryRateSlider = new SliderWidget(20, 110, 150, 20, Text.of("Telemetry Rate: Every " + tickRate + " ticks"), floatrate) {

            @Override
            protected void updateMessage() {

                floatrate = this.value;

                try {
                    Config.setPacketRate((float) floatrate);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                tickRate = (int) Math.floor(floatrate * sliderRange);
                if (tickRate == 0) tickRate = 1;

                this.message = Text.of("Telemetry Rate: Every " + tickRate + " ticks");

            }

            @Override
            protected void applyValue() {

            }
        };

        addDrawableChild(togglePlayers);
        addDrawableChild(toggleMobs);
        addDrawableChild(toggleBlocks);
        addDrawableChild(toggleItems);

        addDrawableChild(telemetryRateSlider);

    }

}

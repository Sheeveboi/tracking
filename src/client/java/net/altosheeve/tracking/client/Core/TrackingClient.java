package net.altosheeve.tracking.client.Core;

import net.altosheeve.tracking.client.Render.Rendering;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import java.io.IOException;

public class TrackingClient implements ClientModInitializer {

    private static int tick = 0;
    private static boolean init = true;

    @Override
    public void onInitializeClient() {

        /*try { NodeCreation.loadNodes(); }
        catch (IOException e) { throw new RuntimeException(e); }

        Execution.setProgram(TestProgram.getProgram());

        try {
            Relaying.startStream();
        } catch (IOException e) {
            System.out.println("whatT???");
            throw new RuntimeException(e);
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (Hotkeys.keys.isEmpty() && client.options != null) Hotkeys.gatherHotkeys();
            try {
                Keys.handleKeys();
                tick ++;
                tick = tick % 100;
                Navigation.tick = tick;
                Navigation.calculateAllNodes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Execution.execute();
            try {
                Relaying.relayInfo();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (MinecraftClient.getInstance().player != null) Navigation.playerPrev = MinecraftClient.getInstance().player.getPos().toVector3f();
        });*/

        Keys.registerKeys();

        try {
            Relaying.startStream();
        } catch (IOException e) {
            System.out.println("whatT???");
            throw new RuntimeException(e);
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                Relaying.relayInfo();
                Keys.handleKeys();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(Rendering::render3d);

        //HudRenderCallback.EVENT.register(Rendering::render2d);
    }
}

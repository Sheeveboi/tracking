package net.altosheeve.tracking.client.Core;

import net.altosheeve.tracking.client.Kernel.Execution;
import net.altosheeve.tracking.client.Kernel.TerminalKernel;
import net.altosheeve.tracking.client.Navigation.Navigation;
import net.altosheeve.tracking.client.Navigation.NodeCreation;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import java.io.IOException;

public class TrackingClient implements ClientModInitializer {

    private static int tick = 0;
    private static boolean init = true;

    @Override
    public void onInitializeClient() {

        Keys.registerKeys();
        TerminalKernel.loadImplementation();

        try                   { NodeCreation.loadNodes(); }
        catch (IOException e) { throw new RuntimeException(e); }

        try {
            Relaying.startStream();
        } catch (IOException e) {
            System.out.println("whatT???");
            throw new RuntimeException(e);
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            tick ++;
            tick = tick % 100;
            Navigation.tick = tick;

            try {

                Relaying.relayInfo();
                Keys.handleKeys();
                Execution.execute();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(Rendering::render3d);

        //HudRenderCallback.EVENT.register(Rendering::render2d);
    }
}

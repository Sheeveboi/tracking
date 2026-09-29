package net.altosheeve.tracking.client.Navigation.Nodes;

import net.altosheeve.tracking.client.Core.Rendering;
import net.altosheeve.tracking.client.Navigation.Navigation;
import net.altosheeve.tracking.client.Soprano.Terminal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

public class Use extends Node{
    public Use(int x, int y, int z) {
        super(x, y, z);

        this.r = 1;
        this.g = 0;
        this.b = 0;
        this.a = .25f;
    }
    
    @Override
    public void handle() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        Terminal.addLine("interaction distance: " + this.distanceToPlayer());

        if (this.distanceToPlayer() < Navigation.interactionThreshold) {

            Terminal.addLine("entering interactable");

            Camera camera = Rendering.client.gameRenderer.getCamera();

            double dx = camera.getCameraPos().x - Navigation.targetNode.x - .5;
            double dy = camera.getCameraPos().y - Navigation.targetNode.y - .5;
            double dz = camera.getCameraPos().z - Navigation.targetNode.z - .5;

            double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);

            dx /= dist;
            dy /= dist;
            dz /= dist;

            float pitch = (float) Math.asin(-dy);
            float yaw = (float) Math.atan2(dz, dx);

            pitch = (float) (pitch * 180.0 / Math.PI);
            yaw = (float) (yaw * 180.0 / Math.PI) + 90;

            player.setPitch(-pitch);
            player.setYaw(yaw);

            client.options.useKey.setPressed(true);

            this.finished = true;

        } else {
            client.options.useKey.setPressed(false);
        }

        super.handle();
        
    }
}

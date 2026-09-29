package net.altosheeve.tracking.client.Navigation.Nodes;

import net.altosheeve.tracking.client.Navigation.Navigation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class Lode extends Node {
    public Lode(int x, int y, int z) {
        super(x, y, z);

        this.r = .5f;
        this.g = .5f;
        this.b = 0;
    }

    @Override
    public void handle() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        if (Navigation.targetNode instanceof Lode) super.handle();

        else {

            player.setPitch(90);

            if (Navigation.targetNode.y > Navigation.currentNode.y) client.options.useKey.setPressed(true);
            else client.options.attackKey.setPressed(true);

        }

    }
}

package net.altosheeve.tracking.client.Navigation.Nodes;

import net.altosheeve.tracking.client.Navigation.Navigation;
import net.altosheeve.tracking.client.Soprano.Terminal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import org.joml.Vector3f;

import java.util.Objects;

import static java.lang.Math.abs;
import static net.altosheeve.tracking.client.Navigation.Navigation.stareAtTarget;

public class Iceroad extends Node {
    public Iceroad(int x, int y, int z) {
        super(x, y, z);

        this.r = .5f;
        this.g = .5f;
        this.b = 1f;
        this.a = .3f;
    }
    
    @Override
    public void handle() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        if (Navigation.tick % 2 == 0) client.options.jumpKey.setPressed(true);
        else client.options.jumpKey.setPressed(false);
        client.options.sprintKey.setPressed(true);

        stareAtTarget(player);

        Vector3f idealVector = new Vector3f(Navigation.targetNode.x, 0, Navigation.targetNode.z).sub(Navigation.currentNode.x, 0, Navigation.targetNode.z);
        Vector3f idealNormal = new Vector3f(idealVector.z, 0, -idealVector.x);

        Vector3f currentNodeVector = new Vector3f((float) player.getX(), 0, (float) player.getZ()).sub(Navigation.targetNode.x, 0, Navigation.targetNode.z);

        currentNodeVector.normalize();
        idealVector.normalize();
        idealNormal.normalize();

        double innacuracy = idealVector.dot(currentNodeVector);
        double deviation = idealNormal.dot(currentNodeVector);

        Terminal.addLine("ideal vector: " + idealVector);
        Terminal.addLine("ideal normal: " + idealNormal);
        Terminal.addLine("current vector: " + currentNodeVector);

        Terminal.addLine("innaccuracy: " + innacuracy);
        Terminal.addLine("deviation: " + deviation);

        if (abs(innacuracy) < .9) {

            if (deviation < 0) client.options.leftKey.setPressed(true);
            else client.options.rightKey.setPressed(true);

        }

        if (player.getHungerManager().getFoodLevel() < 17) {
            if (!Objects.equals(player.getInventory().getStack(player.getInventory().getSelectedSlot()).getItemName().getString(), "Baked Potato") && !eating) {
                for (int slot = 0; slot < 36; slot++) {
                    if (Objects.equals(player.getInventory().getStack(slot).getItemName().getString(), "Baked Potato")) {

                        player.playerScreenHandler.enableSyncing();
                        player.playerScreenHandler.syncState();

                        player.setPitch(0);

                        client.interactionManager.clickSlot(
                                player.playerScreenHandler.syncId,
                                slot,
                                player.getInventory().getSelectedSlot(),
                                SlotActionType.SWAP,
                                player
                        );

                        player.getInventory().setSelectedSlot(0);

                        break;

                    }
                }
            }

            client.options.useKey.setPressed(true);

            eating = true;

        }

        else eating = false;
        
    }

    @Override
    public void error() {

        Terminal.addLine("running error handling");

        erroring = true;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        MinecraftClient.getInstance().options.forwardKey.setPressed(true);
        stareAtTarget(player);

        Vector3f idealVector = new Vector3f(Navigation.targetNode.x, (float) player.getY(), Navigation.targetNode.z).sub(Navigation.currentNode.x, (float) player.getY(), Navigation.currentNode.z);

        double innacuracy = idealVector.dot(player.getVelocity().toVector3f());

        Terminal.addLine("innaccuracy: " + innacuracy);
        Terminal.addLine("velocityTime: " + errorSteps);

        //attempt door unstick
        //TODO: swap to non-placeable in the event of a door unstick attempt
        if (errorSteps < 5) {

            player.setPitch(60);
            client.options.useKey.setPressed(true);

        }

        if (errorSteps > 5 && errorSteps < 20) {

            //System.out.println("jumping, " + errorSteps);

            client.options.jumpKey.setPressed(true);

            this.handle();

        }

        //attempt sneak
        if (errorSteps >= 20 && errorSteps < 60) {

            player.setPitch(0);
            client.options.sneakKey.setPressed(true);

        }

        else client.options.jumpKey.setPressed(false);

        //TODO: use a kernel for sequencing action, that's what its there for. And make it time based

        //attempt strafe
        //TODO: measure forward velocity and general velocity to detect futile strafing
        if (errorSteps > 60 && errorSteps < 160) {

            System.out.println("attempting strafe, " + Navigation.deviationDirection);

            client.options.jumpKey.setPressed(true);

            if (Navigation.deviationDirection) client.options.rightKey.setPressed(true);
            else                    client.options.leftKey.setPressed(true);
        }

        if (errorSteps >= 160 && errorSteps < 260) {

            client.options.jumpKey.setPressed(true);

            if (Navigation.deviationDirection) client.options.leftKey.setPressed(true);
            else                    client.options.rightKey.setPressed(true);

        }

        if (errorSteps >= 260) errorSteps = 0;

        errorSteps++;

    }
}

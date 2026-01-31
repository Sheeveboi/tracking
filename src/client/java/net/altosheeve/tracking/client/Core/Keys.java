package net.altosheeve.tracking.client.Core;

import net.altosheeve.tracking.client.Render.Map;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

public class Keys {

    public static KeyBinding nodeScreen;
    public static KeyBinding connectNode;
    public static KeyBinding selectNode;
    public static KeyBinding enableExec;
    public static KeyBinding loadTestProgram;
    public static KeyBinding mapKey;
    public static KeyBinding outputDistancesKey;

    public static void registerKeys() {
        nodeScreen = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Add / Edit node",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_LEFT_BRACKET,
                        "Soprano"
                )
        );

        connectNode = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Connect Nodes",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_BRACKET,
                        "Soprano"
                )
        );

        selectNode = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Select Node",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_BACKSLASH,
                        "Soprano"
                )
        );
        enableExec = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Enable Execution",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_APOSTROPHE,
                        "Soprano"
                )
        );

        mapKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Map Key",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_SEMICOLON,
                        "Soprano"
                )
        );

        loadTestProgram = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Load Test Program",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_EQUAL,
                        "Soprano"
                )
        );

        outputDistancesKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Output Node Distances",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_PERIOD,
                        "Soprano"
                )
        );
    }

    public static void handleKeys() throws IOException {

        while(mapKey.wasPressed()) MinecraftClient.getInstance().setScreen(new Map(Text.of("Civ Map")));

        /*while(mapKey.wasPressed()) MinecraftClient.getInstance().setScreen(new CivMap(Text.of("Civ Map")));
        while(nodeScreen.wasPressed()) MinecraftClient.getInstance().setScreen(new EditNodeScreen(Text.of("Civ Map")));

        while(connectNode.wasPressed()) NodeCreation.connectNode();
        while(selectNode.wasPressed()) NodeCreation.selectNode();
        while(enableExec.wasPressed()) Execution.toggle();
        while(loadTestProgram.wasPressed()) Execution.setProgram(TestProgram.getProgram());
        while(outputDistancesKey.wasPressed()) {
            for (Node node : Navigation.nodes) {
                node.calculateDistances();
                for (int distanceKey : node.distanceMap.keySet()) {
                    System.out.println(node.tag + "'s distance to " + Navigation.nodes.get(distanceKey).tag + ": " + node.distanceMap.get(distanceKey));
                }
            }
        }
        while(testKey.wasPressed()) {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;
            assert player != null;

            for (int slot = 0; slot < 36; slot++) {
                if (Objects.equals(player.getInventory().getStack(slot).getItemName().getString(), "Baked Potato")) {
                    player.getInventory().swapSlotWithHotbar(slot);
                }
            }
        }*/
    }

}

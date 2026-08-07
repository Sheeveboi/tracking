package net.altosheeve.tracking.client.Core;

import net.altosheeve.tracking.client.Soprano.Terminal;
import net.altosheeve.tracking.client.Navigation.EditNodeScreen;
import net.altosheeve.tracking.client.Navigation.NodeCreation;
import net.altosheeve.tracking.client.Waypoints.WaypointConfigScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

public class Keys {

    public static KeyBinding nodeScreen;
    public static KeyBinding connectNode;
    public static KeyBinding selectNode;
//    public static KeyBinding enableExec;
//    public static KeyBinding loadTestProgram;
//    public static KeyBinding mapKey;
//    public static KeyBinding outputDistancesKey;
//    public static KeyBinding openTerminal;

    public static KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("net.altosheeve", "tracking"));

    public static KeyBinding waypointScreen;
    public static KeyBinding debugScreen;
    public static KeyBinding placeNextNode;

    public static void registerKeys() {

        waypointScreen = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Open waypoint config",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_LEFT_BRACKET,
                        category
                )
        );

        debugScreen = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Open debug",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_RIGHT_BRACKET,
                        category
                )
        );

        nodeScreen = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Add / Edit node",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_COMMA,
                        category
                )
        );

        connectNode = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Connect Node",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_SEMICOLON,
                        category
                )
        );

        selectNode = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Select Node",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_APOSTROPHE,
                        category
                )
        );

        placeNextNode = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Place Next Node",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_PERIOD,
                        category
                )
        );
    }

    public static void handleKeys() throws IOException {

        while (waypointScreen.wasPressed()) MinecraftClient.getInstance().setScreen(new WaypointConfigScreen());
        while (debugScreen.wasPressed()) MinecraftClient.getInstance().setScreen(new Debug.DebugScreen(Text.of("debug")));

//        while(mapKey.wasPressed()) MinecraftClient.getInstance().setScreen(new Mapping(Text.of("Civ Mapping")));
//
        while(connectNode.wasPressed()) NodeCreation.connectNode();
        while(selectNode.wasPressed())  NodeCreation.selectNode();
//
        while(nodeScreen.wasPressed()) MinecraftClient.getInstance().setScreen(new EditNodeScreen(Text.of("Node Screen")));

        while(placeNextNode.wasPressed()) NodeCreation.addNextNode();
//
//        while(openTerminal.wasPressed()) MinecraftClient.getInstance().setScreen(new Terminal(Text.of("Terminal Screen")));

//        while(connectNode.wasPressed()) NodeCreation.connectNode();
//        while(selectNode.wasPressed()) NodeCreation.selectNode();
//        while(enableExec.wasPressed()) Execution.toggle();
//        while(loadTestProgram.wasPressed()) Execution.setProgram(TestProgram.getProgram());
//        while(outputDistancesKey.wasPressed()) {
//            for (Node node : Navigation.nodes) {
//                node.calculateDistances();
//                for (int distanceKey : node.distanceMap.keySet()) {
//                    System.out.println(node.tag + "'s distance to " + Navigation.nodes.get(distanceKey).tag + ": " + node.distanceMap.get(distanceKey));
//                }
//            }
//        }
//        while(testKey.wasPressed()) {
//            MinecraftClient client = MinecraftClient.getInstance();
//            ClientPlayerEntity player = client.player;
//            assert player != null;
//
//            for (int slot = 0; slot < 36; slot++) {
//                if (Objects.equals(player.getInventory().getStack(slot).getItemName().getString(), "Baked Potato")) {
//                    player.getInventory().swapSlotWithHotbar(slot);
//                }
//            }
//        }
    }

}

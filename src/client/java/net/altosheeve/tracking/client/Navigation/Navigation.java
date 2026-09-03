package net.altosheeve.tracking.client.Navigation;

import net.altosheeve.tracking.client.Core.Rendering;
import net.altosheeve.tracking.client.Shapes.Layer;
import net.altosheeve.tracking.client.Soprano.Terminal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.*;

import static java.lang.Math.abs;

public class Navigation {

    public static Node currentNode;
    public static Node targetNode;

    public static double velocityThreshold;
    public static int errorSteps = 0;
    public static int handleSteps = 0;
    private static boolean eating = false;
    private static boolean erroring = false;
    private static boolean interacted = false;
    public static double interactionThreshold;
    private static boolean deviationDirection = false;


    public static int tick;
    public static int deepThreshold = 3;
    public static int maxDeepSearchDistance = 5;
    public static Vector3f playerPrev = new Vector3f();
    public static Handler handler;
    public static Handler lastHandler;

    public static ArrayList<Node> nodes = new ArrayList<>();
    public static Layer fillLayer = new Layer("Navigation Node Boxes", Layer.Method.FILL_UNOCCLUDED);
    public static Layer lineLayer = new Layer("Navigation Node Connections", Layer.Method.LINE_UNOCCLUDED);

    public interface Handler {
        void cb();
    }

    public static void resetControls() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        client.options.useKey.setPressed(false);
        client.options.jumpKey.setPressed(false);
        client.options.leftKey.setPressed(false);
        client.options.rightKey.setPressed(false);
        client.options.forwardKey.setPressed(false);
        client.options.backKey.setPressed(false);
        client.options.attackKey.setPressed(false);
        client.options.sprintKey.setPressed(false);
        client.options.sneakKey.setPressed(false);

    }

    public static void addNode(Node node) {
        nodes.add(node);
        fillLayer.addShape(node);
        lineLayer.addShape(node);
    }

    public static void removeNode(Node node) {
        nodes.remove(node);
        fillLayer.removeShape(node);
        lineLayer.removeShape(node);
    }

    public static void clearNodes() {

        nodes.clear();
        fillLayer.shapes.clear();
        lineLayer.shapes.clear();

    }

    public static void detectError() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        double velocity = new Vec3d(player.getVelocity().x, 0, player.getVelocity().z).length();

        Vector3f currentNodeVector = new Vector3f((float) player.getX(), 0, (float) player.getZ()).sub(targetNode.x, 0, targetNode.z);
        Vector3f idealVector = new Vector3f(currentNode.x, 0, currentNode.z).sub(targetNode.x, 0, targetNode.z);

        deviationDirection = idealVector.dot(currentNodeVector) < 0;

        if ((velocity < velocityThreshold && handleSteps > 20)) {
            lastHandler = handler;
            handler = Navigation::errorHandler;
        }

        else {
            errorSteps = 0;
            handleSteps ++;
        }

    }

    //TODO: make node types and their associated handlers object oriented

    public static void errorHandler() {

        Terminal.addLine("running error handling");

        erroring = true;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        Vector3f orientation     = player.getFacing().getUnitVector().normalize();
        Vector3f currentVelocity = new Vec3d(player.getVelocity().x, 0, player.getVelocity().z).toVector3f();

        double forwardVelocity = orientation.dot(currentVelocity);

        Terminal.addLine("forward velocity: " + forwardVelocity);

        if (currentVelocity.length() > velocityThreshold) {

            Terminal.addLine("error handled");

            handler = lastHandler;
            errorSteps = 0;
            erroring = false;

            Terminal.addLine(handler.toString());

            return;

        }

        if (errorSteps < 20) {

            client.options.jumpKey.setPressed(true);
            errorSteps++;

            lastHandler.cb();

            return;
        }


        Vector3f idealVector = new Vector3f(targetNode.x, (float) player.getY(), targetNode.z).sub(currentNode.x, (float) player.getY(), currentNode.z);
        Vector3f idealNormal = new Vector3f(idealVector.z, (float) player.getY(), -idealVector.x);

        float innacuracy = idealVector.dot(player.getVelocity().toVector3f());

        Terminal.addLine("innaccuracy: " + innacuracy);
        Terminal.addLine("velocityTime: " + errorSteps);

        //attempt strafe
        //TODO: measure forward velocity and general velocity to detect futile strafing
        if (errorSteps < 100) {
            if (deviationDirection) client.options.rightKey.setPressed(true);
            else                    client.options.leftKey.setPressed(true);
        }

        if (errorSteps > 100 && errorSteps < 200) {
            if (!deviationDirection) client.options.rightKey.setPressed(true);
            else                     client.options.leftKey.setPressed(true);
        }

        //attempt door unstick
        if (errorSteps > 200 && errorSteps < 220) {

            player.setPitch(60);
            client.options.useKey.setPressed(true);

        }

        //attempt sneak
        if (errorSteps > 220 && errorSteps < 250) {

            player.setPitch(0);
            client.options.sneakKey.setPressed(true);

        }

        if (errorSteps > 200) client.options.jumpKey.setPressed(false);
        if (errorSteps > 250) errorSteps = 0;

        errorSteps++;

    }

    public static void basicWalkHandler() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        double dx = player.getX() - targetNode.x - .5;
        double dy = player.getY() - targetNode.y;
        double dz = player.getZ() - targetNode.z - .5;

        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);

        dx /= dist;
        dy /= dist;
        dz /= dist;

        float pitch = (float) Math.asin(-dy);
        float yaw = (float) Math.atan2(dz, dx);

        pitch = (float) (pitch * 180.0 / Math.PI);
        yaw = (float) (yaw * 180.0 / Math.PI);

        yaw += 90;

        player.setPitch(-pitch);
        player.setYaw(yaw);

    }

    public static void iceroadHandler() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        if (tick % 2 == 0) client.options.jumpKey.setPressed(true);
        else client.options.jumpKey.setPressed(false);
        client.options.sprintKey.setPressed(true);

        Vector3f idealVector = new Vector3f(targetNode.x, 0, targetNode.z).sub(currentNode.x, 0, targetNode.z);
        Vector3f idealNormal = new Vector3f(idealVector.z, 0, -idealVector.x);

        Vector3f currentNodeVector = new Vector3f((float) player.getX(), 0, (float) player.getZ()).sub(targetNode.x, 0, targetNode.z);

        currentNodeVector.normalize();
        idealVector.normalize();
        idealNormal.normalize();

        float innacuracy = idealVector.dot(currentNodeVector);
        float deviation = idealNormal.dot(currentNodeVector);

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

    public static void interactionHandler() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        Terminal.addLine("interaction distance: " + player.getLastRenderPos().distanceTo(new Vec3d(targetNode.x + .5, targetNode.y + .5, targetNode.z + .5)));

        if (player.getLastRenderPos().distanceTo(new Vec3d(targetNode.x + .5, targetNode.y + .5, targetNode.z + .5)) < interactionThreshold && !interacted) {

            Terminal.addLine("entering interactable");

            Camera camera = Rendering.client.gameRenderer.getCamera();

            double dx = camera.getCameraPos().x - targetNode.x - .5;
            double dy = camera.getCameraPos().y - targetNode.y - .5;
            double dz = camera.getCameraPos().z - targetNode.z - .5;

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

            interacted = true;

        } else {
            interacted = false;
            client.options.useKey.setPressed(false);
        }

        basicWalkHandler();
    }

    public static void lodestoneHandler() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        if (targetNode.type != Node.NodeType.LODE) basicWalkHandler();

        else {

            player.setPitch(90);

            if (targetNode.y > currentNode.y) client.options.useKey.setPressed(true);
            else client.options.attackKey.setPressed(true);

        }

    }

    public static void calculateAllNodes() { //potentially expensive with larger systems of nodes. only perform when necessary
        for (Node node : nodes) node.calculateDistances();
    }

    public static ArrayList<Integer> generatePathingItinerary(String nodeTag) {

        ArrayList<Integer> closestApproachOut = new ArrayList<>();
        ArrayList<Integer> lastClosestApproachOutSolution = new ArrayList<>();
        int loopdetection = 0;

        Optional<Node> testing = nodes.stream().filter(node -> Objects.equals(nodeTag, node.tag)).findFirst();
        if (testing.isEmpty()) return null;

        Node targetNode = testing.get();
        Node testNode = currentNode;

        ArrayList<Integer> searched = new ArrayList<>();

        class key implements Comparator {
            @Override
            public int compare(Object o1, Object o2) {

                Node node1 = nodes.get((Integer) o1);
                Node node2 = nodes.get((Integer) o2);

                int node1Dist = 99999;
                int node2Dist = 99999;

                if (node1.distanceMap.containsKey(nodes.indexOf(targetNode))) node1Dist = node1.distanceMap.get(nodes.indexOf(targetNode));
                if (node2.distanceMap.containsKey(nodes.indexOf(targetNode))) node2Dist = node2.distanceMap.get(nodes.indexOf(targetNode));

                if (node1Dist > node2Dist) return 1;
                else if (node1Dist == node2Dist) return 0;
                return -1;
            }
        }

        //gather based on closest approach
        while (true) {

            ArrayList<Integer> sorted = testNode.connections;
            sorted.sort(new key());

            boolean deadEnd = true;

            if (testNode.connections.contains(nodes.indexOf(targetNode))) {
                closestApproachOut.add(nodes.indexOf(targetNode));
                return closestApproachOut;
            }

            if (!searched.containsAll(testNode.connections)) {
                for (int i : sorted) {

                    Node instance = nodes.get(i);

                    if (!searched.contains(i)) {

                        deadEnd = false;

                        searched.add(i);
                        closestApproachOut.add(i);

                        testNode = instance;

                        //Terminal.addLine("distance map: " + testNode.distanceMap);

                        break;

                    }

                }
            }

            else if (closestApproachOut.isEmpty()) return closestApproachOut;

            //if all the nodes in the testNode's connections have been tested already, jump back one node
            if (deadEnd) {

                if (closestApproachOut.size() != 1) closestApproachOut.removeLast();

                testNode = nodes.get(closestApproachOut.getLast());

            }

            if (lastClosestApproachOutSolution == closestApproachOut) loopdetection++;
            if (loopdetection > 100) return closestApproachOut;

            lastClosestApproachOutSolution = closestApproachOut;

            Terminal.addLine(String.valueOf(closestApproachOut));

        }
    }

}

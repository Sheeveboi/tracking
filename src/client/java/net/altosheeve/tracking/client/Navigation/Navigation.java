package net.altosheeve.tracking.client.Navigation;

import net.altosheeve.tracking.client.Core.Rendering;
import net.altosheeve.tracking.client.Navigation.Nodes.Node;
import net.altosheeve.tracking.client.Shapes.Layer;
import net.altosheeve.tracking.client.Soprano.Terminal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.abs;

public class Navigation {

    public static Node currentNode;
    public static Node targetNode;

    public static double velocityThreshold;
    public static double interactionThreshold;

    public static int errorSteps = 0;
    public static int handleSteps = 0;

    private static boolean eating = false;
    private static boolean erroring = false;
    private static boolean interacted = false;
    public static boolean deviationDirection = false;

    public static Vector3f idealVector     = new Vector3f();
    public static Vector3f idealNormal     = new Vector3f();
    public static Vector3f orientation     = new Vector3f();
    public static Vector3f currentVelocity = new Vector3f();
    public static Vector3f normalVelocity  = new Vector3f();
    public static Vector3f targetVector    = new Vector3f();


    public static double currentXPos = 0;
    public static double currentZPos = 0;
    public static double currentXVelocity = 0;
    public static double currentZVelocity = 0;
    public static double lastXPos = 0;
    public static double lastZPos = 0;

    public static double forwardVelocity;
    public static double targetVelocity;

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

    public static Node getNodeByUUID(String UUID) {

        for (Node testNode : Navigation.nodes)
            if (Objects.equals(testNode.UUID, UUID))
                return testNode;

        return null;

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

        try {

            if (!node.connections.isEmpty())
                for (String connection : node.connections)
                    for (Node testNode : nodes)
                        if (Objects.equals(testNode.UUID, connection) && !testNode.connections.contains(node.UUID))
                            testNode.connections.add(node.UUID);

        } catch (Exception ignored) {}

        try { NodeCreation.dumpNodes(); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    public static void stareAtTarget(ClientPlayerEntity player) {

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

    public static void removeNode(Node target) {

        for (Node node : Navigation.nodes) {

            if (node != target)
                node.connections.remove(target.UUID);

            node.distanceMap.remove(target.UUID);

            for (String connection : node.connections)
                for (Node testNode : nodes)
                    if (Objects.equals(testNode.UUID, connection))
                        testNode.connections.remove(node.UUID);

        }

        nodes.remove(target);
        fillLayer.removeShape(target);
        lineLayer.removeShape(target);

        try { NodeCreation.dumpNodes(); }
        catch (IOException e) { throw new RuntimeException(e); }

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

        currentXPos = player.getX();
        currentZPos = player.getX();

        idealVector     = new Vector3f(currentNode.x, 0, currentNode.z).sub(targetNode.x, 0, targetNode.z);
        idealNormal     = new Vector3f(-idealVector.z, 0, idealVector.x);
        orientation     = player.getFacing().getUnitVector().normalize();
        targetVector    = new Vector3f(targetNode.x, 0, targetNode.z).sub((float) player.getX(), 0, (float) player.getZ()).normalize();

        //TODO: currentVelocity NaNs out when getting stopped in corners. Probably a good idea to abandon vector3f all together
        currentXVelocity = Math.abs(currentXPos - lastXPos);
        currentZVelocity = Math.abs(currentZPos - lastZPos);

        currentVelocity = new Vector3f((float) player.getVelocity().x, 0, (float) player.getVelocity().z);

        normalVelocity  = currentVelocity.normalize();

        lastXPos = currentXPos;
        lastZPos = currentZPos;

        forwardVelocity = orientation.dot(normalVelocity);
        targetVelocity  = Math.abs(targetVector.dot(normalVelocity) * forwardVelocity);

        Terminal.addLine("target velocity: " + targetVelocity);
        Terminal.addLine("forwardVelocity: " + forwardVelocity);
        Terminal.addLine("regularVelocity: " + currentVelocity);
        Terminal.addLine("Nanning?: " + Double.isNaN(forwardVelocity));
        Terminal.addLine("Nanning?: " + Double.isNaN(targetVelocity));
        Terminal.addLine("threshold: " + velocityThreshold);
        Terminal.addLine("erroring: " + erroring);
        Terminal.addLine("handleSteps: " + handleSteps);
        Terminal.addLine("currentXVelocity: " + currentXVelocity);
        Terminal.addLine("currentZVelocity: " + currentZVelocity);

        if (targetVelocity < velocityThreshold || Double.isNaN(targetVelocity)) {

            Terminal.addLine("bot stalled. handling error");

            if (!targetNode.erroring) deviationDirection = idealNormal.dot(targetVector) < 0;

            targetNode.erroring = true;

            erroring = true;

        }

        if (targetVelocity >= velocityThreshold) {

            if (erroring) handler = lastHandler;

            targetNode.erroring = false;
            targetNode.errorSteps = 0;

            handleSteps ++;
            errorSteps = 0;
            erroring = false;
        }

    }

    //TODO: make node types and their associated handlers object oriented

    public static void errorHandler() {

        Terminal.addLine("running error handling");

        erroring = true;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        Vector3f idealVector = new Vector3f(targetNode.x, (float) player.getY(), targetNode.z).sub(currentNode.x, (float) player.getY(), currentNode.z);

        double innacuracy = idealVector.dot(player.getVelocity().toVector3f());

        Terminal.addLine("innaccuracy: " + innacuracy);
        Terminal.addLine("velocityTime: " + errorSteps);

        //attempt door unstick
        //TODO: swap to non-placeable in the event of a door unstick attempt
        if (errorSteps < 5) {

            player.setPitch(60);
            client.options.useKey.setPressed(true);

        }

        if (errorSteps < 20) {

            //System.out.println("jumping, " + errorSteps);

            client.options.jumpKey.setPressed(true);

            lastHandler.cb();

        }

        stareAtTarget(player);

        //TODO: use a kernel for sequencing action, that's what its there for. And make it time based

        //attempt strafe
        //TODO: measure forward velocity and general velocity to detect futile strafing
        if (errorSteps >= 20 && errorSteps < 120) {

            System.out.println("attempting strafe, " + deviationDirection);

            client.options.jumpKey.setPressed(true);

            if (deviationDirection) client.options.rightKey.setPressed(true);
            else                    client.options.leftKey.setPressed(true);
        }

        if (errorSteps >= 120 && errorSteps < 220) {

            client.options.jumpKey.setPressed(true);

            if (deviationDirection) client.options.leftKey.setPressed(true);
            else                    client.options.rightKey.setPressed(true);

        }

        //attempt sneak
        if (errorSteps >= 240 && errorSteps < 260) {

            player.setPitch(0);
            client.options.sneakKey.setPressed(true);

        }

        if (errorSteps >= 220) client.options.jumpKey.setPressed(false);
        if (errorSteps >= 260) errorSteps = 0;

        errorSteps++;

    }

    public static void basicWalkHandler() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        stareAtTarget(player);

    }

    public static void iceroadHandler() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        if (tick % 2 == 0) client.options.jumpKey.setPressed(true);
        else client.options.jumpKey.setPressed(false);
        client.options.sprintKey.setPressed(true);

        stareAtTarget(player);

        Vector3f idealVector = new Vector3f(targetNode.x, 0, targetNode.z).sub(currentNode.x, 0, targetNode.z);
        Vector3f idealNormal = new Vector3f(idealVector.z, 0, -idealVector.x);

        Vector3f currentNodeVector = new Vector3f((float) player.getX(), 0, (float) player.getZ()).sub(targetNode.x, 0, targetNode.z);

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

    public static ArrayList<String> generatePathingItinerary(String nodeTag) {

        ArrayList<String> closestApproachOut = new ArrayList<>();
        ArrayList<String> lastClosestApproachOutSolution = new ArrayList<>();
        int loopdetection = 0;

        Optional<Node> testing = nodes.stream().filter(node -> Objects.equals(nodeTag, node.tag)).findFirst();
        if (testing.isEmpty()) return null;

        Node targetNode = testing.get();
        Node testNode = currentNode;

        ArrayList<String> searched = new ArrayList<>();

        class key implements Comparator {
            @Override
            public int compare(Object o1, Object o2) {

                Node node1 = Navigation.getNodeByUUID((String) o1);
                Node node2 = Navigation.getNodeByUUID((String) o2);

                int node1Dist = 99999;
                int node2Dist = 99999;

                if (node1.distanceMap.containsKey(targetNode.UUID)) node1Dist = node1.distanceMap.get(targetNode.UUID);
                if (node2.distanceMap.containsKey(targetNode.UUID)) node2Dist = node2.distanceMap.get(targetNode.UUID);

                if (node1Dist > node2Dist) return 1;
                else if (node1Dist == node2Dist) return 0;
                return -1;
            }
        }

        //gather based on closest approach
        while (true) {

            ArrayList<String> sorted = testNode.connections;
            sorted.sort(new key());

            boolean deadEnd = true;

            if (testNode.connections.contains(targetNode.UUID)) {
                closestApproachOut.add(targetNode.UUID);
                return closestApproachOut;
            }

            if (!searched.containsAll(testNode.connections)) {
                for (String uuid : sorted) {

                    Node instance = getNodeByUUID(uuid);

                    if (!searched.contains(uuid)) {

                        deadEnd = false;

                        searched.add(uuid);
                        closestApproachOut.add(uuid);

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

                testNode = getNodeByUUID(closestApproachOut.getLast());

            }

            if (lastClosestApproachOutSolution == closestApproachOut) loopdetection++;
            if (loopdetection > 100) return closestApproachOut;

            lastClosestApproachOutSolution = closestApproachOut;

            Terminal.addLine(String.valueOf(closestApproachOut));

        }
    }

}

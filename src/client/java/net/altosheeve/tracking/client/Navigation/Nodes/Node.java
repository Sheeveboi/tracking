package net.altosheeve.tracking.client.Navigation.Nodes;

import net.altosheeve.tracking.client.Core.Rendering;
import net.altosheeve.tracking.client.Navigation.Navigation;
import net.altosheeve.tracking.client.Shapes.Box;
import net.altosheeve.tracking.client.Shapes.Transforms;
import net.altosheeve.tracking.client.Soprano.Terminal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.json.JSONArray;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import static net.altosheeve.tracking.client.Navigation.Navigation.stareAtTarget;

public class Node extends Box {

    public enum NodeType {
        NORMAL,
        ICEROAD,
        USE,
        LODE
    }

    public String tag;
    public ArrayList<String> connections;
    public NodeType type;
    public Map<String, Integer> distanceMap = new HashMap<>();

    public float velocityThreshold = 0.09f; //enable recovery if bot speed is below this threshold
    public float innaccuracyThreshold = 0.9f; //enable recovery if bot vector in relation to target node deviates from ideal currentNode to targetNode vector
    public float tolerance = 0.7f; //completion distance to targetNode
    public boolean sprint = false; //run fast
    public float lookx = 0;
    public float looky = 0;
    public float lookz = 0;
    public float offsetx = 0;
    public float offsety = 0;
    public float offsetz = 0;
    public int waitTimeIn = 0; //how many ticks to wait before pathing to this node
    public int waitTimeOut = 0; //how many ticks to wait after pathing to this node

    public static Node mutate(Node node) {

        Node newNode;

        switch (node.type) {

            case ICEROAD -> newNode = new Iceroad((int) node.x, (int) node.y, (int) node.z);
            case USE     -> newNode = new Use((int) node.x, (int) node.y, (int) node.z);
            case LODE    -> newNode = new Lode((int) node.x, (int) node.y, (int) node.z);

            default -> newNode = new Walk((int) node.x, (int) node.y, (int) node.z);

        }

        Field[] allFields = Node.class.getDeclaredFields();
        for (Field field : allFields) newNode.setField(field.getName(), node.getField(field.getName()));

        return newNode;

    }

    public Node(int x, int y, int z) {
        super(x, y, z, "");

        String uuid = String.valueOf(java.util.UUID.randomUUID());
        this.UUID = uuid;

        System.out.println("created node: " + this.UUID);
        this.connections = new ArrayList<>();
    }

    public Node(int x, int y, int z, NodeType type) {
        super(x, y, z, "");

        String uuid = String.valueOf(java.util.UUID.randomUUID());
        this.UUID = uuid;

        System.out.println("created node: " + this.UUID);
        this.type = type;
        this.connections = new ArrayList<>();
    }

    public Node(int x, int y, int z, NodeType type, String tag) {
        super(x, y, z, "");

        String uuid = String.valueOf(java.util.UUID.randomUUID());
        this.UUID = uuid;

        System.out.println("created node: " + this.UUID);
        this.type = type;
        this.tag = tag;
        this.connections = new ArrayList<>();
    }

    public Node(int x, int y, int z, NodeType type, String tag, boolean sprint, float velocityThreshold, int waitTimeIn, int waitTimeOut) {
        super(x, y, z, "");

        String uuid = String.valueOf(java.util.UUID.randomUUID());
        this.UUID = uuid;

        System.out.println("created node: " + this.UUID);
        this.type = type;
        this.tag = tag;
        this.connections = new ArrayList<>();
        this.velocityThreshold = velocityThreshold;
        this.sprint = sprint;
        this.waitTimeIn = waitTimeIn;
        this.waitTimeOut = waitTimeOut;
    }

    public Node(int x, int y, int z, NodeType type, String tag, ArrayList<String> connections) {
        super(x, y, z, "");

        String uuid = String.valueOf(java.util.UUID.randomUUID());
        this.UUID = uuid;

        System.out.println("created node: " + this.UUID);
        this.type = type;
        this.tag = tag;
        this.connections = connections;
    }

    //From Alex Burdusel on StackOverflow (Modified to not be static)
    public boolean setField(String fieldName, Object fieldValue) {

        Field field;
        Class superClass = this.getClass().getSuperclass();

        if (fieldValue instanceof BigDecimal) fieldValue = ((BigDecimal) fieldValue).floatValue();
        if (Objects.equals(fieldName, "type") && fieldValue instanceof String) {
            System.out.println("yay");
            this.type = NodeType.valueOf((String) fieldValue);
            return true;
        }
        if (fieldValue instanceof JSONArray) fieldValue = new ArrayList<>(((JSONArray) fieldValue).toList());

        try {
            field = this.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            field = null;
        }

        while (field == null && superClass != null) {

            try {
                field = superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                superClass = superClass.getSuperclass();
            }

        }

        if (field == null) {
            return false;
        }

        field.setAccessible(true);

        try {
            field.set(this, fieldValue);
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }

    }

    public Object getField(String fieldName) {

        Object value = null;

        try {

            Field field = Node.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            value = field.get(this);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return value;

    }

    public void calculateDistances() {

        Map<String, ArrayList<Integer>> potentialDistances = new HashMap<>();

        //gather distances of all neighbors
        for (String connection : this.connections) {

            Node neighbor = Navigation.getNodeByUUID(connection);

            if (neighbor != null) {

                //set distances to self to one on parent node
                neighbor.distanceMap.put(this.UUID, 1);
                this.distanceMap.put(neighbor.UUID, 1);

                //copy neighbors distance map
                for (String distanceKey : neighbor.distanceMap.keySet()) {

                    if (!Objects.equals(distanceKey, this.UUID)) {

                        int distance = neighbor.distanceMap.get(distanceKey);

                        if (!potentialDistances.containsKey(distanceKey))
                            potentialDistances.put(distanceKey, new ArrayList<>());

                        potentialDistances.get(distanceKey).add(distance);

                    }

                }

            }

        }

        class key implements Comparator {
            @Override
            public int compare(Object o1, Object o2) {

                int distance1 = (int) o1;
                int distance2 = (int) o2;

                if      (distance1 > distance2) return 1;
                else if (distance1 == distance2) return 0;
                return -1;

            }
        }

        //assign the closest distance of all the nodes the neighbors can see + 1
        for (String distanceKey : potentialDistances.keySet()) {

            ArrayList<Integer> sortedDistances = potentialDistances.get(distanceKey);
            sortedDistances.sort(new key());

            int finalDistance = sortedDistances.getLast() + 1;

            if (!distanceMap.containsKey(distanceKey)) distanceMap.put(distanceKey, finalDistance);

            else if (distanceMap.get(distanceKey) > finalDistance) {
                distanceMap.put(distanceKey, finalDistance);
                for (Node testNode : Navigation.nodes)
                    if (Objects.equals(testNode.UUID, distanceKey)) {
                        testNode.distanceMap.put(distanceKey, finalDistance);
                        break;
                    }
            }

        }

    }

    @Override
    public void line(BufferBuilder buffer) {

        for (String connection : this.connections) {

            Node connectingNode = Navigation.getNodeByUUID(connection);

            if (connectingNode != null) {

                buffer.vertex(this.activeTransform, .5f, .5f, .5f).color(this.r, this.g, this.b, this.a);
                buffer.vertex(connectingNode.activeTransform, .5f, .5f, .5f).color(connectingNode.r, connectingNode.g, connectingNode.b, connectingNode.a);

            }

        }

        if (!this.connections.isEmpty() || Navigation.currentNode == this) this.parentLayer.activeDrawCalls++;

        if (Navigation.currentNode == this) {

            buffer.vertex(this.activeTransform, .5f, .5f, .5f).color(this.r, this.g, this.b, this.a);
            buffer.vertex((float) Rendering.client.getCameraEntity().getX(), (float) Rendering.client.getCameraEntity().getY(), (float) Rendering.client.getCameraEntity().getZ()).color(this.r, this.g, this.b, this.a);

        }

    }

    public static void drawText(VertexConsumerProvider.Immediate provider) {

        if (Navigation.nodes.isEmpty()) return;

        ArrayList<Node> nodesCopy = new ArrayList<>(Navigation.nodes);

        nodesCopy.sort((a, b) -> Float.compare(Transforms.facingValue(b.x, b.y, b.z), Transforms.facingValue(a.x, a.y, a.z)));

        Node focusedNode;
        try {
            focusedNode = nodesCopy.getFirst();
        } catch (Exception e) {
            return;
        }

        double distanceTo = Rendering.client.player.getLastRenderPos().distanceTo(new Vec3d(focusedNode.x, focusedNode.y + 1, focusedNode.z));

        if (Transforms.facingValue(focusedNode.x, focusedNode.y + 1, focusedNode.z) <= 1 - .01f && distanceTo <= 60) return;

        Matrix4f spriteTransform = Transforms.getWorld3dSpriteTransform(focusedNode.x, focusedNode.y + 1, focusedNode.z, 0.005f, -0.005f, 0.005f);

        float distanceStringWidth = -Rendering.client.textRenderer.getWidth(focusedNode.tag + ", " + focusedNode.type) / 2f;
        Rendering.client.textRenderer.draw(Text.literal(focusedNode.tag + ", " + focusedNode.type), distanceStringWidth, 0, 0xffffffff, true, spriteTransform, provider, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);

    }

    public static ArrayList<String> ignoredFields = new ArrayList<String>(List.of(new String[]{
            "errorSteps",
            "handleSteps",
            "erroring",
            "eating",
            "finished",
            "distanceMap",
            "ignoredFields"
    }));

    public int errorSteps = 0;
    public int handleSteps = 0;
    public boolean erroring = false;
    public boolean eating = false;
    public boolean finished = false;

    public void init() {
        this.errorSteps = 0;
        this.handleSteps = 0;
        this.erroring = false;
        this.eating = false;
        this.finished = false;
    }

    public double distanceToPlayer() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        return player.getLastRenderPos().distanceTo(new Vec3d(this.x + .5 + this.offsetx, this.y + .5 + this.offsety, this.z + .5 + this.offsetz));

    }

    public boolean isFinished() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        finished = distanceToPlayer() < tolerance;;

        return finished;
    }

    public void handle() {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        stareAtTarget(player);

        MinecraftClient.getInstance().options.forwardKey.setPressed(true);

        handleSteps++;

    }

    public void error() {

        Terminal.addLine("running error handling");

        erroring = true;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        MinecraftClient.getInstance().options.forwardKey.setPressed(true);

        Vector3f idealVector = new Vector3f(Navigation.targetNode.x, (float) player.getY(), Navigation.targetNode.z).sub(Navigation.currentNode.x, (float) player.getY(), Navigation.currentNode.z);

        double innacuracy = idealVector.dot(player.getVelocity().toVector3f());

        Terminal.addLine("innaccuracy: " + innacuracy);
        Terminal.addLine("velocityTime: " + errorSteps);

        if (errorSteps > 5 && errorSteps < 20) {

            //System.out.println("jumping, " + errorSteps);

            client.options.jumpKey.setPressed(true);

            this.handle();

        }

        stareAtTarget(player);

        //TODO: use a kernel for sequencing action, that's what its there for. And make it time based

        //attempt strafe
        //TODO: measure forward velocity and general velocity to detect futile strafing
        if (errorSteps > 20 && errorSteps < 120) {

            System.out.println("attempting strafe, " + Navigation.deviationDirection);

            client.options.jumpKey.setPressed(true);

            if (Navigation.deviationDirection) client.options.rightKey.setPressed(true);
            else                    client.options.leftKey.setPressed(true);
        }

        if (errorSteps >= 120 && errorSteps < 220) {

            client.options.jumpKey.setPressed(true);

            if (Navigation.deviationDirection) client.options.leftKey.setPressed(true);
            else                    client.options.rightKey.setPressed(true);

        }

        //attempt door unstick
        //TODO: swap to non-placeable in the event of a door unstick attempt
        if (errorSteps >= 220 && errorSteps < 240) {

            player.setPitch(60);
            client.options.useKey.setPressed(true);

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

}

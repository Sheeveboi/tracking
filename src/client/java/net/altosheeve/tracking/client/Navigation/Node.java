package net.altosheeve.tracking.client.Navigation;

import net.altosheeve.tracking.client.Core.Rendering;
import net.altosheeve.tracking.client.Shapes.Box;
import net.altosheeve.tracking.client.Shapes.Transforms;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.*;

public class Node extends Box {

    public enum NodeType {
        NORMAL,
        ICEROAD,
        USE,
        LODE
    }

    public String tag;
    public ArrayList<Integer> connections;
    public NodeType type;
    public Map<Integer, Integer> distanceMap = new HashMap<>();

    public float velocityThreshold = 0.09f; //enable recovery if bot speed is below this threshold
    public float innaccuracyThreshold = 0.9f; //enable recovery if bot vector in relation to target node deviates from ideal currentNode to targetNode vector
    public float tolerance = 0.7f; //completion distance to targetNode
    public boolean sprint = false; //run fast
    public float lookx = 0;
    public float looky = 0;
    public float lookz = 0;
    public int waitTimeIn = 0; //how many ticks to wait before pathing to this node
    public int waitTimeOut = 0; //how many ticks to wait after pathing to this node

    public void setColor() {
        this.a = .3f;
        switch (this.type) {
            case NORMAL -> {
                this.r = 0;
                this.g = 0;
                this.b = 1;
            }

            case ICEROAD -> {
                this.r = .5f;
                this.g = .5f;
                this.b = 1f;
            }

            case USE -> {
                this.r = 1f;
                this.g = 0f;
                this.b = 0f;
            }

            case LODE -> {
                this.r = .5f;
                this.g = .5f;
                this.b = 0;
            }
        }
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
        this.setColor();
        this.connections = new ArrayList<>();
    }

    public Node(int x, int y, int z, NodeType type, String tag) {
        super(x, y, z, "");

        String uuid = String.valueOf(java.util.UUID.randomUUID());
        this.UUID = uuid;

        System.out.println("created node: " + this.UUID);
        this.type = type;
        this.setColor();
        this.tag = tag;
        this.connections = new ArrayList<>();
    }

    public Node(int x, int y, int z, NodeType type, String tag, boolean sprint, float velocityThreshold, int waitTimeIn, int waitTimeOut) {
        super(x, y, z, "");

        String uuid = String.valueOf(java.util.UUID.randomUUID());
        this.UUID = uuid;

        System.out.println("created node: " + this.UUID);
        this.type = type;
        this.setColor();
        this.tag = tag;
        this.connections = new ArrayList<>();
        this.velocityThreshold = velocityThreshold;
        this.sprint = sprint;
        this.waitTimeIn = waitTimeIn;
        this.waitTimeOut = waitTimeOut;
    }

    public Node(int x, int y, int z, NodeType type, String tag, ArrayList<Integer> connections) {
        super(x, y, z, "");

        String uuid = String.valueOf(java.util.UUID.randomUUID());
        this.UUID = uuid;

        System.out.println("created node: " + this.UUID);
        this.type = type;
        this.setColor();
        this.tag = tag;
        this.connections = connections;
    }

    public void calculateDistances() {

        Map<Integer, ArrayList<Integer>> potentialDistances = new HashMap<>();

        //gather distances of all neighbors
        for (int index : this.connections) {

            Node neighbor = Navigation.nodes.get(index);

            //set distances to self to one on parent node
            Navigation.nodes.get(index).distanceMap.put(Navigation.nodes.indexOf(this), 1);
            this.distanceMap.put(index, 1);

            //copy neighbors distance map
            for (int distanceKey : neighbor.distanceMap.keySet()) {

                if (distanceKey != Navigation.nodes.indexOf(this)) {

                    int distance = neighbor.distanceMap.get(distanceKey);

                    if (!potentialDistances.containsKey(distanceKey))
                        potentialDistances.put(distanceKey, new ArrayList<>());

                    potentialDistances.get(distanceKey).add(distance);

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
        for (int distanceKey : potentialDistances.keySet()) {

            ArrayList<Integer> sortedDistances = potentialDistances.get(distanceKey);
            sortedDistances.sort(new key());

            int finalDistance = sortedDistances.getLast() + 1;

            if (!distanceMap.containsKey(distanceKey)) distanceMap.put(distanceKey, finalDistance);

            else if (distanceMap.get(distanceKey) > finalDistance) {
                distanceMap.put(distanceKey, finalDistance);
                Navigation.nodes.get(distanceKey).distanceMap.put(distanceKey, finalDistance);
            }

        }

    }

    @Override
    public void line(BufferBuilder buffer) {

        for (int i : this.connections) {

            Node connectingNode = Navigation.nodes.get(i);

            buffer.vertex(this.activeTransform, .5f, .5f, .5f).color(this.r, this.g, this.b, this.a);
            buffer.vertex(connectingNode.activeTransform, .5f, .5f, .5f).color(connectingNode.r, connectingNode.g, connectingNode.b, connectingNode.a);

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

}

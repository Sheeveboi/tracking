package net.altosheeve.tracking.client.Navigation;

import net.altosheeve.tracking.client.Core.Rendering;
import net.altosheeve.tracking.client.Navigation.Nodes.Node;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

//TODO: this class is purely for local storage
//TODO: create dedicated package for static (non realtime) information sharing

public class NodeCreation {

    public static String current = System.getProperty("user.dir");
    public static File nodeFile = new File(current + File.separator + "nodes/default.json");

    public static void selectNode() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        for (Node node : Navigation.nodes) {
            if (node.x == player.getBlockX() && node.y == player.getBlockY() && node.z == player.getBlockZ()) {
                Navigation.currentNode = node;
                return;
            }
        }
        Navigation.currentNode = null; //unselect
    }

    public static void connectNode() throws IOException {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        assert player != null;
        if (Navigation.currentNode == null) return;

        for (Node node : Navigation.nodes) {
            if (node.x == player.getBlockX() && node.y == player.getBlockY() && node.z == player.getBlockZ()) {

                if (node.connections.contains(Navigation.currentNode.UUID)) node.connections.remove(Navigation.currentNode.UUID);
                else node.connections.add(Navigation.currentNode.UUID);

                break;
            }
        }

        dumpNodes();
    }

    public static void dumpNodes() throws IOException {

        if (!nodeFile.exists()) {
            nodeFile.createNewFile();
            return;
        }

        JSONArray nodesArray = new JSONArray();

        List<Field> allFields = Arrays.asList(Node.class.getDeclaredFields());

        System.out.println(allFields);

        for (Node node : Navigation.nodes) {

            JSONObject nodeJson = new JSONObject();

            for (Field field : allFields) nodeJson.put(field.getName(), node.getField(field.getName())); //I love reflection!
            nodesArray.put(nodeJson);

        }

        JSONObject nodesJson = new JSONObject();
        nodesJson.put("nodes", nodesArray);

        FileWriter writer = new FileWriter(current + File.separator + "nodes/" + nodeFile.getName());
        writer.write(nodesJson.toString());
        writer.close();
    }

    public static void loadNodes() throws IOException {
        if (!nodeFile.exists()) {
            String current = System.getProperty("user.dir");
            System.out.println(new File(current + File.separator + "nodes").mkdir());
            nodeFile.createNewFile();
            return;
        }

        //TODO: implement targeted removal and addition for nodes so we arent rebuilding every time a change to the node system is made
        Navigation.clearNodes();

        Scanner scanner = new Scanner(nodeFile);
        StringBuilder jsonString = new StringBuilder();
        while (scanner.hasNextLine()) jsonString.append(scanner.nextLine());

        System.out.println(jsonString);

        if (jsonString.isEmpty()) return;

        JSONObject nodesJson = new JSONObject(jsonString.toString());
        JSONArray nodes = nodesJson.getJSONArray("nodes");

        for (int i = 0; i < nodes.length(); i++) {

            System.out.println("adding node");

            Node newNode = new Node(0, 0, 0);
            JSONObject nodeJson = nodes.getJSONObject(i);

            System.out.println(nodeJson.toString());

            for (String field : nodeJson.keySet()) newNode.setField(field, nodeJson.get(field));
            for (Object connection : nodeJson.getJSONArray("connections").toList()) newNode.connections.add((String) connection);

            newNode = Node.mutate(newNode);

            newNode.UUID = nodeJson.getString("uuid");

            Navigation.addNode(newNode);

            System.out.println(Navigation.nodes);
        }

        Navigation.calculateAllNodes();

    }

    public static void addNextNode() {

        ClientPlayerEntity player = Rendering.client.player;

        if (Navigation.currentNode == null) {

            Node newNode = new Node(

                    player.getBlockX(),
                    player.getBlockY(),
                    player.getBlockZ(),

                    Node.NodeType.NORMAL,
                    "Name me!",
                    false,
                    .7f,
                    0,
                    0

            );

            newNode = Node.mutate(newNode);

            Navigation.addNode(newNode);

            Navigation.currentNode = newNode;

            return;

        }

        Node newNode = new Node(

                player.getBlockX(),
                player.getBlockY(),
                player.getBlockZ(),

                Navigation.currentNode.type,
                Navigation.currentNode.tag,

                Navigation.currentNode.sprint,
                Navigation.currentNode.velocityThreshold,
                Navigation.currentNode.waitTimeIn,
                Navigation.currentNode.waitTimeOut

        );

        newNode = Node.mutate(newNode);

        newNode.connections.add(Navigation.currentNode.UUID);

        Navigation.addNode(newNode);

        Navigation.currentNode = newNode;

        try { dumpNodes(); }
        catch (IOException e) { throw new RuntimeException(e); }

    }

}

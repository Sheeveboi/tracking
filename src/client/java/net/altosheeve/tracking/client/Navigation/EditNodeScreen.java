package net.altosheeve.tracking.client.Navigation;

import net.altosheeve.tracking.client.Soprano.Execution;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

//TODO: this is short term but this screen could def be improved

public class EditNodeScreen extends Screen {

    public EditNodeScreen(Text title) {
        super(title);
    }

    @Override
    public void init() {

        TextWidget errorField = new TextWidget(10, 1, 500, 20, Text.of(""), this.textRenderer);

        TextWidget nodeXName = new TextWidget(95, 20, 100, 20, Text.of("Node X"), this.textRenderer);
        TextWidget nodeYName = new TextWidget(95, 40, 100, 20, Text.of("Node Y"), this.textRenderer);
        TextWidget nodeZName = new TextWidget(95, 60, 100, 20, Text.of("Node Z"), this.textRenderer);

        TextWidget nodeTypeName = new TextWidget(95, 80, 100, 20, Text.of("Node Type"), this.textRenderer);
        TextWidget nodeNameName = new TextWidget(95, 100, 100, 20, Text.of("Node Name"), this.textRenderer);

        TextFieldWidget nodeX = new TextFieldWidget(this.textRenderer, 10, 20, 150, 20, Text.of("Node X"));
        TextFieldWidget nodeY = new TextFieldWidget(this.textRenderer, 10, 40, 150, 20, Text.of("Node Y"));
        TextFieldWidget nodeZ = new TextFieldWidget(this.textRenderer, 10, 60, 150, 20, Text.of("Node Z"));

        TextFieldWidget type = new TextFieldWidget(this.textRenderer, 10, 80, 150, 20, Text.of("Node Type"));
        TextFieldWidget name = new TextFieldWidget(this.textRenderer, 10, 100, 150, 20, Text.of("Node Name"));

        TextFieldWidget nodefileName = new TextFieldWidget(this.textRenderer, 10, 170, 150, 20, Text.of("File Name"));
        TextWidget currentNodefile = new TextWidget(10, 230, 150, 20, Text.of("Current: " + NodeCreation.nodeFile.getName()), this.textRenderer);

        int rightSide = this.width - 160;

        TextFieldWidget nodeTag = new TextFieldWidget(this.textRenderer, rightSide, 60, 150, 20, Text.of("Node Selection"));
        TextFieldWidget itemTag = new TextFieldWidget(this.textRenderer, rightSide, 80, 150, 20, Text.of("Item Selection"));

        TextWidget nodeTagName = new TextWidget(rightSide + 85, 60, 100, 20, Text.of("Dest Node"), this.textRenderer);
        TextWidget itemTagName = new TextWidget(rightSide + 85, 80, 100, 20, Text.of("Item Tag"), this.textRenderer);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;

        if (Navigation.currentNode != null) {
            nodeX.setText(String.valueOf((int) Navigation.currentNode.x));
            nodeY.setText(String.valueOf((int) Navigation.currentNode.y));
            nodeZ.setText(String.valueOf((int) Navigation.currentNode.z));

            name.setText(Navigation.currentNode.tag);
            type.setText(Navigation.currentNode.type.toString());
        } else {

            nodeX.setText(String.valueOf(player.getBlockX()));
            nodeY.setText(String.valueOf(player.getBlockY()));
            nodeZ.setText(String.valueOf(player.getBlockZ()));

            type.setText("NORMAL");
        }

        ButtonWidget createOrLoadNodefile = ButtonWidget.builder(Text.of("Create or load .NODE file"), (widget) -> {

            if (Objects.equals(nodefileName.getText(), "")) {
                errorField.setMessage(Text.of("Error: .NODE file Name field not set"));
                return;
            }

            NodeCreation.nodeFile = new File(NodeCreation.current + File.separator + "nodes/" + nodefileName.getText() + ".NODE");

            try {

                if (!NodeCreation.nodeFile.exists()) NodeCreation.nodeFile.createNewFile();

                nodefileName.setMessage(Text.of("Current: " + NodeCreation.nodeFile.getName()));

                NodeCreation.loadNodes();

            } catch (IOException e) { throw new RuntimeException(e); }


        }).dimensions(10, 190, 150, 20).build();

        ButtonWidget deleteCurrentNodefile = ButtonWidget.builder(Text.of("Delete Current .NODE File"), (widget) -> {

            if (!NodeCreation.nodeFile.exists()) {
                errorField.setMessage(Text.of("Error: No current .NODE file to delete"));

            }

            NodeCreation.nodeFile.delete();

        }).dimensions(10, 210, 150, 20).build();

        ButtonWidget update = ButtonWidget.builder(Text.of("Update"), (widget) -> {

            if (Navigation.currentNode == null) return;

            if (Objects.equals(name.getText(), "")) {
                errorField.setMessage(Text.of("Error: Node Name field not set"));
                return;
            }

            if (Objects.equals(type.getText(), "")) {
                errorField.setMessage(Text.of("Error: Node Type field not set"));
                return;
            }

            for (Node node : Navigation.nodes) {
                if (node == Navigation.currentNode) {
                    node.x = Integer.parseInt(nodeX.getText());
                    node.y = Integer.parseInt(nodeY.getText());
                    node.z = Integer.parseInt(nodeZ.getText());

                    node.tag = name.getText();

                    try {
                        node.type = Node.NodeType.valueOf(type.getText());
                    } catch (IllegalArgumentException ignored) {
                        errorField.setMessage(Text.of("Error: Node Type does not exist"));
                    }

                    break;
                }
            }

            try { NodeCreation.dumpNodes(); }
            catch (IOException e) { throw new RuntimeException(e); }
        }).dimensions(60, 120, 50, 20).build();;

        ButtonWidget add = ButtonWidget.builder(Text.of("Add"), (widget) -> {
            try {

                if (Objects.equals(name.getText(), "")) {
                    errorField.setMessage(Text.of("Error: Node Name field not set"));
                    return;
                }

                if (Objects.equals(type.getText(), "")) {
                    errorField.setMessage(Text.of("Error: Node Type field not set"));
                    return;
                }

                Node.NodeType typeObj;

                try {
                    typeObj = Node.NodeType.valueOf(type.getText());
                } catch (IllegalArgumentException e) {
                    errorField.setMessage(Text.of("Error: Node Type does not exist"));
                    return;
                }

                Node newNode = new Node(
                        player.getBlockX(),
                        player.getBlockY(),
                        player.getBlockZ(),

                        typeObj,
                        name.getText()
                );

                Navigation.addNode(newNode);

                if (Navigation.currentNode != null) {
                    newNode.connections.add(Navigation.nodes.indexOf(Navigation.currentNode));
                    Navigation.currentNode.connections.add(Navigation.nodes.indexOf(newNode));
                }

                Navigation.currentNode = newNode;

            } catch (IllegalArgumentException ignored) { System.out.println(ignored); }

            try { NodeCreation.dumpNodes(); }
            catch (IOException e) { throw new RuntimeException(e); }
        }).dimensions(10, 120, 50, 20).build();

        ButtonWidget delete = ButtonWidget.builder(Text.of("Delete"), (widget) -> {

            if (Navigation.currentNode != null) {

                int index = Navigation.nodes.indexOf(Navigation.currentNode);

                for (Node node : Navigation.nodes) {
                    if (node != Navigation.currentNode && node.connections.contains(Navigation.nodes.indexOf(Navigation.currentNode)))
                        node.connections.remove((Integer) Navigation.nodes.indexOf(Navigation.currentNode));

                    node.distanceMap.remove(Navigation.nodes.indexOf(Navigation.currentNode));

                    ArrayList<Integer> newConnections = new ArrayList<>();

                    for (int connection : node.connections) {
                        if (connection >= index) {
                            connection --;
                            newConnections.add(connection);
                        }
                        else newConnections.add(connection);
                    }

                    node.connections = newConnections;

                }

                Navigation.removeNode(Navigation.currentNode);

                Navigation.currentNode = null;

            }

            try { NodeCreation.dumpNodes(); }
            catch (IOException e) { throw new RuntimeException(e); }
        }).dimensions(110, 120, 50, 20).build();

        ButtonWidget getPlayerCoords = ButtonWidget.builder(Text.of("Apply Player Coords"), (widget) -> {
                    nodeX.setText(String.valueOf(player.getBlockX()));
                    nodeY.setText(String.valueOf(player.getBlockY()));
                    nodeZ.setText(String.valueOf(player.getBlockZ()));
        }).dimensions(10, 140, 150, 20).build();

        ButtonWidget calibrate = ButtonWidget.builder(Text.of("Calibrate"), (widget) -> {

            if (Objects.equals(nodeTag.getText(), "")) {
                errorField.setMessage(Text.of("Error: Node Tag not set"));
                return;
            }

            Node targetNode = null;
            for (Node node : Navigation.nodes) {
                if (Objects.equals(node.tag, nodeTag.getText())) {
                    targetNode = node;
                    break;
                }
            }

            if (targetNode == null) {
                errorField.setMessage(Text.of("Error: Could not find node with that tag"));
                return;
            }

            Execution.setProgram(ShellPrograms.calibrate(targetNode));

        }).dimensions(rightSide, 100, 150, 20).build();

        ButtonWidget pathTo = ButtonWidget.builder(Text.of("Path To"), (widget) -> {

            if (Objects.equals(nodeTag.getText(), "")) {
                errorField.setMessage(Text.of("Error: Node Tag not set"));
                return;
            }

            Node targetNode = null;
            for (Node node : Navigation.nodes) {
                if (Objects.equals(node.tag, nodeTag.getText())) {
                    targetNode = node;
                    break;
                }
            }

            if (targetNode == null) {
                errorField.setMessage(Text.of("Error: Could not find node with that tag"));
                return;
            }

            Execution.setProgram(ShellPrograms.pathTo(targetNode));

        }).dimensions(rightSide, 120, 150, 20).build();

        ButtonWidget gather = ButtonWidget.builder(Text.of("Gather"), (widget) -> {

            if (Objects.equals(itemTag.getText(), "")) {
                errorField.setMessage(Text.of("Error: Item Tag not set"));
                return;
            }

            Execution.setProgram(ShellPrograms.gather(itemTag.getText()));

        }).dimensions(rightSide, 140, 150, 20).build();

        ButtonWidget gatherFrom = ButtonWidget.builder(Text.of("Gather From"), (widget) -> {

            if (Objects.equals(nodeTag.getText(), "")) {
                errorField.setMessage(Text.of("Error: Node Tag not set"));
                return;
            }

            if (Objects.equals(itemTag.getText(), "")) {
                errorField.setMessage(Text.of("Error: Item Tag not set"));
                return;
            }

            Node targetNode = null;
            for (Node node : Navigation.nodes) {
                if (Objects.equals(node.tag, nodeTag.getText())) {
                    targetNode = node;
                    break;
                }
            }

            if (targetNode == null) {
                errorField.setMessage(Text.of("Error: Could not find node with that tag"));
                return;
            }

            Execution.setProgram(ShellPrograms.gatherFrom(targetNode, itemTag.getText()));

        }).dimensions(rightSide, 160, 150, 20).build();

        ButtonWidget store = ButtonWidget.builder(Text.of("Store"), (widget) -> {

            if (Objects.equals(nodeTag.getText(), "")) {
                errorField.setMessage(Text.of("Error: Node Tag not set"));
                return;
            }

            if (Objects.equals(itemTag.getText(), "")) {
                errorField.setMessage(Text.of("Error: Item Tag not set"));
                return;
            }

            Node targetNode = null;
            for (Node node : Navigation.nodes) {
                if (Objects.equals(node.tag, nodeTag.getText())) {
                    targetNode = node;
                    break;
                }
            }

            if (targetNode == null) {
                errorField.setMessage(Text.of("Error: Could not find node with that tag"));
                return;
            }

            Execution.setProgram(ShellPrograms.store(targetNode, itemTag.getText()));

        }).dimensions(rightSide, 180, 150, 20).build();

        ButtonWidget storeAll = ButtonWidget.builder(Text.of("Store All"), (widget) -> {

            if (Objects.equals(nodeTag.getText(), "")) {
                errorField.setMessage(Text.of("Error: Node Tag not set"));
                return;
            }

            Node targetNode = null;
            for (Node node : Navigation.nodes) {
                if (Objects.equals(node.tag, nodeTag.getText())) {
                    targetNode = node;
                    break;
                }
            }

            if (targetNode == null) {
                errorField.setMessage(Text.of("Error: Could not find node with that tag"));
                return;
            }

            Execution.setProgram(ShellPrograms.storeAll(targetNode));

        }).dimensions(rightSide, 200, 150, 20).build();

        ButtonWidget make = ButtonWidget.builder(Text.of("Make"), (widget) -> {

            errorField.setMessage(Text.of("Error: To be implemented"));

        }).dimensions(rightSide, 220, 150, 20).build();

        ButtonWidget killRunning = ButtonWidget.builder(Text.of("Kill Running"), (widget) -> {

            Execution.clearAll();

        }).dimensions(rightSide, 250, 150, 20).build();

        addDrawableChild(nodeX);
        addDrawableChild(nodeY);
        addDrawableChild(nodeZ);
        addDrawableChild(name);
        addDrawableChild(type);
        addDrawableChild(update);
        addDrawableChild(delete);
        addDrawableChild(add);
        addDrawableChild(getPlayerCoords);

        addDrawableChild(nodefileName);
        addDrawableChild(createOrLoadNodefile);
        addDrawableChild(deleteCurrentNodefile);
        addDrawableChild(currentNodefile);

        addDrawableChild(nodeXName);
        addDrawableChild(nodeYName);
        addDrawableChild(nodeZName);
        addDrawableChild(nodeTypeName);
        addDrawableChild(nodeNameName);
        addDrawableChild(errorField);

        addDrawableChild(nodeTag);
        addDrawableChild(itemTag);
        addDrawableChild(nodeTagName);
        addDrawableChild(itemTagName);

        addDrawableChild(calibrate);
        addDrawableChild(pathTo);
        addDrawableChild(gather);
        addDrawableChild(gatherFrom);
        addDrawableChild(store);
        addDrawableChild(storeAll);
        addDrawableChild(make);

        addDrawableChild(killRunning);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        super.render(context, mouseX, mouseY, delta);

        String currentTag = "None";
        String targetTag = "None";

        if (Navigation.currentNode != null) currentTag = Navigation.currentNode.tag;
        if (Navigation.targetNode != null) targetTag = Navigation.targetNode.tag;

        context.drawText(this.textRenderer, "Current Node: " + currentTag, this.width - 160, 30, 0xffffffff, true);
        context.drawText(this.textRenderer, "Target Node: " + targetTag, this.width - 160, 50, 0xffffffff, true);

    }
}


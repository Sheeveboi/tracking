package net.altosheeve.tracking.client.Soprano;

import net.altosheeve.tracking.client.Navigation.Navigation;
import net.altosheeve.tracking.client.Navigation.Node;
import net.altosheeve.tracking.client.Soprano.Async.Request;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CivKernel extends BasicFunctions {

    private static final Logger log = LoggerFactory.getLogger(CivKernel.class);

    public void _CALIBRATE() {
        System.out.println("Calibrating");

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;

        String targetTag = Typing._PARSE_STRING(this);
        double tolerance = Typing._PARSE_FLOAT(this);

        Optional<Node> testing = Navigation.nodes.stream().filter(node -> Objects.equals(targetTag, node.tag)).findFirst();
        if (testing.isEmpty()) return;

        Node targetNode = testing.get();

        System.out.println(targetNode.tag);
        System.out.println(targetNode.x);
        System.out.println(targetNode.y);
        System.out.println(targetNode.z);

        player.setYaw(0);

        ArrayList<Double> xAveragingBuffer = new ArrayList<>();
        ArrayList<Double> zAveragingBuffer = new ArrayList<>();

        AtomicBoolean xOscillating = new AtomicBoolean(false);
        AtomicBoolean zOscillating = new AtomicBoolean(false);

        int filterCutoff = 10;

        final int[] xCorrectionAttempts = {10};
        final int[] zCorrectionAttempts = {10};

        this.addRequest(new Request(() -> {

            Navigation.resetControls();

            xAveragingBuffer.add(player.getVelocity().x);
            zAveragingBuffer.add(player.getVelocity().z);

            if (xAveragingBuffer.size() > filterCutoff) xAveragingBuffer.removeFirst();
            if (zAveragingBuffer.size() > filterCutoff) zAveragingBuffer.removeFirst();

            double xAveragingSum = 0;
            for (double velocity : xAveragingBuffer) xAveragingSum += velocity;
            xAveragingSum /= xAveragingBuffer.size();

            double zAveragingSum = 0;
            for (double velocity : zAveragingBuffer) zAveragingSum += velocity;
            zAveragingSum /= zAveragingBuffer.size();

            if (Math.abs(xAveragingSum) <= .01 &&
                    xAveragingBuffer.size() == filterCutoff &&
                    xAveragingBuffer.get((int) Math.floor((double) xAveragingBuffer.size() / 2)) > .01) xOscillating.set(true);
            if (Math.abs(zAveragingSum) <= .01 &&
                    zAveragingBuffer.size() == filterCutoff &&
                    zAveragingBuffer.get((int) Math.floor((double) zAveragingBuffer.size() / 2)) > .01) zOscillating.set(true);

            System.out.println((player.getX() - (targetNode.x + .5)));
            System.out.println((player.getZ() - (targetNode.z + .5)));

            boolean left = player.getX() < targetNode.x + .5;
            boolean forward = player.getZ() < targetNode.z + .5;

            boolean xFufilled = Math.abs((player.getX() - (targetNode.x + .5))) < tolerance;
            boolean zFufilled = Math.abs((player.getZ() - (targetNode.z + .5))) < tolerance;

            if (!zFufilled) {

                if (forward) client.options.forwardKey.setPressed(true);
                else client.options.backKey.setPressed(true);

                if (zOscillating.get()) {

                    client.options.forwardKey.setPressed(false);
                    client.options.backKey.setPressed(false);

                    if ((Navigation.tick % 20) == 0 && zCorrectionAttempts[0] > 0) {

                        zCorrectionAttempts[0] --;

                        if (forward) client.options.forwardKey.setPressed(true);
                        else client.options.backKey.setPressed(true);

                    }

                    if (zCorrectionAttempts[0] == 0) zFufilled = true;

                }

            }

            if (!xFufilled) {

                if (left) client.options.leftKey.setPressed(true);
                else client.options.rightKey.setPressed(true);

                if (xOscillating.get()) {

                    client.options.leftKey.setPressed(false);
                    client.options.rightKey.setPressed(false);

                    if ((Navigation.tick % filterCutoff) == 0 && xCorrectionAttempts[0] > 0) {

                        xCorrectionAttempts[0] --;

                        if (left) client.options.leftKey.setPressed(true);
                        else client.options.rightKey.setPressed(true);

                    }

                    if (xCorrectionAttempts[0] == 0) xFufilled = true;

                }
            }

            client.options.sneakKey.setPressed(true);

            if (xFufilled && zFufilled) {

                if (Navigation.tick % filterCutoff == 0) {

                    Navigation.resetControls();
                    Navigation.currentNode = targetNode;

                    double dx = player.getX() - targetNode.x - .5;
                    double dy = player.getY() - targetNode.y - 1.5;
                    double dz = player.getZ() - targetNode.z - .5;

                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                    dx /= dist;
                    dy /= dist;
                    dz /= dist;

                    float pitch = (float) Math.asin(-dy);
                    float yaw = (float) Math.atan2(dz, dx);

                    pitch = (float) (pitch * 180.0 / Math.PI);
                    yaw = (float) (yaw * 180.0 / Math.PI);

                    yaw += 90;

                    player.setPitch(pitch);
                    player.setYaw(yaw);

                    return true;
                }
            }

            return false;
        }));
    }

    public void _MOVE_TO() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;

        System.out.println("parsing integers");

        int blockX = Typing._PARSE_INTEGER(this);
        int blockY = Typing._PARSE_INTEGER(this);
        int blockZ = Typing._PARSE_INTEGER(this);

        System.out.println("blockX: " + blockX);
        System.out.println("blockY: " + blockY);
        System.out.println("blockZ: " + blockZ);

        float tolerance = Typing._PARSE_FLOAT(this);

        final boolean[] firstTick = {true};
        this.addRequest(new Request(() -> {

            Navigation.resetControls();

            boolean out = player.getLastRenderPos().distanceTo(new Vec3d(blockX + .5, blockY + .5, blockZ + .5)) < tolerance;

            if (Navigation.targetNode.type == Node.NodeType.INTERACTABLE) {
                System.out.println(out);
                MinecraftClient.getInstance().options.forwardKey.setPressed(!out);
            }
            else MinecraftClient.getInstance().options.forwardKey.setPressed(true);

            if (!firstTick[0] || out) Navigation.handler.cb();

            firstTick[0] = false;

            return out;
        }));

        //ensure there actually is a screen for the script to use
        if (Navigation.targetNode.type == Node.NodeType.INTERACTABLE) this.addRequest(new Request(() -> MinecraftClient.getInstance().player.currentScreenHandler != null));
    }

    public void _PATH_TO() {
        String targetTag = Typing._PARSE_STRING(this);
        float tolerance = Typing._PARSE_FLOAT(this);

        int origin = this.programPointer + 1;

        ArrayList<Integer> itinerary = Navigation.generatePathingItinerary(targetTag);
        assert itinerary != null;

        System.out.println("itinerary: " + itinerary);

        for (int i : itinerary) {

            Node node = Navigation.nodes.get(i);

            this.insertInstruction((byte) 0x3, origin); origin++; //set as target node

            this.insertInstruction((byte) Typing.STATIC_EXPRESSION, origin); origin++; //set as static expression
            this.insertInstructions(Typing._ENCODE_INTEGER(i), origin); origin += Typing.INTEGER_SIZE + 1; //encode index

            switch (node.type) {
                case NORMAL:

                    this.insertInstruction((byte) 0x5, origin); origin++; //set basic movement chestHandler

                    this.insertInstruction((byte) Typing.STATIC_EXPRESSION, origin); origin++; //set as static expression
                    this.insertInstructions(Typing._ENCODE_FLOAT(.09f), origin); origin += Typing.FLOAT_SIZE + 1; //encode velocity threshold

                    break;

                case ICEROAD:

                    this.insertInstruction((byte) 0x7, origin); origin++; //set basic movement chestHandler

                    this.insertInstruction((byte) Typing.STATIC_EXPRESSION, origin); origin++; //set as static expression
                    this.insertInstructions(Typing._ENCODE_FLOAT(.09f), origin); origin += Typing.FLOAT_SIZE + 1; //encode velocity threshold

                    break;

                case INTERACTABLE:

                    this.insertInstruction((byte) 0x8, origin); origin++; //set basic interaction chestHandler

                    this.insertInstruction((byte) Typing.STATIC_EXPRESSION, origin); origin++; //set as static expression
                    this.insertInstructions(Typing._ENCODE_FLOAT(2), origin); origin += Typing.FLOAT_SIZE + 1; //encode interaction threshold

                    tolerance = 2;

                    this.insertInstruction((byte) Typing.STATIC_EXPRESSION, origin); origin++; //set as static expression
                    this.insertInstructions(Typing._ENCODE_FLOAT(.09f), origin); origin += Typing.FLOAT_SIZE + 1; //encode velocity threshold

                    break;

            }

            this.insertInstruction((byte) 0x1, origin); origin++; //walk to

            this.insertInstruction((byte) Typing.STATIC_EXPRESSION, origin); origin++;
            this.insertInstructions(Typing._ENCODE_INTEGER((int) node.x), origin); origin += Typing.INTEGER_SIZE + 1;

            this.insertInstruction((byte) Typing.STATIC_EXPRESSION, origin); origin++;
            this.insertInstructions(Typing._ENCODE_INTEGER((int) node.y), origin); origin += Typing.INTEGER_SIZE + 1;

            this.insertInstruction((byte) Typing.STATIC_EXPRESSION, origin); origin++;
            this.insertInstructions(Typing._ENCODE_INTEGER((int) node.z), origin); origin += Typing.INTEGER_SIZE + 1;

            this.insertInstruction((byte) Typing.STATIC_EXPRESSION, origin); origin++;
            this.insertInstructions(Typing._ENCODE_FLOAT(tolerance), origin); origin += Typing.FLOAT_SIZE + 1; //set door threshold

            this.insertInstruction((byte) 0x4, origin); origin++; //set as current node
            this.insertInstruction((byte) Typing.STATIC_EXPRESSION, origin); origin++;
            this.insertInstructions(Typing._ENCODE_INTEGER(i), origin); origin += Typing.INTEGER_SIZE + 1; //encode static integer

        }

        this.insertInstruction((byte) 0x2aa, origin); //reset all controls

    }

    public void _RESET_CONTROLS() {
        System.out.println("resetting controls");
        Navigation.resetControls();
    }

    public void _SET_CURRENT_NODE() {
        int index = Typing._PARSE_INTEGER(this);
        Navigation.currentNode = Navigation.nodes.get(index);
    }

    public void _SET_TARGET_NODE() {
        int index = Typing._PARSE_INTEGER(this);
        Navigation.targetNode = Navigation.nodes.get(index);
        System.out.println(Navigation.targetNode.tag);
    }

    public void _SET_BASIC_MOVEMENT_HANDLER() {
        Navigation.velocityThreshold = Typing._PARSE_FLOAT(this);
        Navigation.handler = Navigation::basicWalkHandler;
    }

    public void _SET_ICEROAD_HANDLER() {
        Navigation.velocityThreshold = Typing._PARSE_FLOAT(this);
        Navigation.handler = Navigation::iceroadHandler;
    }

    public void _SET_INTERACTION_HANDLER() {
        Navigation.interactionThreshold = Typing._PARSE_FLOAT(this);
        Navigation.velocityThreshold = Typing._PARSE_FLOAT(this);
        Navigation.handler = Navigation::interactionHandler;
    }

    public void _EXIT_INTERACTION() {
        System.out.println("exiting interaction");
        if (MinecraftClient.getInstance().player.currentScreenHandler == null) return;
        if (MinecraftClient.getInstance().currentScreen == null) return;
        MinecraftClient.getInstance().currentScreen.close();
    }

    public void _QUICK_MOVE_BY_INDEX() {
        int index = Typing._PARSE_INTEGER(this);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ScreenHandler chestHandler = player.currentScreenHandler;
        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;

        if (chestHandler == null) return;

        interactionManager.clickSlot(
                chestHandler.syncId,
                chestHandler.slots.get(index).id,
                0,
                SlotActionType.QUICK_MOVE,
                player
        );
    }

    public void _QUICK_MOVE_BY_NAME_INCLUSIVE() {

        String name = Typing._PARSE_STRING(this);
        int toInventoryOrInteractable = Typing._PARSE_INTEGER(this);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ScreenHandler chestHandler = player.currentScreenHandler;
        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;

        if (chestHandler == null) return;

        chestHandler.enableSyncing();

        int interactableSize = chestHandler.slots.size() - 36;

        if (toInventoryOrInteractable == 0) {
            for (int i = 0; i < interactableSize - 1; i++) {
                Slot slot = chestHandler.slots.get(i);
                if (slot.getStack().getItemName().getString().equals(name) || name.isEmpty()) {
                    interactionManager.clickSlot(
                            chestHandler.syncId,
                            slot.id,
                            0,
                            SlotActionType.QUICK_MOVE,
                            player
                    );
                }
            }
        }

        if (toInventoryOrInteractable == 1) {
            for (int i = interactableSize; i < chestHandler.slots.size() - 1; i++) {
                Slot slot = chestHandler.slots.get(i);
                if (slot.getStack().getItemName().getString().equals(name) || name.isEmpty()) {
                    interactionManager.clickSlot(
                            chestHandler.syncId,
                            slot.id,
                            0,
                            SlotActionType.QUICK_MOVE,
                            player
                    );
                }
            }
        }

        chestHandler.syncState();
        chestHandler.updateToClient();

    }

    public void _QUICK_MOVE_BY_NAME_EXCLUSIVE() {

        String name = Typing._PARSE_STRING(this);
        int toInventoryOrInteractable = Typing._PARSE_INTEGER(this);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ScreenHandler chestHandler = player.currentScreenHandler;
        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;

        if (chestHandler == null) return;

        chestHandler.enableSyncing();

        int interactableSize = chestHandler.slots.size() - 36;

        if (toInventoryOrInteractable == 0) {
            for (int i = 0; i < interactableSize - 1; i++) {
                Slot slot = chestHandler.slots.get(i);
                if (!slot.getStack().getItemName().getString().equals(name) || name.isEmpty()) {
                    interactionManager.clickSlot(
                            chestHandler.syncId,
                            slot.id,
                            0,
                            SlotActionType.QUICK_MOVE,
                            player
                    );
                }
            }
        }

        if (toInventoryOrInteractable == 1) {
            for (int i = interactableSize; i < chestHandler.slots.size(); i++) {
                Slot slot = chestHandler.slots.get(i);
                if (!slot.getStack().getItemName().getString().equals(name) || name.isEmpty()) {
                    interactionManager.clickSlot(
                            chestHandler.syncId,
                            slot.id,
                            0,
                            SlotActionType.QUICK_MOVE,
                            player
                    );
                }
            }
        }

    }

    public void _SWAP_BY_SOURCE_DEST() {
        int source = Typing._PARSE_INTEGER(this);
        int destination = Typing._PARSE_INTEGER(this);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ScreenHandler chestHandler = player.currentScreenHandler;
        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;

        if (chestHandler == null) return;

        System.out.println(chestHandler.slots.get(source).getStack().getItemName().getString());

        interactionManager.clickSlot(
                chestHandler.syncId,
                chestHandler.slots.get(source).id,
                destination,
                SlotActionType.SWAP,
                player
        );
    }

    public void _SWAP_BY_NAME_DEST_INCLUSIVE() {
        String name = Typing._PARSE_STRING(this);
        int destination = Typing._PARSE_INTEGER(this);
        int toInventoryOrInteractable = Typing._PARSE_INTEGER(this);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ScreenHandler chestHandler = player.currentScreenHandler;
        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;

        if (chestHandler == null) return;

        chestHandler.enableSyncing();

        int interactableSize = chestHandler.slots.size() - 36;

        if (toInventoryOrInteractable == 0) {
            for (int i = 0; i < interactableSize - 1; i++) {
                Slot slot = chestHandler.slots.get(i);
                if (slot.getStack().getItemName().getString().equals(name) || name.isEmpty()) {
                    interactionManager.clickSlot(
                            chestHandler.syncId,
                            slot.id,
                            destination,
                            SlotActionType.SWAP,
                            player
                    );
                    break;
                }
            }
        }

        if (toInventoryOrInteractable == 1) {
            for (int i = interactableSize; i < chestHandler.slots.size() - 1; i++) {
                Slot slot = chestHandler.slots.get(i);
                if (slot.getStack().getItemName().getString().equals(name) || name.isEmpty()) {
                    interactionManager.clickSlot(
                            chestHandler.syncId,
                            slot.id,
                            destination,
                            SlotActionType.SWAP,
                            player
                    );
                    break;
                }
            }
        }
    }

    public void _SWAP_BY_NAME_DEST_EXCLUSIVE() {
        String name = Typing._PARSE_STRING(this);
        int destination = Typing._PARSE_INTEGER(this);
        int toInventoryOrInteractable = Typing._PARSE_INTEGER(this);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ScreenHandler chestHandler = player.currentScreenHandler;
        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;

        if (chestHandler == null) return;

        chestHandler.enableSyncing();

        int interactableSize = chestHandler.slots.size() - 36;

        if (toInventoryOrInteractable == 0) {
            for (int i = 0; i < interactableSize - 1; i++) {
                Slot slot = chestHandler.slots.get(i);
                if (!slot.getStack().getItemName().getString().equals(name) || name.isEmpty()) {
                    interactionManager.clickSlot(
                            chestHandler.syncId,
                            slot.id,
                            destination,
                            SlotActionType.SWAP,
                            player
                    );
                    break;
                }
            }
        }

        if (toInventoryOrInteractable == 1) {
            for (int i = interactableSize; i < chestHandler.slots.size() - 1; i++) {
                Slot slot = chestHandler.slots.get(i);
                if (!slot.getStack().getItemName().getString().equals(name) || name.isEmpty()) {
                    interactionManager.clickSlot(
                            chestHandler.syncId,
                            slot.id,
                            destination,
                            SlotActionType.SWAP,
                            player
                    );
                    break;
                }
            }
        }
    }

    public void _DEBUG_INTERACTION_DATA() {

        System.out.println("is this being called?");

        ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;

        if (handler == null) return;

        System.out.println(handler.slots.size());

        int i = 0;
        for (Slot slot : handler.slots) {
            i++;
            System.out.println(i + ": " + slot.getStack().getItemName());
        }
    }

    public void _WAIT() {

        final int[] ticks = {Typing._PARSE_INTEGER(this)};

        this.addRequest(new Request(() -> {
            if (ticks[0] <= 0) return true;
            ticks[0]--;
            return false;
        }));

    }

    public void _PUT() {

        int register = Typing._PARSE_INTEGER(this);
        int value = Typing._PARSE_INTEGER(this);

        this.memory.put((byte) register, (byte) value);

    }

    public void _PUT_ALL() {

        int register = Typing._PARSE_INTEGER(this);

        ArrayList<Byte> body = Typing._GATHER_BODY(this, false);

        for (int i = 0; i < body.size(); i++) this.memory.put((byte) (register + i), body.get(i));

    }

    public void _PRINT_UTF_8() {
        String out = Typing._PARSE_STRING(this);
        System.out.println("stout: " + out);
    }

    public void _PRINT_RAW() {
        System.out.println(Typing._GATHER_BODY(this, false));
    }

    public void _PRINT_FLOAT() { System.out.println(Typing._PARSE_FLOAT(this)); }

    public void _EXECUTE() {

        CivKernel program = Typing._PARSE_FUNCTION(this);
        this.pushStack(program);

    }

    public void _RETURN() {

        int length = Typing._PARSE_INTEGER(this);

        this.exitValues = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            this.itter();
            this.exitValues.add(this.translateProgramPointer());
        }

        this.parentStackObject.breakStack(this.exitValues);

    }

    public void _CONDITIONAL() {

        ArrayList<Byte> value = Typing._GATHER_BODY(this, false);

        boolean out = true;
        for (int i = 0; i < value.size() && out; i++) out = value.get(i) == 0;
        //I could use streams but they're slower

        ArrayList<Byte> body = Typing._GATHER_BODY(this, false);
        if (out) this.pushStack(new CivKernel(body, new ArrayList<>(), this));

    }

    public void _INVERSE_CONDITIONAL() {

        ArrayList<Byte> value = Typing._GATHER_BODY(this, false);

        boolean out = true;
        for (int i = 0; i < value.size() && out; i++) out = value.get(i) != 0;
        //I could use streams but they're slower

        ArrayList<Byte> body = Typing._GATHER_BODY(this, false);
        if (out) this.pushStack(new CivKernel(body, new ArrayList<>(), this));

    }

    public void _GO_TO_POINT() {

        this.programPointer = Typing._PARSE_INTEGER(this);
        //risky business!

    }

    public void _OP_AND() {

        ArrayList<Byte> value1 = Typing._GATHER_BODY(this, false);
        ArrayList<Byte> value2 = Typing._GATHER_BODY(this, false);

        ArrayList<Byte> out = new ArrayList<>();

        if (value1.size() > value2.size()) {

            for (int i = 0; i < value1.size(); i++) {
                if (i < value2.size()) out.add((byte) (value1.get(i) & value2.get(i)));
                else                   out.add((byte) (0));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }

        else {

            for (int i = 0; i < value2.size(); i++) {
                if (i < value1.size()) out.add((byte) (value1.get(i) & value2.get(i)));
                else                   out.add((byte) (0));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }

    }

    public void _OP_OR() {
        ArrayList<Byte> value1 = Typing._GATHER_BODY(this, false);
        ArrayList<Byte> value2 = Typing._GATHER_BODY(this, false);

        ArrayList<Byte> out = new ArrayList<>();

        if (value1.size() > value2.size()) {

            for (int i = 0; i < value1.size(); i++) {
                if (i < value2.size()) out.add((byte) (value1.get(i) | value2.get(i)));
                else                   out.add(value1.get(i));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }

        else {

            for (int i = 0; i < value2.size(); i++) {
                if (i < value1.size()) out.add((byte) (value1.get(i) | value2.get(i)));
                else                   out.add(value2.get(i));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }

    }

    public void _OP_XOR() {
        ArrayList<Byte> value1 = Typing._GATHER_BODY(this, false);
        ArrayList<Byte> value2 = Typing._GATHER_BODY(this, false);

        ArrayList<Byte> out = new ArrayList<>();

        if (value1.size() > value2.size()) {

            for (int i = 0; i < value1.size(); i++) {
                if (i < value2.size()) out.add((byte) (value1.get(i) ^ value2.get(i)));
                else                   out.add(value1.get(i));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }

        else {

            for (int i = 0; i < value2.size(); i++) {
                if (i < value1.size()) out.add((byte) (value1.get(i) ^ value2.get(i)));
                else                   out.add(value2.get(i));
            }

            this.exitValues = Typing._ENCODE_STRING(out);
        }

    }

    public void _OP_INV() {

        ArrayList<Byte> value1 = Typing._GATHER_BODY(this, false);
        ArrayList<Byte> out = new ArrayList<>();

        for (byte b : value1) out.add((byte) ~b);

        this.exitValues = Typing._ENCODE_STRING(out);

    }

    public void _OP_SHIFT_LEFT() {

        ArrayList<Byte> value1 = Typing._GATHER_BODY(this, false);
        int places = Typing._PARSE_INTEGER(this);

        ArrayList<Byte> out = new ArrayList<>();

        for (byte b : value1) out.add((byte) (b << places));

        this.exitValues = Typing._ENCODE_STRING(out);

    }

    public void _OP_SHIFT_RIGHT() {

        ArrayList<Byte> value1 = Typing._GATHER_BODY(this, false);
        int places = Typing._PARSE_INTEGER(this);

        ArrayList<Byte> out = new ArrayList<>();

        for (byte b : value1) out.add((byte) (b >> places));

        this.exitValues = Typing._ENCODE_STRING(out);

    }

    public void _OP_ADD() {

        ArrayList<Byte> value1 = Typing._GATHER_BODY(this, false);
        ArrayList<Byte> value2 = Typing._GATHER_BODY(this, false);

        ArrayList<Byte> out = new ArrayList<>();

        if (value1.size() > value2.size()) {

            for (int i = 0; i < value1.size(); i++) {
                if (i < value2.size()) out.add((byte) (value1.get(i) + value2.get(i)));
                else                   out.add(value1.get(i));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }

        else {

            for (int i = 0; i < value2.size(); i++) {
                if (i < value1.size()) out.add((byte) (value1.get(i) + value2.get(i)));
                else                   out.add(value2.get(i));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }

    }

    public void _OP_SUB() {

        ArrayList<Byte> value1 = Typing._GATHER_BODY(this, false);
        ArrayList<Byte> value2 = Typing._GATHER_BODY(this, false);

        ArrayList<Byte> out = new ArrayList<>();

        if (value1.size() > value2.size()) {

            for (int i = 0; i < value1.size(); i++) {
                if (i < value2.size()) out.add((byte) (value1.get(i) - value2.get(i)));
                else                   out.add(value1.get(i));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }

        else {

            for (int i = 0; i < value2.size(); i++) {
                if (i < value1.size()) out.add((byte) (value1.get(i) - value2.get(i)));
                else                   out.add(value2.get(i));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }

    }

    public void _OP_MUL() {

        ArrayList<Byte> value1 = Typing._GATHER_BODY(this, false);
        ArrayList<Byte> value2 = Typing._GATHER_BODY(this, false);

        ArrayList<Byte> out = new ArrayList<>();

        if (value1.size() > value2.size()) {

            for (int i = 0; i < value1.size(); i++) {
                if (i < value2.size()) out.add((byte) (value1.get(i) * value2.get(i)));
                else                   out.add((byte) (0));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }

        else {

            for (int i = 0; i < value2.size(); i++) {
                if (i < value1.size()) out.add((byte) (value1.get(i) * value2.get(i)));
                else                   out.add((byte) (0));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }
    }

    public void _OP_DIV() {

        ArrayList<Byte> value1 = Typing._GATHER_BODY(this, false);
        ArrayList<Byte> value2 = Typing._GATHER_BODY(this, false);

        ArrayList<Byte> out = new ArrayList<>();

        if (value1.size() > value2.size()) {

            for (int i = 0; i < value1.size(); i++) {
                if (i < value2.size()) out.add((byte) (value1.get(i) / value2.get(i)));
                else                   out.add((byte) (0));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }

        else {

            for (int i = 0; i < value2.size(); i++) {
                if (i < value1.size()) out.add((byte) (value1.get(i) / value2.get(i)));
                else                   out.add((byte) (0));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }
    }

    public void _OP_MOD() {

        ArrayList<Byte> value1 = Typing._GATHER_BODY(this, false);
        ArrayList<Byte> value2 = Typing._GATHER_BODY(this, false);

        ArrayList<Byte> out = new ArrayList<>();

        if (value1.size() > value2.size()) {

            for (int i = 0; i < value1.size(); i++) {
                if (i < value2.size()) out.add((byte) (value1.get(i) % value2.get(i)));
                else                   out.add((byte) (0));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }

        else {

            for (int i = 0; i < value2.size(); i++) {
                if (i < value1.size()) out.add((byte) (value1.get(i) % value2.get(i)));
                else                   out.add((byte) (0));
            }

            this.exitValues = Typing._ENCODE_STRING(out);

        }
    }

    public void _GET_X() {

        assert MinecraftClient.getInstance().player != null;
        this.exitValues = Typing._ENCODE_FLOAT((float) MinecraftClient.getInstance().player.getX());

    }

    public void _GET_Y() {

        assert MinecraftClient.getInstance().player != null;
        this.exitValues = Typing._ENCODE_FLOAT((float) MinecraftClient.getInstance().player.getY());

    }

    public void _GET_Z() {

        assert MinecraftClient.getInstance().player != null;
        this.exitValues = Typing._ENCODE_FLOAT((float) MinecraftClient.getInstance().player.getZ());

    }

    public void _GET_XYZ() {

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;

        this.exitValues = Typing._ENCODE_XYZ(player.getLastRenderPos().toVector3f());

    }

    public void _GET_VX() {

        assert MinecraftClient.getInstance().player != null;
        this.exitValues = Typing._ENCODE_FLOAT((float) MinecraftClient.getInstance().player.getX() - Navigation.playerPrev.x);

    }

    public void _GET_VY() {

        assert MinecraftClient.getInstance().player != null;
        System.out.println(MinecraftClient.getInstance().player.getY() - Navigation.playerPrev.y);
        this.exitValues = Typing._ENCODE_FLOAT((float) MinecraftClient.getInstance().player.getY() - Navigation.playerPrev.y);

    }

    public void _GET_VZ() {

        assert MinecraftClient.getInstance().player != null;
        this.exitValues = Typing._ENCODE_FLOAT((float) MinecraftClient.getInstance().player.getZ() - Navigation.playerPrev.z);

    }

    public void _GET_VXYZ() {

        assert MinecraftClient.getInstance().player != null;
        this.exitValues = Typing._ENCODE_XYZ(MinecraftClient.getInstance().player.getVelocity().toVector3f());

    }

    public void _GET_PROGRAM_POINTER() {
        this.exitValues = Typing._ENCODE_INTEGER(this.programPointer);
    }

    public void _ATTACK_HOLD() {
        boolean enable = Typing._PARSE_INTEGER(this) == 0;
        MinecraftClient.getInstance().options.attackKey.setPressed(enable);
    }

    public void _USE_HOLD() {
        boolean enable = Typing._PARSE_INTEGER(this) == 0;
        MinecraftClient.getInstance().options.useKey.setPressed(enable);
    }

    public void _COUNT_ITEM_INVENTORY() {

        String itemName = Typing._PARSE_STRING(this);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        PlayerInventory inventory = player.getInventory();

        int total = 0;

        for (int i = 0; i < inventory.size(); i++) total += inventory.getStack(i).getCount();

        this.exitValues = Typing._ENCODE_INTEGER(total);

    }

    public void _COUNT_ITEM_INTERACTABLE() {

        String itemName = Typing._PARSE_STRING(this);

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ScreenHandler chestHandler = player.currentScreenHandler;
        ClientPlayerInteractionManager interactionManager = MinecraftClient.getInstance().interactionManager;

        if (chestHandler == null) return;

        int total = 0;
        int interactableSize = chestHandler.slots.size() - 36;

        for (int i = 0; i < interactableSize - 1; i++) total += chestHandler.slots.get(i).getStack().getCount();

    }

    public void _WRAP_TYPE() {

        this.exitValues = Typing._ENCODE_STRING(Typing._GATHER_BODY(this, true));

    }

    public CivKernel(ArrayList<Byte> program, ArrayList<Byte> arguments, BasicFunctions parent) {
        super(program, arguments, parent);

        this.registerInstruction((byte) 0x0, this::_CALIBRATE);

        this.registerInstruction((byte) 0x1, this::_MOVE_TO);
        this.registerInstruction((byte) 0x2, this::_PATH_TO);
        this.registerInstruction((byte) 0x2aa, this::_RESET_CONTROLS);

        this.registerInstruction((byte) 0x3, this::_SET_TARGET_NODE);
        this.registerInstruction((byte) 0x4, this::_SET_CURRENT_NODE);

        this.registerInstruction((byte) 0x5, this::_SET_BASIC_MOVEMENT_HANDLER);
        this.registerInstruction((byte) 0x6, this::_SET_DOOR_HANDLER);
        this.registerInstruction((byte) 0x7, this::_SET_ICEROAD_HANDLER);
        this.registerInstruction((byte) 0x8, this::_SET_INTERACTION_HANDLER);

        this.registerInstruction((byte) 0x9, this::_EXIT_INTERACTION);
        this.registerInstruction((byte) 0x10, this::_DEBUG_INTERACTION_DATA);
        this.registerInstruction((byte) 0x11, this::_QUICK_MOVE_BY_INDEX);
        this.registerInstruction((byte) 0x12, this::_QUICK_MOVE_BY_NAME_INCLUSIVE);
        this.registerInstruction((byte) 0x13, this::_QUICK_MOVE_BY_NAME_EXCLUSIVE);
        this.registerInstruction((byte) 0x14, this::_SWAP_BY_SOURCE_DEST);
        this.registerInstruction((byte) 0x15, this::_SWAP_BY_NAME_DEST_INCLUSIVE);
        this.registerInstruction((byte) 0x16, this::_SWAP_BY_NAME_DEST_EXCLUSIVE);

        this.registerInstruction((byte) 0x17, this::_WAIT);
        this.registerInstruction((byte) 0x18, this::_PUT);
        this.registerInstruction((byte) 0x19, this::_PUT_ALL);

        this.registerInstruction((byte) 0x20, this::_PRINT_UTF_8);
        this.registerInstruction((byte) 0x21, this::_PRINT_RAW);
        this.registerInstruction((byte) 0x22a, this::_PRINT_FLOAT);

        this.registerInstruction((byte) 0x22, this::_EXECUTE);
        this.registerInstruction((byte) 0x23, this::_RETURN);
        this.registerInstruction((byte) 0x24, this::_CONDITIONAL);
        this.registerInstruction((byte) 0x24a, this::_INVERSE_CONDITIONAL);

        this.registerInstruction((byte) 0x25, this::_GO_TO_POINT);

        this.registerInstruction((byte) 0x27, this::_OP_AND);
        this.registerInstruction((byte) 0x28, this::_OP_OR);
        this.registerInstruction((byte) 0x29, this::_OP_XOR);
        this.registerInstruction((byte) 0x30, this::_OP_INV);
        this.registerInstruction((byte) 0x31, this::_OP_SHIFT_LEFT);
        this.registerInstruction((byte) 0x32, this::_OP_SHIFT_RIGHT);

        this.registerInstruction((byte) 0x33, this::_OP_ADD);
        this.registerInstruction((byte) 0x34, this::_OP_SUB);
        this.registerInstruction((byte) 0x35, this::_OP_MUL);
        this.registerInstruction((byte) 0x36, this::_OP_DIV);
        this.registerInstruction((byte) 0x37, this::_OP_MOD);

        this.registerInstruction((byte) 0x38, this::_GET_X);
        this.registerInstruction((byte) 0x39, this::_GET_Y);
        this.registerInstruction((byte) 0x40, this::_GET_Z);
        this.registerInstruction((byte) 0x41, this::_GET_XYZ);
        this.registerInstruction((byte) 0x42, this::_GET_VX);
        this.registerInstruction((byte) 0x43, this::_GET_VY);
        this.registerInstruction((byte) 0x44, this::_GET_VZ);
        this.registerInstruction((byte) 0x45, this::_GET_VXYZ);

        this.registerInstruction((byte) 0x46, this::_GET_PROGRAM_POINTER);

        this.registerInstruction((byte) 0x47, this::_COUNT_ITEM_INVENTORY);
        this.registerInstruction((byte) 0x48, this::_COUNT_ITEM_INTERACTABLE);

        this.registerInstruction((byte) 0x49, this::_ATTACK_HOLD);
        this.registerInstruction((byte) 0x50, this::_USE_HOLD);

        this.registerInstruction((byte) 0x51, this::_WRAP_TYPE);

    }
}

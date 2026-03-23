package net.altosheeve.tracking.client.Kernel;

import net.altosheeve.tracking.client.ChASM.ExtendableCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class TerminalKernel extends BasicFunctions {

    public ExtendableCompiler implementation;
    public static ExtendableCompiler terminalImplementation;

    public static void loadImplementation() {

        File extention = new File("sh.chasm");
        Scanner extentionReader;

        try                             { extentionReader = new Scanner(extention);}
        catch (FileNotFoundException e) { throw new RuntimeException(e); }

        StringBuilder extentionData = new StringBuilder();
        while (extentionReader.hasNextLine()) extentionData.append(extentionReader.nextLine());
        extentionReader.close();

        try                 { terminalImplementation = new ExtendableCompiler(extentionData.toString().toCharArray()); }
        catch (Exception e) { throw new RuntimeException(e); }

    }

    public TerminalKernel(ArrayList<Byte> program, ArrayList<Byte> arguments, BasicFunctions parent) {

        super(program, arguments, parent);

        this.registerInstruction((byte) 0x0, this::_LOAD_IMPLEMENTATION);
        this.registerInstruction((byte) 0x1, this::_RUN);
        this.registerInstruction((byte) 0x2, this::_ECHO);
        this.registerInstruction((byte) 0x3, this::_LIST_NAVIGATION);
        this.registerInstruction((byte) 0x4, this::_CALIBRATE);

    }

    public void _LOAD_IMPLEMENTATION() throws Exception {

        String target = Typing._PARSE_STRING(this);

        File extention = new File("/implementations/" + target + ".chasm");
        Scanner extentionReader = new Scanner(extention);
        StringBuilder extentionData = new StringBuilder();
        while (extentionReader.hasNextLine()) extentionData.append(extentionReader.nextLine());
        extentionReader.close();

        this.implementation = new ExtendableCompiler(extentionData.toString().toCharArray());

    }

    public void _RUN() throws Exception {

        String target = Typing._PARSE_STRING(this);

        File program = new File("/programs/" + target);
        Scanner programReader = new Scanner(program);
        StringBuilder programData = new StringBuilder();
        while (programReader.hasNextLine()) programData.append(programReader.nextLine());
        programReader.close();

        Execution.setProgram(this.implementation.runCompiler(programData.toString()));

    }

    public void _ECHO() throws Exception {
        TerminalScreen.lines.add(Typing._PARSE_STRING(this));
    }

    public void _LIST_NAVIGATION() throws Exception {

        ArrayList<char[]> identifiers = new ArrayList<>();
        identifiers.add(new char[]{' '});

        char[] tag = Typing._PARSE_STRING(this).toCharArray();

        ArrayList<Node> listNodes = new ArrayList<>();

        for (Node node : Navigation.nodes) {

            ArrayList<char[]> tags = ExtendableCompiler.tokenize(identifiers, node.tag.toCharArray());

            for (char[] nodeTag : tags) {

                if (Arrays.equals(nodeTag, tag)) listNodes.add(node);

            }

        }

        class key implements Comparator {
            @Override
            public int compare(Object o1, Object o2) {

                Node node1 = (Node) o1;
                Node node2 = (Node) o2;

                Vec3d node1Pos = new Vec3d(node1.x, node1.y, node1.z);
                Vec3d node2Pos = new Vec3d(node2.x, node2.y, node2.z);

                Vec3d cameraPos = Rendering.client.cameraEntity.getPos();

                double node1Dist = cameraPos.distanceTo(node1Pos);
                double node2Dist = cameraPos.distanceTo(node2Pos);

                if      (node1Dist > node2Dist) return 1;
                else if (node1Dist == node2Dist) return 0;
                return -1;
            }
        }

        listNodes.sort(new key());

        TerminalScreen.lines.add("Nodes with tags that match:" + new String(tag));
        TerminalScreen.lines.add("(Sorted closest to farthest)");

        for (Node node : listNodes) {

            Vec3d nodePos   = new Vec3d(node.x, node.y, node.z);
            Vec3d cameraPos = Rendering.client.cameraEntity.getPos();

            double dist = cameraPos.distanceTo(nodePos);

            TerminalScreen.lines.add(node.tag + " (Distance: " + dist + "m)");

        }

    }

    public void _CALIBRATE() throws Exception {

        String tag = Typing._PARSE_STRING(this);

        ArrayList<Byte> civProgram = new ArrayList<>();

        civProgram.add((byte) 0x0); //calibrate operation

        //encode static tag
        civProgram.add((byte) Typing.STATIC_EXPRESSION);
        civProgram.addAll(Typing._ENCODE_STRING(tag));

        //encode static tolerance value
        civProgram.add((byte) Typing.STATIC_EXPRESSION);
        civProgram.addAll(Typing._ENCODE_FLOAT(0.7f));

        Execution.setProgram(civProgram);

    }

    public void runCommand(String command) throws Exception {

        this.clearInstructions();
        this.addInstructions(terminalImplementation.runCompiler(command));
        Execution.setKernel(this);

    }

}

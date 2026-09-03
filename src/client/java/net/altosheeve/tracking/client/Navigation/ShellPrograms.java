package net.altosheeve.tracking.client.Navigation;

import net.altosheeve.tracking.client.Soprano.Typing;

import java.util.ArrayList;

public class ShellPrograms {

    public static ArrayList<Byte> calibrate(Node target) {

        ArrayList<Byte> out = new ArrayList<>();

        //calibrate operation
        out.add((byte) 0x0);

        //encode target node
        out.add((byte) Typing.STATIC_EXPRESSION);
        out.addAll(Typing._ENCODE_STRING(target.tag));

        //encode calibration tolerance
        out.add((byte) Typing.STATIC_EXPRESSION);
        out.addAll(Typing._ENCODE_FLOAT(0.02f));

        return out;

    }

    public static ArrayList<Byte> pathTo(Node target) {

        ArrayList<Byte> out = new ArrayList<>();

        //path_to operation
        out.add((byte) 0x2);

        //encode target node
        out.add((byte) Typing.STATIC_EXPRESSION);
        out.addAll(Typing._ENCODE_STRING(target.tag));

        //encode node tolerance
        out.add((byte) Typing.STATIC_EXPRESSION);
        out.addAll(Typing._ENCODE_FLOAT(0.6f));

        return out;

    }

    public static ArrayList<Byte> gather(String item) {

        ArrayList<Byte> out = new ArrayList<>();

        return out;

    }

    public static ArrayList<Byte> gatherFrom(Node target, String item) {

        ArrayList<Byte> out = new ArrayList<>();

        return out;

    }

    public static ArrayList<Byte> store(Node target, String item) {

        ArrayList<Byte> out = new ArrayList<>();

        return out;

    }

    public static ArrayList<Byte> storeAll(Node target) {

        ArrayList<Byte> out = new ArrayList<>();

        return out;

    }

}

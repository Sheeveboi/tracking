package net.altosheeve.tracking.client.Soprano;

import net.altosheeve.tracking.client.Soprano.Async.Thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Execution {

    private static boolean run = true;
    private static Map<Integer, Thread> threads = new HashMap<>();

    public static void setProgram(ArrayList<Byte> program) {

        threads.put(threads.size(), new Thread(
                new CivKernel(
                        program,
                        new ArrayList<>(),
                        null
                )
        ));

    }

    public static void setKernel(BasicFunctions kernel) {

        threads.put(threads.size(), new Thread(kernel));

    }

    public static void blockThread(int id) {
        threads.get(id).block = true;
    }

    public static void unblockThread(int id) {
        threads.get(id).block = false;
    }

    public static void lockThread(int target) {

        for (int id : threads.keySet())
            if (id != target) threads.get(id).block = true;

    }

    public static void unlockThread(int target) {

        for (int id : threads.keySet())
            if (id != target) threads.get(id).block = false;

    }

    public static void toggle() { run = !run; }

    public static boolean execute() throws Exception {

        if (run) {

            for (int id : threads.keySet())
                if (threads.get(id).iterateInstructionSet()) threads.remove(id);

        }

        return threads.isEmpty();

    }
}

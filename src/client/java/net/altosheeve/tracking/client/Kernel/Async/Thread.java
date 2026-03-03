package net.altosheeve.tracking.client.Kernel.Async;

import net.altosheeve.tracking.client.Kernel.BasicFunctions;

public class Thread {

    public boolean block = false;
    private final BasicFunctions instructionSet;

    public Thread(BasicFunctions instructionSet) {
        this.instructionSet = instructionSet;
    }

    public boolean iterateInstructionSet() {
        if (!block) instructionSet.run();
        return instructionSet.finished();
    }

}

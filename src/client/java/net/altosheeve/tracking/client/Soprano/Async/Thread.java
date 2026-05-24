package net.altosheeve.tracking.client.Soprano.Async;

import net.altosheeve.tracking.client.Soprano.BasicFunctions;

public class Thread {

    public boolean block = false;
    private final BasicFunctions instructionSet;

    public Thread(BasicFunctions instructionSet) {
        this.instructionSet = instructionSet;
    }

    public boolean iterateInstructionSet() throws Exception {
        if (!block) instructionSet.run();
        return instructionSet.finished();
    }

}

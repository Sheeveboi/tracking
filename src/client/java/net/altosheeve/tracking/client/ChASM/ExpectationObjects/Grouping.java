package net.altosheeve.tracking.client.ChASM.ExpectationObjects;

import java.util.ArrayList;

public class Grouping extends net.altofeather.ChASM.ExpectationObjects.Expectation {

    //we don't imply any extensionals here because a grouping could imply multiple extensionals
    //our target extensional is defined in check()
    public Grouping(char[] name, boolean gathered) {
        super(name, gathered);
    }

    @Override
    public boolean check(char[] programToken) throws Exception {
        return false;
    }

    @Override
    public int assignParameters(ArrayList<char[]> fullContext, int location) {
        return 0;
    }

}

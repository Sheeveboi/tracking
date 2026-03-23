package net.altosheeve.tracking.client.ChASM.StandardCompiler.ExpectationObjects;

import net.altosheeve.tracking.client.ChASM.ExtendableCompiler;

import java.util.ArrayList;
import java.util.Arrays;

public class Extensional extends Expectation {

    public Extensional(char[] name, boolean gathered) {
        super(name, gathered);
        this.extensional = ExtendableCompiler.getExtension(name);
    }

    @Override
    public boolean check(char[] programToken) throws Exception {

        boolean out = Arrays.equals(programToken, this.name);

        if (out && !gathered) this.extensional.runOperation();
        return out;

    }

    @Override
    public int assignParameters(ArrayList<char[]> fullContext, int location) {
        return 0;
    }
}

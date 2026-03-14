package net.altosheeve.tracking.client.ChASM.ExpectationObjects;

import net.altosheeve.tracking.client.ChASM.StackObject;

import java.util.ArrayList;
import java.util.Arrays;

import static net.altosheeve.tracking.client.ChASM.ExtendableCompiler.*;

public abstract class Expectation {

    public char[] name;
    StackObject extensional;
    public boolean gathered;
    public boolean action = false;

    public Expectation(char[] name,  boolean gathered) {
        this.name = name;
        this.gathered = gathered;
    }

    public static Expectation generateExpectation(char[] token, boolean gathered) {

        if      (getExtension(token)         != null) return new Extensional         (token, gathered);
        else if (getAbstractExtension(token) != null) return new AbstractExtensional (token, gathered);
        else if (getExtensionalGroup(token)  != null) return new Grouping            (token, gathered);

        else if (Arrays.equals(token, "CAPTURE".toCharArray())) return new net.altosheeve.tracking.client.ChASM.ExpectationObjects.Capture(token, gathered);

        return null;

    }

    public abstract boolean check(char[] programToken) throws Exception;
    public abstract int assignParameters(ArrayList<char[]> fullContext, int location);

}

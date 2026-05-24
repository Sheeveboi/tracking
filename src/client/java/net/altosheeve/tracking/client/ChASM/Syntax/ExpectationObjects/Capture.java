package net.altosheeve.tracking.client.ChASM.Syntax.ExpectationObjects;

import net.altosheeve.tracking.client.ChASM.ExtendableCompiler;
import net.altosheeve.tracking.client.ChASM.StackObject;

import java.util.ArrayList;
import java.util.Arrays;

public class Capture extends Expectation {

    public char[] bodyName;
    public char[] openingPattern;
    public char[] closingPattern;

    public Capture(char[] name, boolean gathered) {

        super(name, gathered);
        this.action = true;

        this.extensional = ExtendableCompiler.getCurrentStackObject();

    }

    @Override
    public int assignParameters(ArrayList<char[]> fullContext, int location) {

        this.bodyName = fullContext.get(location + 1);
        this.openingPattern = fullContext.get(location + 2);
        this.closingPattern = fullContext.get(location + 3);

        if (Arrays.equals(this.bodyName, "self".toCharArray())) throw new RuntimeException("body name 'self' is a reserved namespace");

        System.out.println(this.bodyName);
        System.out.println(this.openingPattern);
        System.out.println(this.closingPattern);

        if (ExtendableCompiler.getExtension(this.openingPattern) == null)
            ExtendableCompiler.extensions.put(openingPattern, new StackObject(() -> true, this.openingPattern, "Opening capture token"));

        if (ExtendableCompiler.getExtension(this.closingPattern) == null)
            ExtendableCompiler.extensions.put(this.closingPattern, new StackObject(() -> true, this.openingPattern, "Closing capture token"));

        return 3;

    }

    @Override
    public boolean check(char[] programToken) throws Exception {

        boolean out = Arrays.equals(programToken, this.openingPattern);

        int openers = 0;

        this.extensional.capturedBodies.put(this.bodyName, new ArrayList<>());

        while (ExtendableCompiler.programPointer < ExtendableCompiler.tokenizedProgram.size()) {

            ExtendableCompiler.programPointer ++;
            char[] token = ExtendableCompiler.tokenizedProgram.get(ExtendableCompiler.programPointer);

            if (Arrays.equals(token, this.closingPattern) && openers == 0) {
                out = true;
                break;
            }

            else if (Arrays.equals(token, this.closingPattern)) openers --;

            else if (Arrays.equals(token, this.openingPattern)) openers ++;

            this.extensional.capturedBodies.get(this.bodyName).add(token);
        }

        System.out.println(this.extensional.capturedBodies.get(this.bodyName));

        return out;
    }
}

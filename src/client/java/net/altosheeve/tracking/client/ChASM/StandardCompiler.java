package net.altosheeve.tracking.client.ChASM;

import net.altosheeve.tracking.client.ChASM.ExpectationObjects.Expectation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class StandardCompiler extends ExtendableCompiler {

    public StandardCompiler(char[] program) throws Exception {
        super(program);
    }

    public interface StackEdition {
        void cb() throws Exception;
    }

    protected static void _EXTRACT() throws Exception {

        if (!abstractExtension) throw new Exception("Cannot extract substring from non-abstract extensional.");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        StackObject currentStackObject = getCurrentStackObject();
        String pattern = new String(currentToken);

        currentStackObject.pushStack(() -> {

            //System.out.println(currentStackObject.selfValue);

            currentStackObject.selfValue = currentStackObject.selfValue.replaceAll(pattern, "");

            return true;

        }, extendingToken, "EXTRACT");
    }

    protected static void _POSITIVE_SYNTAX() throws Exception {

        if (!abstractExtension) throw new Exception("Cannot apply syntax to non-abstract extensional.");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        getCurrentStackObject().positiveSyntax = Pattern.compile(new String(currentToken));

    }

    protected static void _NEGATIVE_SYNTAX() throws Exception {

        if (!abstractExtension) throw new Exception("Cannot apply syntax to non-abstract extensional.");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        getCurrentStackObject().negativeSyntax = Pattern.compile(new String(currentToken));

    }

    protected static void _EXPECT() throws Exception {

        ArrayList<char[]> expectationTokens = new ArrayList<>();

        //gather raw tokens here
        for (tokenPointer++; tokenPointer < compilerTokens.size(); tokenPointer++) {

            char[] token = compilerTokens.get(tokenPointer);

            if (Arrays.equals(token, ";".toCharArray())) break;

            if (!new String(token).replace(" ", "").isEmpty()) expectationTokens.add(token);

        }

        StackObject currentStackObject = getCurrentStackObject();

        //parse and build expectations
        for (int expectationIndex = 0; expectationIndex < expectationTokens.size(); expectationIndex++) {

            char[] expectation = expectationTokens.get(expectationIndex);

            Expectation fullExpectation = Expectation.generateExpectation(expectation, false);

            if (fullExpectation == null) throw new Exception("Unexpected expectation token");

            if (fullExpectation.action) expectationIndex += fullExpectation.assignParameters(expectationTokens, expectationIndex);


            currentStackObject.expectations.add(fullExpectation);

        }

        currentStackObject.pushStack(() -> {

            for (int expectationIndex = 0; expectationIndex < currentStackObject.expectations.size(); expectationIndex++) {

                programPointer++;
                Expectation expectation = currentStackObject.expectations.get(expectationIndex);

                if (programPointer >= tokenizedProgram.size()) throw new Exception("Syntax Error: Unexpected end of program");

                char[] programToken = tokenizedProgram.get(programPointer);

                //System.out.println(STR."checking \{new String(programToken)} (expecting \{new String(expectation.name)})");

                if (!expectation.check(programToken)) throw new Exception("Syntax Error: Unexpected Token");

            }

            return true;
        }, extendingToken, "EXPECT");

    }

    protected static void _EXTEND() throws Exception {

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        extendingToken = currentToken;
        abstractExtension = false;

        if (extensions.containsKey(currentToken)) throw new Exception("Extensional with name '" + new String(currentToken) + "' already exists.");

        if (abstractExtensions.containsKey(currentToken)) throw new Exception("Abstract Extensional with name '" + new String(currentToken) + "' already exists.");

        extensions.put(currentToken, new StackObject(() -> true, extendingToken.clone(), "EXTEND"));

    }

    protected static void _ABSTRACT_EXTEND() throws Exception {

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        extendingToken = currentToken;
        abstractExtension = true;

        if (extensions.containsKey(currentToken)) throw new Exception("Extensional with name '" + new String(currentToken) + "' already exists.");

        if (abstractExtensions.containsKey(currentToken)) throw new Exception("Abstract Extensional with name '" + new String(currentToken) + "' already exists.");

        abstractExtensions.put(currentToken, new StackObject(() -> true, extendingToken.clone(), "ABSTRACT EXTEND"));

    }

    protected static void _IMPLY() throws Exception {

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        StackObject currentStackObject = getCurrentStackObject();
        StackObject abstractStackObject = null;

        for (char[] key : abstractExtensions.keySet())
            if (Arrays.equals(key, currentToken))
                abstractStackObject = abstractExtensions.get(key);

        if (abstractStackObject == null) throw new Exception("Could not find abstract extensional.");

        for (char[] implication : currentStackObject.implications)
            if (Arrays.equals(implication, currentToken))
                throw new Exception("Abstract extension may only be implied once per extension");

        currentStackObject.pushStack(abstractStackObject);
        currentStackObject.implications.add(currentToken);

    }

    protected static void _AS() throws Exception {

        if (abstractExtension) throw new Exception("Cannot group abstract extensionals to other abstract extensionals.");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        if (!abstractExtensions.containsKey(currentToken)) throw new Exception("Could not find abstract extensional.");

        if (!abstractGroups.containsKey(currentToken)) abstractGroups.put(currentToken, new ArrayList<>());
        abstractGroups.get(currentToken).add(extendingToken);

    }

    protected static void _INSERT_FLOAT() throws Exception {

        //System.out.println(STR."\{new String(extendingToken)} will insert a float");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        StackObject currentStackObject = getCurrentStackObject();

        final String[] value = {new String(currentToken)};

        boolean self = value[0].equals("SELF");
        if (!abstractExtension && self) throw new Exception("SELF may not be referenced in non-abstract extensional.");

        currentStackObject.pushStack(() -> {

            ArrayList<Byte> out = new ArrayList<>();

            //encode value
            if (self) value[0] = currentStackObject.selfValue;
            int intBits = Float.floatToIntBits(Float.parseFloat(value[0]));

            out.add((byte) (intBits >> 24));
            out.add((byte) (intBits >> 16));
            out.add((byte) (intBits >> 8));
            out.add((byte) (intBits));

            compiledBytecode.addAll(out);

            return true;
        }, extendingToken, "INSERT_NUMBER");

    }

    protected static void _INSERT_INTEGER() throws Exception {

        //System.out.println(STR."\{new String(extendingToken)} will insert an integer");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        StackObject currentStackObject = getCurrentStackObject();

        final String[] value = {new String(currentToken)};

        boolean self = value[0].equals("SELF");
        if (!abstractExtension && self) throw new Exception("SELF may not be referenced in non-abstract extensional.");

        currentStackObject.pushStack(() -> {
            if (self) value[0] = currentStackObject.selfValue;
            compiledBytecode.add((byte) Integer.parseInt(value[0]));
            return true;
        }, extendingToken, "INSERT_INTEGER");

    }

    protected static void _INSERT_HEX() throws Exception {

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        StackObject currentStackObject = getCurrentStackObject();

        final String[] value = {new String(currentToken)};

        boolean self = value[0].equals("SELF");
        if (!abstractExtension && self) throw new Exception("SELF may not be referenced in non-abstract extensional.");

        currentStackObject.pushStack(() -> {
            if (self) value[0] = currentStackObject.selfValue;
            compiledBytecode.add((byte) Integer.parseInt(value[0], 16));
            return true;
        }, extendingToken, "INSERT_HEX");

    }

    protected static void _INSERT_UTF_8() throws Exception {

        //System.out.println("inserting utf8");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        StackObject currentStackObject = getCurrentStackObject();

        final String[] value = {new String(currentToken)};

        boolean self = value[0].equals("SELF");
        if (!abstractExtension && self) throw new Exception("SELF may not be referenced in non-abstract extensional.");

        currentStackObject.pushStack(() -> {
            if (self) value[0] = currentStackObject.selfValue;
            for (char c : value[0].toCharArray()) compiledBytecode.add((byte) c);
            return true;
        }, extendingToken, "INSERT_UTF_8");


    }

    protected static void _PRINT() { //should only be used for debugging. does not actually compile anything
        String sToken = "";
        StringBuilder out = new StringBuilder();
        while (tokenPointer < compilerTokens.size() - 1) {
            tokenPointer++;
            currentToken = compilerTokens.get(tokenPointer);
            sToken = new String(currentToken);
            if (Arrays.equals(currentToken, ";".toCharArray())) break;
            out.append(sToken);
            out.append(" ");
        }
        //System.out.println(out);
    }

    protected static void _PROGRAM_EXTENSION_NAME() {

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        programExtension = new String(currentToken);

    }

    protected static void registerImplementation(String token, StackEdition edition) {
        operationMap.put(token.toCharArray(), edition);
    }
}

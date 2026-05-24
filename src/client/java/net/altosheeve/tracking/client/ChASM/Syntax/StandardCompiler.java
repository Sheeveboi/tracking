package net.altosheeve.tracking.client.ChASM.Syntax;

import net.altosheeve.tracking.client.ChASM.Syntax.ExpectationObjects.Expectation;
import net.altosheeve.tracking.client.ChASM.ExtendableCompiler;
import net.altosheeve.tracking.client.ChASM.StackObject;
import net.altosheeve.tracking.client.ChASM.TreeObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class StandardCompiler extends ExtendableCompiler {

    public StandardCompiler(char[] program) throws Exception {

        root.registerNewTreeSegment(";", new TreeObject());

        //TODO: change these over to fit new class based syntax tree structure
        /*root.registerNewTreeSegment("EXTEND",                  StandardCompiler::_EXPECT);
        root.registerNewTreeSegment("ABSTRACT EXTEND",         StandardCompiler::_ABSTRACT_EXTEND);
        root.registerNewTreeSegment("EXTEND",                  StandardCompiler::_EXTEND);
        root.registerNewTreeSegment("IMPLY",                   StandardCompiler::_IMPLY);
        root.registerNewTreeSegment("AS",                      StandardCompiler::_AS);
        root.registerNewTreeSegment("INSERT FLOAT",            StandardCompiler::_INSERT_FLOAT);
        root.registerNewTreeSegment("INSERT INTEGER",          StandardCompiler::_INSERT_INTEGER);
        root.registerNewTreeSegment("INSERT HEX",              StandardCompiler::_INSERT_HEX);
        root.registerNewTreeSegment("INSERT UTF_8",            StandardCompiler::_INSERT_UTF_8);
        root.registerNewTreeSegment("PRINT",                   StandardCompiler::_PRINT);
        root.registerNewTreeSegment("PROGRAM EXTENSION NAME:", StandardCompiler::_PROGRAM_EXTENSION_NAME);
        root.registerNewTreeSegment("POSITIVE SYNTAX",         StandardCompiler::_POSITIVE_SYNTAX);
        root.registerNewTreeSegment("NEGATIVE SYNTAX",         StandardCompiler::_NEGATIVE_SYNTAX);
        root.registerNewTreeSegment("EXTRACT",                 StandardCompiler::_EXTRACT);*/

        buildCompiler(program);

    }

    public interface StackEdition {
        void cb() throws Exception;
    }

    protected static void _EXTRACT(ArrayList<char[]> program, int position) throws Exception {

        if (!abstractExtension) throw new Exception("Cannot extract substring from non-abstract extensional.");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        StackObject currentStackObject = getCurrentStackObject();
        String pattern = new String(currentToken);

        currentStackObject.pushStack(() -> {

            System.out.println(currentStackObject.selfValue);

            currentStackObject.selfValue = currentStackObject.selfValue.replaceAll(pattern, "");

            return true;

        }, extendingToken, "EXTRACT");
    }

    protected static void _POSITIVE_SYNTAX(ArrayList<char[]> program, int position) throws Exception {

        if (!abstractExtension) throw new Exception("Cannot apply syntax to non-abstract extensional.");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        getCurrentStackObject().positiveSyntax = Pattern.compile(new String(currentToken));

    }

    protected static void _NEGATIVE_SYNTAX(ArrayList<char[]> program, int position) throws Exception {

        if (!abstractExtension) throw new Exception("Cannot apply syntax to non-abstract extensional.");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        getCurrentStackObject().negativeSyntax = Pattern.compile(new String(currentToken));

    }

    protected static void _EXPECT(ArrayList<char[]> program, int position) throws Exception {

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

            System.out.println("expecting");
            System.out.println(currentStackObject.token);
            System.out.println(currentStackObject.operationName);

            for (int expectationIndex = 0; expectationIndex < currentStackObject.expectations.size(); expectationIndex++) {

                programPointer++;
                Expectation expectation = currentStackObject.expectations.get(expectationIndex);

                System.out.println(programPointer);
                System.out.println(tokenizedProgram.size());
                System.out.println(currentStackObject.expectations.size());

                if (programPointer >= tokenizedProgram.size()) throw new Exception("Syntax Error: Unexpected end of program");

                char[] programToken = tokenizedProgram.get(programPointer);

                if (!expectation.check(programToken)) throw new Exception("Syntax Error: Unexpected Token");

            }

            return true;
        }, extendingToken, "EXTEND");

    }

    protected static void _EXTEND(ArrayList<char[]> program, int position) throws Exception {

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        extendingToken = currentToken;
        abstractExtension = false;

        if (extensions.containsKey(currentToken)) throw new Exception("Extensional with name '" + new String(currentToken) + "' already exists.");

        if (abstractExtensions.containsKey(currentToken)) throw new Exception("Abstract Extensional with name '" + new String(currentToken) + "' already exists.");

        extensions.put(currentToken, new StackObject(() -> true, extendingToken.clone(), "EXTEND"));

    }

    protected static void _ABSTRACT_EXTEND(ArrayList<char[]> program, int position) throws Exception {

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        extendingToken = currentToken;
        abstractExtension = true;

        if (extensions.containsKey(currentToken)) throw new Exception("Extensional with name '" + new String(currentToken) + "' already exists.");

        if (abstractExtensions.containsKey(currentToken)) throw new Exception("Abstract Extensional with name '" + new String(currentToken) + "' already exists.");

        abstractExtensions.put(currentToken, new StackObject(() -> true, extendingToken.clone(), "ABSTRACT EXTEND"));

    }

    protected static void _IMPLY(ArrayList<char[]> program, int position) throws Exception {

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        StackObject currentStackObject  = getCurrentStackObject();
        StackObject abstractStackObject = getAbstractExtension(currentToken);

        if (abstractStackObject == null) throw new Exception("Could not find abstract extensional.");

        System.out.println(new String(currentStackObject.token) + " will imply " + abstractStackObject);

        currentStackObject.pushStack((StackObject) abstractStackObject.clone());

    }

    protected static void _AS(ArrayList<char[]> program, int position) throws Exception {

        if (abstractExtension) throw new Exception("Cannot group abstract extensionals to other abstract extensionals.");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        if (!abstractExtensions.containsKey(currentToken)) throw new Exception("Could not find abstract extensional.");

        if (!abstractGroups.containsKey(currentToken)) abstractGroups.put(currentToken, new ArrayList<>());
        abstractGroups.get(currentToken).add(extendingToken);

    }

    protected static void _INSERT_FLOAT(ArrayList<char[]> program, int position) throws Exception {

        System.out.println(new String(extendingToken) + " will insert a float");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        StackObject currentStackObject = getCurrentStackObject();

        final String[] staticValue = {new String(currentToken)};

        //TODO: this method of gathering the length is rather clunky. If more attributes of tokens are necessary in the future then I will implement a more complete self-information gathering system
        boolean selfValue  = new String(currentToken).equals("SELF");
        boolean selfLength = new String(currentToken).equals("SELF.LENGTH");

        if (!abstractExtension && (selfValue || selfLength)) throw new Exception("SELF may not be referenced in non-abstract extensional.");

        currentStackObject.pushStack(() -> {

            ArrayList<Byte> out = new ArrayList<>();
            int intBits;

            //encode value
            if      (selfValue)  intBits = Float.floatToIntBits(Float.parseFloat(currentStackObject.selfValue));
            else if (selfLength) intBits = Float.floatToIntBits((float) currentStackObject.selfValue.length());
            else                 intBits = Float.floatToIntBits(Float.parseFloat(staticValue[0]));

            out.add((byte) (intBits >> 24));
            out.add((byte) (intBits >> 16));
            out.add((byte) (intBits >> 8));
            out.add((byte) (intBits));

            compiledBytecode.addAll(out);

            System.out.println(compiledBytecode);

            return true;
        }, extendingToken, "INSERT_NUMBER");

    }

    protected static void _INSERT_INTEGER(ArrayList<char[]> program, int position) throws Exception {

        System.out.println(new String(extendingToken) + " will insert an integer");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        StackObject currentStackObject = getCurrentStackObject();

        final String[] staticValue = {new String(currentToken)};

        boolean selfValue  = new String(currentToken).equals("SELF");
        boolean selfLength = new String(currentToken).equals("SELF.LENGTH");

        if (!abstractExtension && (selfValue || selfLength)) throw new Exception("SELF may not be referenced in non-abstract extensional.");

        currentStackObject.pushStack(() -> {

            System.out.println("Static value: " + staticValue[0]);

            if      (selfValue)  compiledBytecode.add((byte) Integer.parseInt(currentStackObject.selfValue));
            else if (selfLength) compiledBytecode.add((byte) currentStackObject.selfValue.length());
            else                 compiledBytecode.add((byte) Integer.parseInt(staticValue[0]));

            System.out.println(compiledBytecode);

            return true;

        }, extendingToken, "INSERT_INTEGER");

    }

    protected static void _INSERT_HEX(ArrayList<char[]> program, int position) throws Exception {

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        StackObject currentStackObject = getCurrentStackObject();

        final String[] staticValue = {new String(currentToken)};

        boolean selfValue  = new String(currentToken).equals("SELF");
        boolean selfLength = new String(currentToken).equals("SELF.LENGTH");

        if (!abstractExtension && (selfValue || selfLength)) throw new Exception("SELF may not be referenced in non-abstract extensional.");

        currentStackObject.pushStack(() -> {

            String value = "";

            if      (selfValue)  value = currentStackObject.selfValue;
            else if (selfLength) value = String.valueOf(currentStackObject.selfValue.length());
            else                 value = staticValue[0];

            compiledBytecode.add((byte) Integer.parseInt(value, 16));

            System.out.println(compiledBytecode);

            return true;

        }, extendingToken, "INSERT_HEX");

    }

    protected static void _INSERT_UTF_8(ArrayList<char[]> program, int position) throws Exception {

        System.out.println(new String(extendingToken) + " will insert utf 8 bytes");

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

        StackObject currentStackObject = getCurrentStackObject();

        final String[] staticValue = {new String(currentToken)};

        boolean selfValue  = new String(currentToken).equals("SELF");
        boolean selfLength = new String(currentToken).equals("SELF.LENGTH");

        if (!abstractExtension && (selfValue || selfLength)) throw new Exception("SELF may not be referenced in non-abstract extensional.");

        currentStackObject.pushStack(() -> {

            String value;

            if      (selfValue)  value = currentStackObject.selfValue;
            else if (selfLength) value = String.valueOf(currentStackObject.selfValue.length());
            else                 value = staticValue[0];

            for (char c : value.toCharArray()) compiledBytecode.add((byte) c);

            System.out.println(compiledBytecode);

            return true;

        }, extendingToken, "INSERT_UTF_8");


    }

    protected static void _PRINT(ArrayList<char[]> program, int position) { //should only be used for debugging. does not actually compile anything
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
        System.out.println(out);
    }

    protected static void _PROGRAM_EXTENSION_NAME(ArrayList<char[]> program, int position) {

        getNextCompilerToken();
        System.out.println("setting program extension name");

        programExtension = new String(getCurrentCompilerToken());

    }

    protected static void registerImplementation(String token, StackEdition edition) {
        operationMap.put(token.toCharArray(), edition);
    }
}

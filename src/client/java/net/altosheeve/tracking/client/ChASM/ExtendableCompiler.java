package net.altosheeve.tracking.client.ChASM;

import net.altosheeve.tracking.client.ChASM.StandardCompiler.StandardCompiler;

import java.util.*;

public class ExtendableCompiler {

    //TODO: make these all non-static!

    public static ArrayList<char[]> compilerTokens = new ArrayList<>();

    public static Map<char[], StackObject> extensions = new HashMap<>(); //for front end extensionals
    public static Map<char[], StackObject> abstractExtensions = new HashMap<>(); //for back end extensionals
    public static Map<char[], ArrayList<char[]>> abstractGroups = new HashMap<>(); //groups back end extensionals to multiple front end extensionals

    public static Map<char[], StandardCompiler.StackEdition> operationMap = new HashMap<>(); //maps front end extensionals to back end operations

    public static TreeObject root = new TreeObject() {
        @Override
        public boolean check(char[] t) { return true; }
    };

    public static ArrayList<Byte> compiledBytecode = new ArrayList<>(); //stores final result

    public static ArrayList<char[]> tokenizedProgram; //stores tokenized compile target

    //control variables
    public static char[] currentToken;
    public static int tokenPointer;
    public static int programPointer;
    public static char[] extendingToken;
    public static boolean abstractExtension = false;
    public static String programExtension;

    public static StackObject getExtension(char[] name) {
        for (char[] key : extensions.keySet()) if (Arrays.equals(key, name)) return extensions.get(key);
        return null;
    }

    public static StackObject getAbstractExtension(char[] name) {
        for (char[] key : abstractExtensions.keySet()) if (Arrays.equals(key, name)) return abstractExtensions.get(key);
        return null;
    }

    public static ArrayList<char[]> getExtensionalGroup(char[] name) {
        for (char[] key : abstractGroups.keySet()) if (Arrays.equals(key, name)) return abstractGroups.get(key);
        return null;
    }

    public static ArrayList<char[]> tokenize(ArrayList<char[]> identifiers, char[] program) {

        ArrayList<char[]> newTokens = new ArrayList<>();
        char[] newToken = new char[]{};

        identifiers.sort((o1, o2) -> o2.length - o1.length); //sort by length in descending order. compilerTokens that are less likely to appear are the longest and should be checked first

        for (int pIndex = 0; pIndex < program.length; pIndex++) {

            boolean matched = false;

            if (program[pIndex] != ' ') { //ignore whitespace

                for (char[] token : identifiers) { //check every token

                    int matches = 0;

                    if (token.length - 1 + pIndex < program.length) { //check to see if token match is even within bounds of program before matching

                        int matchIndex = pIndex;

                        for (int tIndex = 0; tIndex < token.length; tIndex++) {

                            matchIndex = pIndex + tIndex;
                            if (token[tIndex] == program[matchIndex]) matches++;

                        }

                        if (matches == token.length) {

                            if (newToken.length != 0) newTokens.add(newToken);
                            newTokens.add(token);
                            newToken = new char[]{};

                            matched = true;

                            pIndex = matchIndex;

                            break;

                        }

                    }
                }

                if (!matched) {
                    newToken = Arrays.copyOf(newToken, newToken.length + 1);
                    newToken[newToken.length - 1] = program[pIndex];
                }

            }

            else if (newToken.length != 0) { //add what's left of the new token if encountering whitespace
                newTokens.add(newToken);
                newToken = new char[]{};
            }

        }

        newTokens.add(newToken);
        return newTokens;
    }

    public static char[] getCurrentCompilerToken() { return currentToken; }

    public static void getNextCompilerToken() {

        tokenPointer++;
        currentToken = compilerTokens.get(tokenPointer);

    }

    public static StackObject getCurrentStackObject() {

        if (!abstractExtension) return extensions.get(extendingToken);
        return abstractExtensions.get(extendingToken);

    }

    public static void buildCompiler(char[] program) throws Exception {

        ArrayList<char[]> identifiers = root.getIdentifiers();

        compilerTokens = tokenize(identifiers, program);

        for (tokenPointer = 0; tokenPointer < compilerTokens.size() - 1; tokenPointer++)
            if (!root.runTree(compilerTokens, tokenPointer))
                throw new Exception("Unexpected token");

    }

    public ArrayList<Byte> runCompiler(String program) throws Exception {

        ArrayList<char[]> identifiers = new ArrayList<>();
        identifiers.add(new char[]{' '});
        identifiers.addAll(extensions.keySet());

        compiledBytecode.clear();

        tokenizedProgram = tokenize(identifiers, program.toCharArray());

        for (programPointer = 0; programPointer < tokenizedProgram.size(); programPointer++) {

            char[] token = tokenizedProgram.get(programPointer);

            for (char[] key : extensions.keySet()) {

                if (Arrays.equals(key, token)) {

                    //System.out.println(STR."running extension: \{new String(token)}");
                    StackObject copiedStackProgram = (StackObject) extensions.get(key).clone();
                    copiedStackProgram.selfValue = new String(token);

                    while (!copiedStackProgram.runOperation());
                    copiedStackProgram.unfreezeFromTop();

                    break;
                }

            }

        }

        return compiledBytecode;

    }

}

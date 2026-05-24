package net.altosheeve.tracking.client.ChASM.Syntax;

import net.altosheeve.tracking.client.ChASM.TreeObject;

import java.util.Arrays;

public class StaticToken extends TreeObject {

    @Override
    public boolean check(char[] programToken) {
        //surrounded by quotes
        return programToken[0] == '"' && programToken[programToken.length - 1] == '"';

    }

    @Override
    public char[] compilerAction(char[] programToken) throws Exception {
        //remove quotes
        return Arrays.copyOfRange(programToken, 1, programToken.length - 2);
    }

}

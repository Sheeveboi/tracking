package net.altosheeve.tracking.client.ChASM.Syntax;

import net.altosheeve.tracking.client.ChASM.ExtendableCompiler;
import net.altosheeve.tracking.client.ChASM.TreeObject;

public class EXTEND extends TreeObject {

    public EXTEND() {

        this.registerNewTreeSegment("", new StaticToken() {

            @Override
            public char[] compilerAction(char[] programToken) throws Exception {

                programToken = super.compilerAction(programToken);

                ExtendableCompiler.extendingToken    = programToken;
                ExtendableCompiler.abstractExtension = false;

                if (ExtendableCompiler.extensions.containsKey(programToken)) throw new Exception("Extensional with name '" + new String(programToken) + "' already exists.");

                if (ExtendableCompiler.abstractExtensions.containsKey(programToken)) throw new Exception("Abstract Extensional with name '" + new String(programToken) + "' already exists.");

                return null;

            }

        });

    }


}

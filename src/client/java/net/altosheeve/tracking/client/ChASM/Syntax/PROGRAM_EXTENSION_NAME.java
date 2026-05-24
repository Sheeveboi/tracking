package net.altosheeve.tracking.client.ChASM.Syntax;

import net.altosheeve.tracking.client.ChASM.ExtendableCompiler;
import net.altosheeve.tracking.client.ChASM.TreeObject;

import java.util.ArrayList;

public class PROGRAM_EXTENSION_NAME extends TreeObject {

    public PROGRAM_EXTENSION_NAME() {

        this.registerNewTreeSegment("", new StaticToken() {

            @Override
            public char[] compilerAction(char[] programToken) throws Exception {

                programToken = super.compilerAction(programToken);

                ExtendableCompiler.programExtension = new String(programToken);

                return programToken;

            }

        });

    }

}

package net.altosheeve.tracking.client.ChASM.Syntax;

import net.altosheeve.tracking.client.ChASM.ExtendableCompiler;
import net.altosheeve.tracking.client.ChASM.TreeObject;

import java.util.ArrayList;

public class AS extends TreeObject {

    public AS() {

        this.registerNewTreeSegment("", new StaticToken() {

            @Override
            public char[] compilerAction(char[] programToken) throws Exception {

                programToken = super.compilerAction(programToken);

                if (ExtendableCompiler.abstractExtension) throw new Exception("Cannot group abstract extensionals to other abstract extensionals.");

                if (!ExtendableCompiler.abstractExtensions.containsKey(programToken)) throw new Exception("Could not find abstract extensional.");

                if (!ExtendableCompiler.abstractGroups.containsKey(programToken)) ExtendableCompiler.abstractGroups.put(programToken, new ArrayList<>());
                ExtendableCompiler.abstractGroups.get(programToken).add(ExtendableCompiler.extendingToken);

                return null;

            }

        });

    }

}

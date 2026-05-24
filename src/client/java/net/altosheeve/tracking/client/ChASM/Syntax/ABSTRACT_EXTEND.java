package net.altosheeve.tracking.client.ChASM.Syntax;

import net.altosheeve.tracking.client.ChASM.ExtendableCompiler;
import net.altosheeve.tracking.client.ChASM.StackObject;
import net.altosheeve.tracking.client.ChASM.TreeObject;

public class ABSTRACT_EXTEND extends TreeObject {

        public ABSTRACT_EXTEND() {

            this.registerNewTreeSegment("", new StaticToken() {

                @Override
                public char[] compilerAction(char[] programToken) throws Exception {

                    programToken = super.compilerAction(programToken);

                    ExtendableCompiler.extendingToken    = programToken;
                    ExtendableCompiler.abstractExtension = true;

                    if (ExtendableCompiler.extensions.containsKey(programToken)) throw new Exception("Extensional with name '" + new String(programToken) + "' already exists.");

                    if (ExtendableCompiler.abstractExtensions.containsKey(programToken)) throw new Exception("Abstract Extensional with name '" + new String(programToken) + "' already exists.");

                    ExtendableCompiler.abstractExtensions.put(programToken, new StackObject(() -> true, ExtendableCompiler.extendingToken.clone(), "ABSTRACT EXTEND"));

                    return null;

                }

            });

        }

}

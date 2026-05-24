package net.altosheeve.tracking.client.ChASM.Syntax;

import net.altosheeve.tracking.client.ChASM.ExtendableCompiler;
import net.altosheeve.tracking.client.ChASM.StackObject;
import net.altosheeve.tracking.client.ChASM.TreeObject;

public class IMPLY extends TreeObject {

    public IMPLY() {

        this.registerNewTreeSegment("", new StaticToken() {

            @Override
            public char[] compilerAction(char[] programToken) throws Exception {

                programToken = super.compilerAction(programToken);

                StackObject currentStackObject  = ExtendableCompiler.getCurrentStackObject();
                StackObject abstractStackObject = ExtendableCompiler.getAbstractExtension(programToken);

                if (abstractStackObject == null) throw new Exception("Could not find abstract extensional.");

                System.out.println(new String(currentStackObject.token) + " will imply " + abstractStackObject);

                currentStackObject.pushStack((StackObject) abstractStackObject.clone());

                return null;

            }

        });

    }

}

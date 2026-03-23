package net.altosheeve.tracking.client.ChASM.StandardCompiler.ExpectationObjects;

import net.altosheeve.tracking.client.ChASM.ExtendableCompiler;

import java.util.ArrayList;
import java.util.regex.Matcher;

public class AbstractExtensional extends Expectation {

    public AbstractExtensional(char[] name, boolean gathered) {
        super(name, gathered);
        this.extensional = ExtendableCompiler.getAbstractExtension(name);
    }

    @Override
    public boolean check(char[] programToken) throws Exception {

        this.extensional.selfValue = new String(programToken);

        boolean out = false;

        if (this.extensional.positiveSyntax == null && this.extensional.negativeSyntax == null) out = true;
        else {

            if (this.extensional.positiveSyntax != null) {
                Matcher matcher = this.extensional.positiveSyntax.matcher(new String(programToken));
                out = matcher.find();
            }

            if (this.extensional.negativeSyntax != null) {
                Matcher matcher = this.extensional.negativeSyntax.matcher(new String(programToken));
                out = !matcher.find();
            }

            if (!out) throw new Exception("Syntax error.");

        }

        if (out && !gathered) this.extensional.runOperation();
        return out;

    }

    @Override
    public int assignParameters(ArrayList<char[]> fullContext, int location) {
        return 0;
    }

}

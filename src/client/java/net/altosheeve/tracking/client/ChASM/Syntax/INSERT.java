package net.altosheeve.tracking.client.ChASM.Syntax;

import net.altosheeve.tracking.client.ChASM.Syntax.InsertionFormats.INTEGER;
import net.altosheeve.tracking.client.ChASM.TreeObject;

public class INSERT extends TreeObject {

    public INSERT() {

        this.registerNewTreeSegment("INTEGER", new INTEGER());

    }

}

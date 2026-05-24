package net.altosheeve.tracking.client.ChASM;

import java.util.ArrayList;
import java.util.Arrays;

public class TreeObject {

    public TreeObject parent;
    public char[]     token;

    public ArrayList<TreeObject> children = new ArrayList<>();

    public TreeObject() {}

    public TreeObject(TreeObject parent, ArrayList<TreeObject> children, char[] token) {

        this.parent   = parent;
        this.children = children;
        this.token    = token;

    }

    public void registerNewTreeSegment(String token, TreeObject childTreeObject) {

        childTreeObject.parent = this;
        childTreeObject.token  = token.toCharArray();

        this.children.add(childTreeObject);
    }

    public ArrayList<char[]> getIdentifiers() {

        ArrayList<char[]> out = new ArrayList<>();
        for (TreeObject child : this.children) out.add(child.token);

        for (TreeObject childTree : this.children)
            if (!childTree.children.isEmpty())
                out.addAll(childTree.getIdentifiers());

        return out;

    }

    public char[]  compilerAction (char[] currentToken) throws Exception { return null; }
    public boolean programAction  (char[] contextToken) { return true; }

    public boolean check (char[] programToken) { return Arrays.equals(programToken, this.token) && this.token != null; }

    public boolean runTree (ArrayList<char[]> program, int position) throws Exception {

        char[] token = program.get(position);

        System.out.println("chasm token: " + new String(token));
        if (this.token != null) System.out.println("expected: " + new String(this.token));

        if (!this.check(token))
            return false;

        char[] compilerContext = this.compilerAction(token);
        if (compilerContext != null) {

            char[][] result = {compilerContext};
            ExtendableCompiler.getCurrentStackObject().pushStack(() -> this.programAction(result[0]), ExtendableCompiler.extendingToken, new String(this.token));

        }

        boolean out = true;

        for (TreeObject childTree : this.children) {

            System.out.println("checking children");

            if (childTree.runTree(program, position + 1)) {
                System.out.println("returning true");
                return true;
            }

            out = false;

        }

        return out;
    }

}

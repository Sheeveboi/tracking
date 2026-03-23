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

    public interface Callback { void cb(ArrayList<char[]> program, int position) throws Exception; }
    public void registerOperation(String token, Callback cb) {

        TreeObject childTreeObject = new TreeObject(this, new ArrayList<>(), token.toCharArray()) {
            @Override
            public void action(ArrayList<char[]> program, int position) throws Exception { cb.cb(program, position); }
        };

        this.children.add(childTreeObject);

    }

    public void registerOperation(String token, TreeObject childTreeObject) {

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

    public void    action  (ArrayList<char[]> program, int position) throws Exception {}

    public boolean check   (char[] programToken) { return Arrays.equals(programToken, this.token) && this.token != null; }

    public boolean runTree (ArrayList<char[]> program, int position) throws Exception {

        char[] token = program.get(position);

        System.out.println("chasm token: " + new String(token));
        if (this.token != null) System.out.println("expected: " + new String(this.token));

        if (!this.check(token))
            return false;

        this.action(program, position);

        boolean out = true;

        for (TreeObject childTree : this.children) {

            System.out.println("checking children");

            if (childTree.runTree(program, position)) {
                System.out.println("returning true");
                return true;
            }

            out = false;

        }

        return out;
    }

}

package net.altosheeve.tracking.client.ChASM;

import net.altosheeve.tracking.client.ChASM.ExpectationObjects.Expectation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class StackObject {

    StackObject parent;
    StackObject child;

    public char[] token;
    public String operationName;
    public int stackSize = 1;
    public Pattern positiveSyntax;
    public Pattern negativeSyntax;
    public String selfValue;

    public ArrayList<char[]> implications = new ArrayList<>();
    public Map<char[], ArrayList<char[]>> capturedBodies = new HashMap<>();

    public boolean complete = false;

    CB function;

    public interface CB {
        boolean cb() throws Exception;
    }

    public ArrayList<Expectation> expectations = new ArrayList<>();

    public StackObject(CB operation, char[] token, String operationName) {
        this.token = token;
        this.function = operation;
        this.operationName = operationName;
    }

    public void unfreeze() {
        this.complete = false;
        if (this.parent != null) this.parent.unfreeze();
    }

    public void printStack() {
        if (this.child != null) this.child.printStack();
        System.out.println(this.operationName);
    }

    public void pushStack(CB operation, char[] token, String operationName) {

        System.out.println(STR."pushing stack on \{this.operationName}");

        this.stackSize++;

        if (this.child != null) this.child.pushStack(operation, token, operationName);

        else {

            System.out.println(STR."ended pushing stack at \{new String(this.token)}");

            StackObject newStackObject = new StackObject(operation, token, operationName);

            this.child = newStackObject;

        }
    }

    public void pushStack(StackObject stackObject) {

        System.out.println(STR."pushing stack on \{this.operationName}");

        this.stackSize++;

        stackObject.token = this.token;

        if (this.child != null) this.child.pushStack(stackObject);

        else {

            System.out.println(STR."ended pushing stack at \{new String(this.token)}");

            this.child = stackObject;
        }
    }

    public boolean extentionStatus() {
        if (this.complete && this.child != null) this.child.extentionStatus();
        return this.complete;
    }

    public StackObject getEnd() {
        if (this.child == null) return this;
        return this.child.getEnd();
    }

    public boolean runOperation() throws Exception {

        if (!this.complete) this.complete = this.function.cb();

        if (this.child == null) {
            unfreeze();
            return true;
        }

        if (this.complete) return this.child.runOperation();

        return false;

    }

}

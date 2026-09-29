package net.altosheeve.tracking.client.Navigation.Nodes;

public class Walk extends Node {
    public Walk(int x, int y, int z) {
        super(x, y, z);

        this.r = 0;
        this.g = 0;
        this.b = 1;
        this.a = .3f;
    }
}

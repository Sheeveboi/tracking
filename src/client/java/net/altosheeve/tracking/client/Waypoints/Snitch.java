package net.altosheeve.tracking.client.Waypoints;

import net.altosheeve.tracking.client.Core.Values;
import net.altosheeve.tracking.client.Shapes.Circle;
import net.altosheeve.tracking.client.Shapes.Transforms;
import net.altosheeve.tracking.client.Shapes.Triangle;
import net.minecraft.client.render.BufferBuilder;

public class Snitch extends Waypoint {
    public Snitch(float x, float y, float z, String UUID, String username) {

        super(x, y, z, UUID, username);

        this.focusThreshold = .005f;
        this.scale          = 2f;
        this.importance     = .4f;
        this.decayRate      = .0001f;
        this.textSize       = .004f;;

        this.r = Values.whiteColor[0];
        this.g = Values.whiteColor[1];
        this.b = Values.whiteColor[2];

    }

    @Override
    public void fill(BufferBuilder buffer) {

        org.joml.Matrix4f spriteTransform = Transforms.getWorld3dSpriteTransform(this.x, this.y, this.z, this.scale * .02f, this.scale * .02f, this.scale * .02f);

        Triangle triangle = new Triangle(0, 0, 0, this.UUID);

        triangle.color(this.r, this.g, this.b, this.importance);
        triangle.parentLayer = this.parentLayer;

        triangle.set(buffer, spriteTransform);

    }
}

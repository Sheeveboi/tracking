package net.altosheeve.tracking.client.Waypoints;

import net.altosheeve.tracking.client.Core.Values;
import net.altosheeve.tracking.client.Shapes.Circle;
import net.altosheeve.tracking.client.Shapes.Transforms;
import net.minecraft.client.render.BufferBuilder;

public class GoodGuy extends Waypoint {

    public GoodGuy(float x, float y, float z, String UUID, String username) {

        super(x, y, z, UUID, username);

        this.focusThreshold = .02f;
        this.scale          = 1f;
        this.importance     = 1f;
        this.decayRate      = .0001f;
        this.textSize       = .0055f;

        this.r = Values.greenColor[0];
        this.g = Values.greenColor[1];
        this.b = Values.greenColor[2];

    }

    @Override
    public void fill(BufferBuilder buffer) {

        org.joml.Matrix4f spriteTransform = Transforms.getWorld3dSpriteTransform(this.x, this.y, this.z, this.scale * .02f, this.scale * .02f, this.scale * .02f);

        Circle innercircle = new Circle(0, 0, 0, this.UUID, 0, .6f);
        Circle outercircle = new Circle(0, 0, 0, this.UUID, .8f, 1f);

        innercircle.color(this.r, this.g, this.b, this.importance);
        outercircle.color(this.r, this.g, this.b, this.importance);

        innercircle.parentLayer = this.parentLayer;
        outercircle.parentLayer = this.parentLayer;

        innercircle.set(buffer, spriteTransform);
        outercircle.set(buffer, spriteTransform);

    }
}

package net.altosheeve.tracking.client.Waypoints;

import net.altosheeve.tracking.client.Core.Values;
import net.altosheeve.tracking.client.Shapes.Circle;
import net.altosheeve.tracking.client.Shapes.Transforms;
import net.minecraft.client.render.BufferBuilder;

public class Hitler extends Waypoint {
    public Hitler(float x, float y, float z, String UUID, String username) {

        super(x, y, z, UUID, username);

        this.focusThreshold = .05f;
        this.scale          = 1f;
        this.importance     = 1f;
        this.decayRate      = .00005f;
        this.textSize       = .0055f;

        this.r = Values.redColor[0];
        this.g = Values.redColor[1];
        this.b = Values.redColor[2];

    }

    @Override
    public void fill(BufferBuilder buffer) {

        org.joml.Matrix4f spriteTransform = Transforms.getWorld3dSpriteTransform(this.x, this.y, this.z, this.scale * .02f, this.scale * .02f, this.scale * .02f);

        Circle outercircle = new Circle(0, 0, 0, this.UUID, 0, 1f);

        outercircle.color(this.r, this.g, this.b, this.importance);
        outercircle.parentLayer = this.parentLayer;

        outercircle.set(buffer, spriteTransform);

    }
}

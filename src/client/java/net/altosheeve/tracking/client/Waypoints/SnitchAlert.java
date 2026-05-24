package net.altosheeve.tracking.client.Waypoints;

import net.altosheeve.tracking.client.Core.Values;

public class SnitchAlert extends Snitch {
    public SnitchAlert(float x, float y, float z, String UUID, String username) {

        super(x, y, z, UUID, username);

        this.focusThreshold = .01f;
        this.scale          = 1f;
        this.importance     = .8f;
        this.decayRate      = .00005f;
        this.textSize       = .006f;

        this.r = Values.redColor[0];
        this.g = Values.redColor[1];
        this.b = Values.redColor[2];

    }
}

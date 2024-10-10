package se.miun.distsys.listeners;

import se.miun.distsys.clock.VectorClock;

public interface VectorClockListener {
    public void onVectorClockChanged(VectorClock vectorClock);
}

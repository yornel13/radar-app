package com.guardias.yornel.gpslocation.entity;

import java.util.List;

/**
 * Created by Yornel on 20/7/2017.
 */

public class Export {

    private List<ControlPosition> controlPositions;
    private List<Watch> watches;

    public List<ControlPosition> getControlPositions() {
        return controlPositions;
    }

    public void setControlPositions(List<ControlPosition> controlPositions) {
        this.controlPositions = controlPositions;
    }

    public List<Watch> getWatches() {
        return watches;
    }

    public void setWatches(List<Watch> watches) {
        this.watches = watches;
    }
}

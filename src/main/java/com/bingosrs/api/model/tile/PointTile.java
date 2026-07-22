package com.bingosrs.api.model.tile;

import com.bingosrs.api.model.Drop;
import com.bingosrs.api.model.RequiredDrop;
import com.bingosrs.api.model.Team;
import com.google.gson.annotations.SerializedName;

public class PointTile extends Tile {
    @SerializedName("requiredPoints")
    private Integer requiredPoints;

    @SerializedName("requiredDropValues")
    private RequiredDrop[] requiredDropValues;

    public Integer getRequiredPoints() {
        return this.requiredPoints;
    }

    public RequiredDrop[] getRequiredDrops() {
        return this.requiredDropValues;
    }

    @Override
    public boolean isCompleted(RequiredDrop[] remainingDrops, Drop[] drops) {
        return remainingDrops.length == 0;
    }
}
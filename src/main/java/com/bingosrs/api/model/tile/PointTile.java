package com.bingosrs.api.model.tile;

import com.bingosrs.api.model.RequiredDrop;
import com.google.gson.annotations.SerializedName;

public class PointTile extends Tile {
    @SerializedName("requiredDropValues")
    private RequiredDrop[] requiredDropValues;

    public RequiredDrop[] getRequiredDrops() {
        return this.requiredDropValues;
    }
}
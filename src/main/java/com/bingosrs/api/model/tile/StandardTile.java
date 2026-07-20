package com.bingosrs.api.model.tile;

import com.bingosrs.api.model.RequiredDrop;
import com.google.gson.annotations.SerializedName;

public class StandardTile extends Tile {
    @SerializedName("requiredDropGroups")
    public RequiredDrop[][] requiredDropGroups;

    public RequiredDrop[][] getRequiredDropGroups() {
        return this.requiredDropGroups;
    }
}
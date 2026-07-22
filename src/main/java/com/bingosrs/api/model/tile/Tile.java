package com.bingosrs.api.model.tile;

import com.bingosrs.api.model.Drop;
import com.bingosrs.api.model.RequiredDrop;
import com.bingosrs.api.model.Team;
import com.google.gson.annotations.SerializedName;

public abstract class Tile {
    @SerializedName("__t")
    public String __t;

    @SerializedName("description")
    public String description;

    public abstract boolean isCompleted(RequiredDrop[] remainingDrops, Drop[] drops);
}
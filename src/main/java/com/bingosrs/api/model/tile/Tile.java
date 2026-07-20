package com.bingosrs.api.model.tile;

import com.google.gson.annotations.SerializedName;

public abstract class Tile {
    @SerializedName("__t")
    public String __t;

    @SerializedName("description")
    public String description;
}
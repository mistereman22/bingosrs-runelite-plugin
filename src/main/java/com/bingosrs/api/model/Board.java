package com.bingosrs.api.model;

import com.bingosrs.api.model.tile.Tile;
import com.google.gson.annotations.SerializedName;

public class Board {
    @SerializedName("size")
    public Integer size;

    @SerializedName("tiles")
    public Tile[] tiles;
}

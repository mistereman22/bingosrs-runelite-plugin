package com.bingosrs.api.model;
import com.bingosrs.api.model.tile.Tile;
import com.google.gson.annotations.SerializedName;

public class Team {
    @SerializedName("name")
    public String name;

    @SerializedName("players")
    public String[] players;

    @SerializedName("remainingDrops")
    public RequiredDrop[][] remainingDrops;

    @SerializedName("drops")
    public Drop[][] drops;

    @Override
    public String toString() {
        return name;
    }

    public boolean isTileComplete(Tile tile, int idx) {
        return tile.isCompleted(this.remainingDrops[idx], this.drops[idx]);
    }
}

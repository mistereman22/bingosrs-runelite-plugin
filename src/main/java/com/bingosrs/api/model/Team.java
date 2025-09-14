package com.bingosrs.api.model;
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
}

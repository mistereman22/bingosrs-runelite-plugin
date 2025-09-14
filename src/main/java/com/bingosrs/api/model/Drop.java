package com.bingosrs.api.model;

import com.google.gson.annotations.SerializedName;

public class Drop {
    @SerializedName("item")
    public Integer item;

    @SerializedName("player")
    public String player;
}

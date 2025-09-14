package com.bingosrs.api.model;

import com.google.gson.annotations.SerializedName;

public class Bingo {
    @SerializedName("name")
    public String name;

    @SerializedName("board")
    public Board board;

    @SerializedName("state")
    public String state;

}

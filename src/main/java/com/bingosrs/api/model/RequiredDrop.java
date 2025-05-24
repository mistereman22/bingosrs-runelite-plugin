package com.bingosrs.api.model;

import com.google.gson.annotations.SerializedName;

public class RequiredDrop {
    @SerializedName("item")
    public Integer item;

    @SerializedName("bosses")
    public Integer[] bosses;
}

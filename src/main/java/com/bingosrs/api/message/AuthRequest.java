package com.bingosrs.api.message;

import com.google.gson.annotations.SerializedName;

public class AuthRequest {
    @SerializedName("bingoId")
    String bingoId;
    @SerializedName("token")
    String token;

    public AuthRequest(String bingoId, String token) {
        this.bingoId = bingoId;
        this.token = token;
    }
}

package com.bingosrs.api.model.tile;

import com.bingosrs.api.model.RequiredDrop;

public class CustomTile extends Tile {
    public RequiredDrop[] getRequiredDrops() {
        return new RequiredDrop[0];
    }
}

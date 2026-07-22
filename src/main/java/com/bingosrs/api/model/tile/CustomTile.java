package com.bingosrs.api.model.tile;

import com.bingosrs.api.model.Drop;
import com.bingosrs.api.model.RequiredDrop;
import com.bingosrs.api.model.Team;

public class CustomTile extends Tile {
    @Override
    public boolean isCompleted(RequiredDrop[] remainingDrops, Drop[] drops) {
        return drops.length > 0;
    }
}

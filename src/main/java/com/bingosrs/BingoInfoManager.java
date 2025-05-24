package com.bingosrs;

import com.bingosrs.api.BingOSRSService;
import com.bingosrs.api.model.RequiredDrop;
import com.bingosrs.api.model.Team;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.events.ConfigChanged;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Slf4j
@Singleton
public class BingoInfoManager {
    public static final String CONFIG_GROUP = "bingosrs";

    @Inject
    private BingOSRSConfig config;

    @Inject
    private Client client;

    @Inject
    private BingOSRSService bingOSRSService;

    // prevent overlapping calls
    private boolean shouldUpdateRequiredDrops = false;
    private List<RequiredDrop> requiredDrops = new ArrayList<>();

    void onConfigChanged(ConfigChanged event) {
        if (client.getGameState() == GameState.LOGGED_IN) {
            if (event.getKey().equals("bingoId") && !config.bingoId().isBlank() && !config.playerToken().isBlank()) {
                this.updateRequiredDrops();
            }
        }
    }

    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState newState = gameStateChanged.getGameState();
        if (newState == GameState.LOGGED_IN) {
            this.shouldUpdateRequiredDrops = true;
        }
    }

    public void onGameTick(GameTick gameTick) {
        if (this.shouldUpdateRequiredDrops) {
            this.shouldUpdateRequiredDrops = false;
            this.updateRequiredDrops();
        }
    }

    public void triggerUpdateRequiredDrops() {
        shouldUpdateRequiredDrops = true;
    }

    private void updateRequiredDrops() {
        bingOSRSService.fetchTeamsAsync()
                .thenAccept(teams -> {
                    List<RequiredDrop> requiredDrops = new ArrayList<>();
                    boolean onTeam = false;
                    for (Team team: teams) {
                        if (Arrays.asList(team.players).contains(client.getLocalPlayer().getName())) {
                            onTeam = true;
                            for (RequiredDrop[] tileDrops: team.remainingDrops) {
                                requiredDrops.addAll(Arrays.asList(tileDrops));
                            }
                            break;
                        }
                    }

                    this.requiredDrops = requiredDrops;
                    if (!onTeam) {
                        log.debug("No team found for player");
                    } else {
                        log.debug("Required drops updated");
                    }
                })
                .exceptionally(throwable -> {
                    return null;
                });
    }

    public boolean isRequiredDrop(Integer itemId, Integer npcId) {
        for (RequiredDrop requiredDrop: requiredDrops) {
            if (Objects.equals(requiredDrop.item, itemId)) {
                if (requiredDrop.bosses.length == 0 || Arrays.asList(requiredDrop.bosses).contains(npcId)) {
                    return true;
                }
            }
        }
        return false;
    }
}

package com.bingosrs;

import com.bingosrs.api.BingOSRSService;
import com.bingosrs.api.model.Bingo;
import com.bingosrs.api.model.RequiredDrop;
import com.bingosrs.api.model.Team;
import com.bingosrs.panel.BingOSRSPanel;
import lombok.Getter;
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

    @Inject
    private BingOSRSPlugin plugin;

    // Default to true so this pulls on first game tick
    private boolean shouldUpdateData = true;

    @Getter
    private Bingo bingo;
    @Getter
    private Team[] teams;
    @Getter
    private Team team;

    public void setBingo(Bingo bingo) {
        this.bingo = bingo;
        this.plugin.updatePanel();
    }

    public void setTeam(Team team) {
        this.team = team;
        this.plugin.updatePanel();
    }

    public void setTeams(Team[] teams) {
        this.teams = teams;
        this.plugin.updatePanel();
    }

    public void startUp() {
        this.updateData();
    }

    public void onGameTick(GameTick gameTick) {
        if (this.shouldUpdateData) {
            this.shouldUpdateData = false;
            this.updateData();
        }
    }

    public void triggerUpdateData() {
        triggerUpdateData(true);
    }

    public void triggerUpdateData(boolean lazy) {
        if (lazy) {
            shouldUpdateData = true;
        } else {
            updateData();
        }
    }

    private void updateData() {
        setBingo(null);
        setTeam(null);
        setTeams(null);

        if (config.bingoId().isBlank()) {
            return;
        }
        bingOSRSService.fetchBingoAsync()
                .thenAccept(bingo -> {
                    setBingo(bingo);
                    if (!bingo.state.equals("Signup")) {
                        bingOSRSService.fetchTeamsAsync()
                                .thenAccept(teams -> {
                                    setTeams(teams);
                                    if (client.getLocalPlayer() != null) {
                                        boolean onTeam = false;
                                        for (Team team: teams) {
                                            if (Arrays.asList(team.players).contains(client.getLocalPlayer().getName())) {
                                                setTeam(team);
                                                onTeam = true;
                                                break;
                                            }
                                        }

                                        if (!onTeam) {
                                            setTeam(null);
                                            log.debug("No team found for player");
                                        } else {
                                            log.debug("Team data updated");
                                        }
                                    }
                                })
                                .exceptionally(throwable -> null);
                    }
                })
                .exceptionally(throwable -> null);
    }

    public boolean isRequiredDrop(Integer itemId, Integer npcId) {
        if (team == null) {
            return false;
        }

        for (RequiredDrop[] tileDrops: this.team.remainingDrops) {
            for (RequiredDrop requiredDrop: tileDrops) {
                if (Objects.equals(requiredDrop.item, itemId)) {
                    if (requiredDrop.bosses.length == 0 || Arrays.asList(requiredDrop.bosses).contains(npcId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

package com.bingosrs.panel;

import com.bingosrs.BingoInfoManager;
import com.bingosrs.api.BingOSRSService;
import com.bingosrs.api.model.Bingo;
import com.bingosrs.api.model.Team;

import java.awt.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.bingosrs.api.model.tile.CustomTile;
import com.bingosrs.api.model.tile.PointTile;
import com.bingosrs.api.model.tile.Tile;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.LinkBrowser;

@Slf4j
@Singleton
public class BingOSRSPanel extends PluginPanel {
    private final BingoInfoManager bingoInfoManager;
    private final BingOSRSService bingOSRSService;
    private final Client client;
    private final ClientThread clientThread;

    private final PluginErrorPanel noBingoDataPanel = new PluginErrorPanel();

    private final WarningPanel notInBingoPanel = new WarningPanel("Team not found. Check with your bingo admin that you're in the bingo and assigned to a team.");
    private final WarningPanel notAuthenticatedPanel = new WarningPanel("Error authenticating, drops will not be submitted. Double check that you entered the Player Token correctly in the config.");

    private final JButton linkButton = new JButton("Open Bingo");

    private final JComponent contentPanel = new JPanel();
    private final JPanel gridPanel = new JPanel();
    private final JPanel detailPanel = new JPanel();
    private JComboBox<Team> teamSelector = new JComboBox<>();
    private final JPanel selectorWrapper = new JPanel(new BorderLayout());

    private Integer activeTileIndex = null;
    private boolean updateTriggered = false;

    @Inject
    BingOSRSPanel(final BingoInfoManager bingoInfoManager, final BingOSRSService bingOSRSService, final Client client, final ClientThread clientThread)
    {
        this.bingoInfoManager = bingoInfoManager;
        this.bingOSRSService = bingOSRSService;
        this.client = client;
        this.clientThread = clientThread;

        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        final JPanel layoutPanel = new JPanel();
        layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));
        add(layoutPanel, BorderLayout.NORTH);

        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(0, 0, 6, 0));

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BorderLayout());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFocusable(false);
        refreshButton.addActionListener(e -> bingoInfoManager.triggerUpdateData(false));
        navPanel.add(refreshButton, BorderLayout.EAST);

        this.linkButton.setFocusable(false);
        this.linkButton.addActionListener(e -> LinkBrowser.browse("https://bingosrs.com/bingo/" + bingoInfoManager.getBingo().id));
        navPanel.add(this.linkButton, BorderLayout.WEST);

        topPanel.add(navPanel, BorderLayout.NORTH);

        layoutPanel.add(topPanel);

        contentPanel.setLayout(new BorderLayout());
        layoutPanel.add(contentPanel);

        teamSelector.setFocusable(false);
        teamSelector.addActionListener(e -> {
            bingoInfoManager.setSelectedTeam((Team) teamSelector.getSelectedItem());
            update();
        });
        selectorWrapper.setBorder(new EmptyBorder(6, 0, 6, 0));
        selectorWrapper.add(teamSelector);
        layoutPanel.add(selectorWrapper);

        gridPanel.setLayout(new BorderLayout());
        layoutPanel.add(gridPanel);

        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        layoutPanel.add(detailPanel);


        noBingoDataPanel.setContent("Bingo not found", "Double check that you entered the correct Bingo ID in the config.");

        update();
    }

    public synchronized void update()
    {
        if (updateTriggered) {
            return;
        }
        updateTriggered = true;

        this.contentPanel.removeAll();
        this.linkButton.setVisible(false);

        Bingo bingo = bingoInfoManager.getBingo();

        if (bingo == null) {
            contentPanel.add(noBingoDataPanel);
            teamSelector.removeAllItems();
            selectorWrapper.setVisible(false);
        } else {
            Team[] teams = bingoInfoManager.getTeams();
            Team team = bingoInfoManager.getSelectedTeam();

            if (teams != null && teams.length > 0) {
                selectorWrapper.setVisible(true);
                if (teamSelector.getItemCount() == 0) {
                    teamSelector.removeAllItems();
                    for (Team t : teams) {
                        teamSelector.addItem(t);
                    }

                    if (team == null && teams.length > 0) {
                        team = teams[0];
                        bingoInfoManager.setSelectedTeam(team);
                    }
                    teamSelector.setSelectedItem(team);
                }
            } else {
                selectorWrapper.setVisible(false);
            }

            this.linkButton.setVisible(true);
            contentPanel.add(new BingoSummary(bingo, teams), BorderLayout.CENTER);

            JPanel notificationPanel = new JPanel();
            notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));

            if (team == null && client.getLocalPlayer() != null) {
                notificationPanel.add(notInBingoPanel);
            }
            if (!bingOSRSService.isAuthenticated()) {
                notificationPanel.add(notAuthenticatedPanel);
            }
            contentPanel.add(notificationPanel, BorderLayout.SOUTH);

            gridPanel.removeAll();
            gridPanel.add(new BingoBoardGrid(bingo.board.tiles, team, (int) Math.sqrt(bingo.board.tiles.length), activeTileIndex != null ? activeTileIndex : 0, index -> {
                this.activeTileIndex = index;
                update();
            }));

            if (activeTileIndex == null && bingo.board.tiles.length > 0) {
                activeTileIndex = 0;
            }
            updateDetailPanel(bingo, team);

        }

        revalidate();
        repaint();

        updateTriggered = false;
    }

    private void updateDetailPanel(Bingo bingo, Team team) {
        detailPanel.removeAll();
        if (activeTileIndex != null && activeTileIndex < bingo.board.tiles.length) {
            boolean tileCompleted = false;
            if (team != null) {
                Tile activeTile = bingo.board.tiles[activeTileIndex];
                tileCompleted = team.isTileComplete(activeTile, activeTileIndex);
            }
            detailPanel.add(new TileBox(bingo.board.tiles[activeTileIndex], tileCompleted, client, clientThread));
        }
        detailPanel.revalidate();
        detailPanel.repaint();
    }
}

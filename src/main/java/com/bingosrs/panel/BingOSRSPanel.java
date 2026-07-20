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
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
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

        // Scoreboard goes into contentPanel as before
        layoutPanel.add(contentPanel);

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
        } else {
            Team[] teams = bingoInfoManager.getTeams();
            Team team = bingoInfoManager.getTeam();

            this.linkButton.setVisible(true);
            contentPanel.add(new BingoSummary(bingo, teams));

            if (team == null && client.getLocalPlayer() != null) {
                contentPanel.add(notInBingoPanel);
            }
            if (!bingOSRSService.isAuthenticated()) {
                contentPanel.add(notAuthenticatedPanel);
            }

            // Grid View
            gridPanel.removeAll();
            gridPanel.add(new BingoBoardGrid(bingo.board.tiles, (int) Math.sqrt(bingo.board.tiles.length), activeTileIndex != null ? activeTileIndex : 0, index -> {
                this.activeTileIndex = index;
                update();
            }));

            // Detail View
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
                if (bingo.board.tiles[activeTileIndex] instanceof CustomTile) {
                    tileCompleted = team.drops[activeTileIndex].length > 0;
                } else if (bingo.board.tiles[activeTileIndex] instanceof com.bingosrs.api.model.tile.PointTile) {
                    tileCompleted = team.remainingDrops[activeTileIndex].length == 0; // Or whatever your point logic is
                } else {
                    tileCompleted = team.remainingDrops[activeTileIndex].length == 0;
                }
            }
            detailPanel.add(new TileBox(bingo.board.tiles[activeTileIndex], tileCompleted, client, clientThread));
        }
        detailPanel.revalidate();
        detailPanel.repaint();
    }
}

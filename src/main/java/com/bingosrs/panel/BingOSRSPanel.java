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

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;

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

    private final JComponent contentPanel = new JPanel();

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

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFocusable(false);
        refreshButton.addActionListener(e -> bingoInfoManager.triggerUpdateData(false));
        topPanel.add(refreshButton, BorderLayout.EAST);

        layoutPanel.add(topPanel);

        this.contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        layoutPanel.add(contentPanel);

        noBingoDataPanel.setContent("Bingo not found", "Double check that you entered the correct Bingo ID in the config.");

        update();
    }

    public synchronized void update()
    {
        if (updateTriggered) {
            return;
        }
        updateTriggered = true;

        clientThread.invokeAtTickEnd(() -> {
            updateTriggered = false;
            this.contentPanel.removeAll();

            Bingo bingo = bingoInfoManager.getBingo();

            if (bingo == null) {
                contentPanel.add(noBingoDataPanel);
            } else {
                Team[] teams = bingoInfoManager.getTeams();
                Team team = bingoInfoManager.getTeam();

                contentPanel.add(new BingoSummary(bingo, teams));

                if (team == null && client.getLocalPlayer() != null) {
                    contentPanel.add(notInBingoPanel);
                }
                if (!bingOSRSService.isAuthenticated()) {
                    contentPanel.add(notAuthenticatedPanel);
                }

                JPanel headerPanel = new JPanel();
                headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
                headerPanel.setBorder(new EmptyBorder(6, 0, 3, 0));
                JLabel headerLabel = new JLabel("Tiles" + (team != null ? (" (" + team.name + ")") : "") + ":");
                headerLabel.setFont(FontManager.getRunescapeBoldFont());
                headerPanel.add(headerLabel);
                contentPanel.add(headerPanel);

                for (int tileIdx = 0; tileIdx < bingo.board.tiles.length; tileIdx++) {
                    TileBox tileBox;
                    if (team != null) {
                        tileBox = new TileBox(bingo.board.tiles[tileIdx], team.remainingDrops[tileIdx], client, clientThread);
                    } else {
                        tileBox = new TileBox(bingo.board.tiles[tileIdx], null, client, clientThread);
                    }
                    contentPanel.add(tileBox);
                }
            }

            revalidate();
            repaint();
        });
    }
}

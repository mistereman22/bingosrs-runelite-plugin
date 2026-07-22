package com.bingosrs.panel;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import com.bingosrs.api.model.RequiredDrop;
import com.bingosrs.api.model.tile.PointTile;
import com.bingosrs.api.model.tile.StandardTile;
import com.bingosrs.api.model.tile.Tile;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPCComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

@Slf4j
public class TileBox extends JPanel {
    TileBox(Tile tile, boolean isCompleted, Client client, ClientThread clientThread) {
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(new EmptyBorder(3, 0, 3, 0), new LineBorder(ColorScheme.BORDER_COLOR, 1)));

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        add(innerPanel);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        innerPanel.add(headerPanel);

        if (tile.description != null) {
            JLabel headerLabel = new JLabel("<html><body style='width: 100%;'>" + tile.description + "</body></html>");
            headerLabel.setFont(FontManager.getRunescapeBoldFont());
            headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            headerPanel.add(headerLabel);
        }

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (tile instanceof PointTile) {
            PointTile pointTile = (PointTile) tile;
            JLabel pointsLabel = new JLabel("Required Points: " + pointTile.getRequiredPoints());
            pointsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            headerPanel.add(pointsLabel);

            renderDrops(pointTile.getRequiredDrops(), contentPanel, client, clientThread);
        } else if (tile instanceof StandardTile) {
            StandardTile standardTile = (StandardTile) tile;
            RequiredDrop[][] groups = standardTile.getRequiredDropGroups();

            if (groups.length == 1) {
                renderDrops(groups[0], contentPanel, client, clientThread);
            } else {
                for (int i = 0; i < groups.length; i++) {
                    if (i > 0) {
                        contentPanel.add(Box.createVerticalStrut(8));

                        JPanel orWrapper = new JPanel();
                        orWrapper.setOpaque(false);
                        orWrapper.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
                        orWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

                        JLabel orLabel = new JLabel("OR");
                        orLabel.setFont(FontManager.getRunescapeSmallFont());
                        orLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
                        orWrapper.add(orLabel);
                        contentPanel.add(orWrapper);

                        contentPanel.add(Box.createVerticalStrut(8));
                    }

                    JPanel groupPanel = new JPanel(new BorderLayout());
                    groupPanel.setBorder(new CompoundBorder(
                            new LineBorder(ColorScheme.DARKER_GRAY_COLOR, 1),
                            new EmptyBorder(4, 4, 4, 4)
                    ));
                    groupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                    JPanel dropsPanel = new JPanel();
                    dropsPanel.setLayout(new BoxLayout(dropsPanel, BoxLayout.Y_AXIS));
                    dropsPanel.setOpaque(false);
                    dropsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                    renderDrops(groups[i], dropsPanel, client, clientThread);
                    groupPanel.add(dropsPanel, BorderLayout.CENTER);
                    contentPanel.add(groupPanel);
                }
            }
        }

        // Only display separator if both header and content are present
        if (headerPanel.getComponentCount() > 0 && contentPanel.getComponentCount() > 0) {
            innerPanel.add(Box.createVerticalStrut(4));
            JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            separator.setForeground(ColorScheme.BORDER_COLOR);
            separator.setAlignmentX(Component.LEFT_ALIGNMENT);
            innerPanel.add(separator);
            innerPanel.add(Box.createVerticalStrut(6));
        }

        innerPanel.add(contentPanel);
    }

    private void renderDrops(RequiredDrop[] drops, JPanel parent, Client client, ClientThread clientThread) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        parent.add(content);

        for (int i = 0; i < drops.length; i++) {
            RequiredDrop drop = drops[i];
            final boolean isFirstDrop = (i == 0);

            clientThread.invoke(() -> {
                ItemComposition itemComposition = client.getItemDefinition(drop.item);
                String name = itemComposition.getMembersName();
                String bossNames = "";

                final String finalBossNames;
                if (drop.bosses != null && drop.bosses.length > 0) {
                    StringBuilder bossText = new StringBuilder();
                    for (int j = 0; j < drop.bosses.length; j++) {
                        NPCComposition npcComposition = client.getNpcDefinition(drop.bosses[j]);
                        bossText.append(npcComposition.getName());
                        if (j < drop.bosses.length - 1) bossText.append(", ");
                    }
                    finalBossNames = "<html><body style='width: 100%;'>" + bossText.toString() + "</body></html>";
                } else {
                    finalBossNames = null;
                }

                SwingUtilities.invokeLater(() -> {
                    if (!isFirstDrop) {
                        content.add(Box.createVerticalStrut(6));
                    }

                    JPanel itemRow = new JPanel(new BorderLayout());
                    itemRow.setOpaque(false);
                    itemRow.setAlignmentX(Component.LEFT_ALIGNMENT);

                    JPanel textContainer = new JPanel();
                    textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
                    textContainer.setOpaque(false);
                    textContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

                    JLabel itemLabel = new JLabel(name);
                    itemLabel.setFont(FontManager.getRunescapeFont());
                    itemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    textContainer.add(itemLabel);

                    if (finalBossNames != null) {
                        JPanel bossPanel = new JPanel();
                        bossPanel.setLayout(new BoxLayout(bossPanel, BoxLayout.X_AXIS));
                        bossPanel.setOpaque(false);
                        bossPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                        JLabel bossLabel = new JLabel(finalBossNames);
                        bossLabel.setFont(FontManager.getRunescapeSmallFont());
                        bossLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
                        bossPanel.add(bossLabel);

                        textContainer.add(Box.createVerticalStrut(2));
                        textContainer.add(bossPanel);
                    }

                    itemRow.add(textContainer, BorderLayout.CENTER);

                    String rightSideText = "";
                    if (drop.amount != null && drop.amount > 1) {
                        rightSideText = "x" + drop.amount;
                    } else if (drop.points != null) {
                        rightSideText = drop.points + (drop.points == 1 ? "pt" : " pts");
                    }

                    if (!rightSideText.isEmpty()) {
                        JLabel rightSideLabel = new JLabel(rightSideText);
                        rightSideLabel.setFont(FontManager.getRunescapeSmallFont());
                        itemRow.add(rightSideLabel, BorderLayout.EAST);
                    }

                    content.add(itemRow);
                    revalidate();
                    repaint();
                });
            });
        }
    }
}

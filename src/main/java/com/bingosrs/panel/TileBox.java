package com.bingosrs.panel;

import javax.inject.Inject;
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

    TileBox(Tile tile, boolean isCompleted, Client client, ClientThread clientThread)
    {
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(new EmptyBorder(3, 0, 3, 0), new LineBorder(ColorScheme.BORDER_COLOR, 1)));

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        add(innerPanel);

        JLabel headerLabel = new JLabel(tile.description);
        headerLabel.setFont(FontManager.getRunescapeBoldFont());
        innerPanel.add(headerLabel);

        if (tile instanceof PointTile) {
            PointTile pointTile = (PointTile) tile;
            JLabel pointsLabel = new JLabel("Required Points: " + pointTile.getRequiredPoints());
            innerPanel.add(pointsLabel);
            renderDrops(pointTile.getRequiredDrops(), innerPanel, client, clientThread, true);
        } else if (tile instanceof StandardTile) {
            StandardTile standardTile = (StandardTile) tile;
            RequiredDrop[][] groups = standardTile.getRequiredDropGroups();
            if (groups.length == 1) {
                renderDrops(groups[0], innerPanel, client, clientThread, false);
            } else {
                for (int i = 0; i < groups.length; i++) {
                    if (i > 0) {
                        JPanel orWrapper = new JPanel();
                        orWrapper.setOpaque(false);
                        orWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

                        JLabel orLabel = new JLabel("OR");
                        orWrapper.add(orLabel);
                        innerPanel.add(orWrapper);
                    }
                    JPanel groupPanel = new JPanel(new BorderLayout());
                    groupPanel.setBorder(new CompoundBorder(
                        new LineBorder(ColorScheme.DARKER_GRAY_COLOR, 1), new EmptyBorder(4, 4, 4, 4)
                    ));
                    groupPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                    JPanel dropsPanel = new JPanel();
                    dropsPanel.setLayout(new BoxLayout(dropsPanel, BoxLayout.Y_AXIS));
                    dropsPanel.setOpaque(false);
                    renderDrops(groups[i], dropsPanel, client, clientThread, false);
                    groupPanel.add(dropsPanel, BorderLayout.CENTER);

                    innerPanel.add(groupPanel);
                }
            }
        }
    }

    private void renderDrops(RequiredDrop[] drops, JPanel content, Client client, ClientThread clientThread, boolean showPoints) {
        for (RequiredDrop drop : drops) {
            clientThread.invoke(() -> {
                ItemComposition itemComposition = client.getItemDefinition(drop.item);
                String name = itemComposition.getMembersName();
                if (showPoints && drop.points != null) {
                    name += " (" + drop.points + " pts)";
                }
                final String labelText = name;
                SwingUtilities.invokeLater(() -> {
                    JLabel itemLabel = new JLabel(labelText);
                    itemLabel.setFont(FontManager.getRunescapeSmallFont());
                    itemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    content.add(itemLabel);
                    revalidate();
                    repaint();
                });
            });
            if (drop.bosses != null && drop.bosses.length > 0) {
                clientThread.invoke(() -> {
                    StringBuilder labelText = new StringBuilder("Bosses: ");
                    for (int i = 0; i < drop.bosses.length; i++) {
                        NPCComposition npcComposition = client.getNpcDefinition(drop.bosses[i]);
                        labelText.append(npcComposition.getName());
                        if (i < drop.bosses.length - 1) labelText.append(", ");
                    }
                    SwingUtilities.invokeLater(() -> {
                        JLabel bossLabel = new JLabel(labelText.toString());
                        bossLabel.setFont(FontManager.getRunescapeSmallFont());
                        bossLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                        content.add(bossLabel);
                        revalidate();
                        repaint();
                    });
                });
            }
        }
    }
}

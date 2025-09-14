package com.bingosrs.panel;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.bingosrs.api.model.RequiredDrop;
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
    private static final Color COMPLETED_COLOR = new Color(0, 50, 0);

    TileBox(Tile tile, RequiredDrop[] remainingDrops, Client client, ClientThread clientThread)
    {
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(new EmptyBorder(3, 0, 3, 0), new LineBorder(ColorScheme.BORDER_COLOR, 1)));

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BorderLayout());
        innerPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        if (remainingDrops != null && remainingDrops.length == 0) {
            innerPanel.setBackground(COMPLETED_COLOR);
        }
        add(innerPanel);

        JLabel headerLabel = new JLabel(tile.description);
        headerLabel.setFont(FontManager.getRunescapeBoldFont());
        innerPanel.add(headerLabel, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        innerPanel.add(content, BorderLayout.CENTER);

        RequiredDrop[] drops = tile.getRequiredDrops();

        for (RequiredDrop drop : drops) {
            ItemComposition itemComposition = client.getItemDefinition(drop.item);
            JLabel itemLabel = new JLabel(itemComposition.getMembersName());
            itemLabel.setFont(FontManager.getRunescapeSmallFont());
            itemLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            content.add(itemLabel);

            if (drop.bosses != null && drop.bosses.length > 0) {
                StringBuilder labelText = new StringBuilder("Bosses: ");

                for (int i = 0; i < drop.bosses.length; i++) {
                    NPCComposition npcComposition = client.getNpcDefinition(drop.bosses[i]);
                    labelText.append(npcComposition.getName());
                    if (i < drop.bosses.length - 1) {
                        labelText.append(", ");
                    }
                }
                JLabel bossLabel = new JLabel(labelText.toString());
                bossLabel.setFont(FontManager.getRunescapeSmallFont());
                bossLabel.setBorder(new EmptyBorder(0, 8, 0, 0));
                content.add(bossLabel);
            }
        }
    }
}

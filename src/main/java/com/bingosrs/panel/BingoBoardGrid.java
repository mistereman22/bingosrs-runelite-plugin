package com.bingosrs.panel;

import com.bingosrs.api.model.tile.Tile;
import net.runelite.client.ui.ColorScheme;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.function.Consumer;

public class BingoBoardGrid extends JPanel {
    public BingoBoardGrid(Tile[] tiles, int boardSize, int selectedIndex, Consumer<Integer> onTileSelected) {
        setLayout(new GridLayout(boardSize, boardSize, 2, 2));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        for (int i = 0; i < tiles.length; i++) {
            JButton tileButton = new JButton();
            tileButton.setPreferredSize(new Dimension(30, 30));
            tileButton.setFocusable(false);
            tileButton.setBackground(ColorScheme.DARK_GRAY_COLOR);

            if (i == selectedIndex) {
                tileButton.setBorder(new LineBorder(ColorScheme.BRAND_ORANGE, 2));
            } else {
                tileButton.setBorder(new LineBorder(ColorScheme.BORDER_COLOR, 1));
            }

            final int index = i;
            tileButton.addActionListener(e -> onTileSelected.accept(index));

            add(tileButton);
        }
    }
}

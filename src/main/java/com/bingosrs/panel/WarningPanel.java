package com.bingosrs.panel;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class WarningPanel extends JPanel {

    private static final Icon WARNING_ICON = scaleIcon(UIManager.getIcon("OptionPane.warningIcon"), 16, 16);

    WarningPanel(String text)
    {
        setLayout(new BorderLayout(8, 0));
        setBorder(new EmptyBorder(6, 0, 3, 0));

        JLabel label = new JLabel("<html><body style = 'text-align:left'>" + text + "</body></html>");
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setIcon(WARNING_ICON);

        add(label);
    }

    public static Icon scaleIcon(Icon icon, int width, int height) {
        if (icon == null) {
            return null;
        }

        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();

        Image scaledImage = bi.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }
}

package com.bingosrs.panel;

import com.bingosrs.api.model.Bingo;
import com.bingosrs.api.model.Drop;
import com.bingosrs.api.model.Team;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class BingoSummary extends JPanel {

    public BingoSummary(Bingo bingo, Team[] teams) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        JTextArea name = new JTextArea(bingo.name);
        name.setBorder(new EmptyBorder(0, 0, 0, 0));
        name.setOpaque(false);
        name.setFont(FontManager.getRunescapeBoldFont());
        name.setEditable(false);
        name.setLineWrap(true);
        name.setWrapStyleWord(true);
        infoPanel.add(name, BorderLayout.CENTER);

        JLabel state = new JLabel(bingo.state);
        state.setFont(FontManager.getRunescapeSmallFont());
        state.setHorizontalAlignment(SwingConstants.RIGHT);
        state.setVerticalAlignment(SwingConstants.TOP);
        state.setOpaque(false);
        infoPanel.add(state, BorderLayout.EAST);

        add(infoPanel);

        if (teams != null) {
            JTable teamsTable = new JTable(new TeamTableModel(teams));
            teamsTable.setRowSelectionAllowed(false);
            teamsTable.setFocusable(false);
            teamsTable.setFont(FontManager.getRunescapeFont());
            teamsTable.setEnabled(false);

            teamsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                final Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    if (c instanceof JComponent) {
                        ((JComponent) c).setBorder(padding);
                        c.setBackground(new Color(0, 0, 0, 0));
                        if (column > 0) {
                            ((JLabel)c).setHorizontalAlignment(SwingConstants.RIGHT);
                        } else {
                            ((JLabel)c).setHorizontalAlignment(SwingConstants.LEFT);
                        }
                    }

                    return c;
                }
            });

            JTableHeader tableHeader = teamsTable.getTableHeader();
            tableHeader.setFont(FontManager.getRunescapeBoldFont());
            tableHeader.setResizingAllowed(false);
            tableHeader.setReorderingAllowed(false);

            add(tableHeader);
            add(teamsTable);
        }
    }

    private static class TeamTableModel extends AbstractTableModel {

        private final Team[] teams;
        private final String[] columnNames = {"Team", "Drops", "Tiles"};

        public TeamTableModel(Team[] teams) {
            this.teams = teams;
        }

        @Override
        public int getRowCount() {
            return teams.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Team team = teams[rowIndex];

            switch (columnIndex) {
                case 0: // Team Name
                    return team.name;
                case 1: // Drops
                    return calculateDrops(team);
                case 2: // Tiles
                    return calculateCompletedTiles(team);
                default:
                    return null;
            }
        }

        private int calculateDrops(Team team) {
            int dropCount = 0;
            if (team.drops != null) {
                for (Drop[] tileDrops : team.drops) {
                    if (tileDrops != null) {
                        dropCount += tileDrops.length;
                    }
                }
            }
            return dropCount;
        }

        private int calculateCompletedTiles(Team team) {
            int completedTiles = 0;
            if (team.remainingDrops != null && team.drops != null) {
                for (int i = 0; i < team.remainingDrops.length; i++) {
                    // A tile is completed if it has no remaining drops
                    // AND has at least one drop obtained.
                    if (team.remainingDrops[i] == null || team.remainingDrops[i].length == 0) {
                        if (team.drops[i] != null && team.drops[i].length > 0) {
                            completedTiles++;
                        }
                    }
                }
            }
            return completedTiles;
        }
    }
}
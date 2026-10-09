package com.frauddetection.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int col) {
        String text = value == null ? "" : value.toString().toUpperCase();

        Color fg;
        Color bg;
        switch (text) {
            case "APPROVED":
            case "LOW":
                fg = UITheme.GREEN;
                bg = UITheme.GREEN_TINT;
                break;
            case "FLAGGED":
            case "MEDIUM":
                fg = UITheme.ORANGE;
                bg = UITheme.ORANGE_TINT;
                break;
            case "BLOCKED":
            case "HIGH":
            case "SUSPICIOUS":
                fg = UITheme.PINK;
                bg = UITheme.PINK_TINT;
                break;
            case "PENDING":
                fg = UITheme.SOFT_PINK;
                bg = new Color(0x35, 0x15, 0x28);
                break;
            default:
                fg = UITheme.MUTED;
                bg = new Color(0x13, 0x1B, 0x2E);
        }

        return new BadgeLabel(text, fg, bg);
    }

    private static class BadgeLabel extends JLabel {
        private final Color pillColor;

        BadgeLabel(String text, Color fg, Color bg) {
            super(text, SwingConstants.CENTER);
            this.pillColor = bg;
            setForeground(fg);
            setFont(UITheme.boldFont(11));
            setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            FontMetrics fm = g2.getFontMetrics(getFont());
            int textW = fm.stringWidth(getText());
            int pillW = Math.min(getWidth() - 4, textW + 20);
            int pillH = 20;
            int x = (getWidth() - pillW) / 2;
            int y = (getHeight() - pillH) / 2;

            g2.setColor(pillColor);
            g2.fillRoundRect(x, y, pillW, pillH, pillH, pillH);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

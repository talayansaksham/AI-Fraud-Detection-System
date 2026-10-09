package com.frauddetection.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MiniBarChart extends JPanel {

    public static class Bar {
        public final double value;
        public final boolean flagged;
        public Bar(double value, boolean flagged) { this.value = value; this.flagged = flagged; }
    }

    private List<Bar> bars = List.of();

    public MiniBarChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(100, 160));
    }

    public void setBars(List<Bar> bars) {
        this.bars = bars;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bars.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int fullW = getWidth(), fullH = getHeight();
        int leftPad = 8, rightPad = 8, topPad = 22, bottomPad = 22;
        int chartW = fullW - leftPad - rightPad;
        int chartH = fullH - topPad - bottomPad;
        int baselineY = topPad + chartH;

        double max = bars.stream().mapToDouble(b -> b.value).max().orElse(1);
        double scaleMax = max * 1.25;

        g2.setColor(new Color(0x13, 0x1B, 0x2E));
        g2.setStroke(new BasicStroke(1f));
        int gridLines = 3;
        for (int i = 1; i <= gridLines; i++) {
            int y = topPad + chartH * i / (gridLines + 1);
            g2.drawLine(leftPad, y, leftPad + chartW, y);
        }

        g2.setColor(new Color(0x1E, 0x2B, 0x48));
        g2.drawLine(leftPad, baselineY, leftPad + chartW, baselineY);

        int gap = 10;
        int barWidth = Math.max(10, (chartW - gap * (bars.size() - 1)) / bars.size());
        int x = leftPad;

        g2.setFont(UITheme.bodyFont(9));
        for (Bar bar : bars) {
            int barHeight = (int) (chartH * (bar.value / scaleMax));
            int y = baselineY - barHeight;
            Color color = bar.flagged ? UITheme.PINK : UITheme.BLUE;

            g2.setColor(color);
            g2.fillRoundRect(x, y, barWidth, Math.max(barHeight, 3), 4, 4);

            String label = formatAmount(bar.value);
            FontMetrics fm = g2.getFontMetrics();
            int labelW = fm.stringWidth(label);
            g2.setColor(UITheme.MUTED);
            g2.drawString(label, x + (barWidth - labelW) / 2f > 0 ? x + (barWidth - labelW) / 2 : x,
                    Math.max(10, y - 6));

            x += barWidth + gap;
        }

        g2.dispose();
    }

    private String formatAmount(double v) {
        if (v >= 1000) return String.format("%.1fk", v / 1000.0);
        return String.format("%.0f", v);
    }
}

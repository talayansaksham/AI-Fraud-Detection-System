package com.frauddetection.gui;

import javax.swing.*;
import java.awt.*;

public class RiskMeter extends JPanel {
    private int score = 0;
    private String caption = "Risk Confidence";

    public RiskMeter() {
        setOpaque(false);
        setPreferredSize(new Dimension(220, 54));
    }

    public void setScore(int score, String caption) {
        this.score = Math.max(0, Math.min(100, score));
        this.caption = caption;
        repaint();
    }

    private Color colorForScore(int s) {
        if (s < 40) return UITheme.GREEN;
        if (s < 70) return UITheme.ORANGE;
        return UITheme.PINK;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        Color fillColor = colorForScore(score);

        g2.setFont(UITheme.boldFont(11));
        g2.setColor(UITheme.MUTED);
        g2.drawString(caption.toUpperCase(), 0, 14);

        g2.setFont(UITheme.titleFont(18));
        g2.setColor(fillColor);
        String pctText = score + "%";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(pctText, w - fm.stringWidth(pctText), 18);

        int trackY = 28, trackH = 10;
        g2.setColor(new Color(0x13, 0x1B, 0x2E));
        g2.fillRoundRect(0, trackY, w, trackH, trackH, trackH);

        int fillW = (int) (w * (score / 100.0));
        if (fillW > 0) {
            g2.setColor(fillColor);
            g2.fillRoundRect(0, trackY, Math.max(trackH, fillW), trackH, trackH, trackH);
        }

        g2.dispose();
    }
}

package com.frauddetection.gui;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {

    private final int radius;
    private final Color background;
    private final Color borderColor;
    private boolean elevated = false;
    private Color gradientTo = null;
    private Color leftAccentColor = null;

    public RoundedPanel(int radius, Color background, Color borderColor) {
        this.radius = radius;
        this.background = background;
        this.borderColor = borderColor;
        setOpaque(false);
    }

    public RoundedPanel withElevation() {
        this.elevated = true;
        setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 4));
        return this;
    }

    public RoundedPanel withGradient(Color toColor) {
        this.gradientTo = toColor;
        return this;
    }

    public RoundedPanel withLeftAccent(Color accentColor) {
        this.leftAccentColor = accentColor;
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth() - (elevated ? 4 : 0);
        int h = getHeight() - (elevated ? 4 : 0);

        if (elevated) {
            for (int i = 4; i >= 1; i--) {
                g2.setColor(new Color(0x00, 0x00, 0x00, 25));
                g2.fillRoundRect(i, i + 2, w, h, radius, radius);
            }
        }

        if (gradientTo != null) {
            g2.setPaint(new GradientPaint(0, 0, background, w, h, gradientTo));
        } else {
            g2.setColor(background);
        }
        g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);

        if (leftAccentColor != null) {
            Shape oldClip = g2.getClip();
            g2.clip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w - 1, h - 1, radius, radius));
            g2.setColor(leftAccentColor);
            g2.fillRect(0, 0, 5, h);
            g2.setClip(oldClip);
        }

        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}

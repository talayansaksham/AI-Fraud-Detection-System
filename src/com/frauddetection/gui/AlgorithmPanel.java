package com.frauddetection.gui;

import com.frauddetection.AppContext;
import com.frauddetection.service.FraudDetector;
import com.frauddetection.service.MLClassifierDetector;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Admin panel for enabling/disabling individual fraud detection algorithms.
 * Toggle states are applied directly to the FraudDetectionService at runtime.
 */
public class AlgorithmPanel extends JPanel {

    private final AppContext ctx;
    private final Map<String, JCheckBox> checkBoxes = new LinkedHashMap<>();
    private JLabel statusLabel;

    public AlgorithmPanel(AppContext ctx) {
        this.ctx = ctx;

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        add(header(), BorderLayout.NORTH);
        add(formCard(), BorderLayout.CENTER);
    }

    private JPanel header() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel kicker = UITheme.sectionKicker("Admin");
        JLabel title = UITheme.sectionTitle("Algorithm Management");
        kicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(kicker);
        p.add(Box.createVerticalStrut(4));
        p.add(title);
        return p;
    }

    private JComponent formCard() {
        RoundedPanel card = new RoundedPanel(14, UITheme.CARD_BG, UITheme.LINE);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        int row = 0;

        // --- Info label ---
        gbc.gridy = row++;
        JLabel info = new JLabel("Select which detection algorithms to use:");
        info.setFont(UITheme.bodyFont(13));
        info.setForeground(UITheme.MUTED);
        card.add(info, gbc);

        // --- Checkboxes for each registered detector ---
        for (FraudDetector detector : ctx.detectionService.getDetectors()) {
            gbc.gridy = row++;
            JCheckBox cb = styledCheckBox(
                    detector.getName(),
                    ctx.detectionService.isDetectorEnabled(detector.getName())
            );
            checkBoxes.put(detector.getName(), cb);
            card.add(cb, gbc);
        }

        // --- Apply Button ---
        gbc.gridy = row++;
        gbc.insets = new Insets(16, 8, 8, 8);
        StyledButton applyBtn = new StyledButton("Apply Algorithm Configuration", UITheme.BLUE);
        applyBtn.addActionListener(e -> handleApply());
        card.add(applyBtn, gbc);

        // --- Reload ML Model Button ---
        gbc.gridy = row++;
        gbc.insets = new Insets(8, 8, 8, 8);
        StyledButton reloadBtn = new StyledButton("Reload / Update ML Model", UITheme.VIOLET);
        reloadBtn.addActionListener(e -> handleReloadML());
        card.add(reloadBtn, gbc);

        // --- Status Label ---
        gbc.gridy = row++;
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.boldFont(13));
        statusLabel.setForeground(UITheme.GREEN);
        card.add(statusLabel, gbc);

        // Constrain width
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        JPanel constrained = new JPanel(new BorderLayout());
        constrained.setOpaque(false);
        constrained.setPreferredSize(new Dimension(480, 440));
        constrained.add(card, BorderLayout.CENTER);
        wrap.add(constrained, BorderLayout.WEST);
        return wrap;
    }

    private JCheckBox styledCheckBox(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setFont(UITheme.bodyFont(14));
        cb.setForeground(UITheme.INK);
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return cb;
    }

    private void handleApply() {
        for (Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
            ctx.detectionService.setDetectorEnabled(entry.getKey(), entry.getValue().isSelected());
        }
        statusLabel.setForeground(UITheme.GREEN);
        statusLabel.setText("✓ Algorithm configuration applied successfully.");
    }

    private void handleReloadML() {
        try {
            ctx.mlClassifier.reloadModel("training_data.arff");
            statusLabel.setForeground(UITheme.GREEN);
            statusLabel.setText("✓ ML model reloaded successfully.");
        } catch (Exception ex) {
            statusLabel.setForeground(UITheme.PINK);
            statusLabel.setText("ML reload failed: " + ex.getMessage());
        }
    }

    public void refresh() {
        for (Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
            entry.getValue().setSelected(ctx.detectionService.isDetectorEnabled(entry.getKey()));
        }
    }
}

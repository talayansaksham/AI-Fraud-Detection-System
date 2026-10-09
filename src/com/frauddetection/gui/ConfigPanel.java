package com.frauddetection.gui;

import com.frauddetection.AppContext;

import javax.swing.*;
import java.awt.*;

/**
 * Admin panel for configuring fraud detection thresholds.
 * All values read from / write to the live detector instances in AppContext,
 * so changes take effect immediately on the next transaction evaluation.
 */
public class ConfigPanel extends JPanel {

    private final AppContext ctx;
    private JTextField amountField;
    private JTextField velocityCountField;
    private JTextField velocityWindowField;
    private JLabel statusLabel;

    public ConfigPanel(AppContext ctx) {
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
        JLabel title = UITheme.sectionTitle("System Configuration");
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

        // --- Amount Threshold ---
        gbc.gridy = row++;
        card.add(fieldLabel("Maximum Transaction Amount Threshold (₹)"), gbc);
        gbc.gridy = row++;
        amountField = new JTextField(String.valueOf(ctx.amountRule.getThreshold()));
        UITheme.styleTextField(amountField);
        card.add(amountField, gbc);

        // --- Velocity Count ---
        gbc.gridy = row++;
        card.add(fieldLabel("Maximum Transactions (Velocity Limit)"), gbc);
        gbc.gridy = row++;
        velocityCountField = new JTextField(String.valueOf(ctx.velocityRule.getMaxTransactions()));
        UITheme.styleTextField(velocityCountField);
        card.add(velocityCountField, gbc);

        // --- Velocity Window ---
        gbc.gridy = row++;
        card.add(fieldLabel("Time Window (minutes)"), gbc);
        gbc.gridy = row++;
        velocityWindowField = new JTextField(String.valueOf(ctx.velocityRule.getWindowMinutes()));
        UITheme.styleTextField(velocityWindowField);
        card.add(velocityWindowField, gbc);

        // --- Save Button ---
        gbc.gridy = row++;
        gbc.insets = new Insets(16, 8, 8, 8);
        StyledButton saveBtn = new StyledButton("Save & Apply Configuration", UITheme.BLUE);
        saveBtn.addActionListener(e -> handleSave());
        card.add(saveBtn, gbc);

        // --- Status Label ---
        gbc.gridy = row++;
        gbc.insets = new Insets(8, 8, 8, 8);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.boldFont(13));
        statusLabel.setForeground(UITheme.GREEN);
        card.add(statusLabel, gbc);

        // Constrain width
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        JPanel constrained = new JPanel(new BorderLayout());
        constrained.setOpaque(false);
        constrained.setPreferredSize(new Dimension(480, 420));
        constrained.add(card, BorderLayout.CENTER);
        wrap.add(constrained, BorderLayout.WEST);
        return wrap;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.boldFont(12));
        l.setForeground(UITheme.INK);
        return l;
    }

    private void handleSave() {
        try {
            double newThreshold = Double.parseDouble(amountField.getText().trim());
            int newMaxTxn = Integer.parseInt(velocityCountField.getText().trim());
            int newWindow = Integer.parseInt(velocityWindowField.getText().trim());

            if (newThreshold <= 0 || newMaxTxn <= 0 || newWindow <= 0) {
                statusLabel.setForeground(UITheme.PINK);
                statusLabel.setText("All values must be greater than zero.");
                return;
            }

            // Apply to live detector instances
            ctx.amountRule.setThreshold(newThreshold);
            ctx.velocityRule.setMaxTransactions(newMaxTxn);
            ctx.velocityRule.setWindowMinutes(newWindow);

            statusLabel.setForeground(UITheme.GREEN);
            statusLabel.setText("✓ Configuration updated successfully.");

        } catch (NumberFormatException ex) {
            statusLabel.setForeground(UITheme.PINK);
            statusLabel.setText("Invalid input — please enter valid numbers.");
        }
    }

    public void refresh() {
        if (amountField != null) {
            amountField.setText(String.valueOf(ctx.amountRule.getThreshold()));
        }
        if (velocityCountField != null) {
            velocityCountField.setText(String.valueOf(ctx.velocityRule.getMaxTransactions()));
        }
        if (velocityWindowField != null) {
            velocityWindowField.setText(String.valueOf(ctx.velocityRule.getWindowMinutes()));
        }
    }
}

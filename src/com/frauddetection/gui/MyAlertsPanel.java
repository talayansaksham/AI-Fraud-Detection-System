package com.frauddetection.gui;

import com.frauddetection.dao.AlertDAO;
import com.frauddetection.dao.TransactionDAO;
import com.frauddetection.model.Alert;
import com.frauddetection.model.User;
import com.frauddetection.service.DetectionResult;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyAlertsPanel extends JPanel {

    private final AlertDAO alertDAO;
    private final TransactionDAO transactionDAO;
    private final User currentUser;
    private DefaultTableModel tableModel;
    private JTable table;
    private RiskMeter riskMeter;

    public MyAlertsPanel(AlertDAO alertDAO, TransactionDAO transactionDAO, User currentUser) {
        this.alertDAO = alertDAO;
        this.transactionDAO = transactionDAO;
        this.currentUser = currentUser;

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        add(header(), BorderLayout.NORTH);
        add(tableCard(), BorderLayout.CENTER);

        refresh();
    }

    private JPanel header() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(UITheme.sectionKicker("Security"));
        p.add(Box.createVerticalStrut(4));
        p.add(UITheme.sectionTitle("Was This You?"));
        return p;
    }

    private JComponent tableCard() {
        RoundedPanel card = new RoundedPanel(14, UITheme.CARD_BG, UITheme.LINE).withElevation();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel hint = new JLabel("Select a flagged transaction below to see its risk confidence, then confirm whether it was you.");
        hint.setFont(UITheme.bodyFont(12));
        hint.setForeground(UITheme.MUTED);

        tableModel = new DefaultTableModel(
                new Object[]{"Txn ID", "Risk Level", "Reason", "Flagged At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = UITheme.zebraTable(tableModel);
        UITheme.styleTableHeader(table.getTableHeader());
        table.setRowHeight(30);
        table.getColumnModel().getColumn(1).setCellRenderer(new StatusBadgeRenderer());
        table.getColumnModel().getColumn(2).setPreferredWidth(360);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::onRowSelected);

        RoundedPanel meterCard = new RoundedPanel(12, UITheme.INPUT_BG, UITheme.LINE);
        meterCard.setLayout(new BorderLayout());
        meterCard.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        riskMeter = new RiskMeter();
        meterCard.add(riskMeter, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonRow.setOpaque(false);

        StyledButton confirmMe = new StyledButton("Yes, this was me", UITheme.GREEN);
        confirmMe.addActionListener(e -> respondToSelected(true));

        StyledButton denyMe = new StyledButton("No, this wasn't me", UITheme.PINK);
        denyMe.addActionListener(e -> respondToSelected(false));

        buttonRow.add(confirmMe);
        buttonRow.add(denyMe);

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        meterCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        meterCard.setMaximumSize(new Dimension(280, 70));
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        south.add(hint);
        south.add(Box.createVerticalStrut(10));
        south.add(meterCard);
        south.add(Box.createVerticalStrut(10));
        south.add(buttonRow);

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    private void onRowSelected(javax.swing.event.ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = table.getSelectedRow();
        if (row < 0) {
            riskMeter.setScore(0, "Select a transaction");
            return;
        }
        String riskLevel = (String) tableModel.getValueAt(row, 1);
        int score = DetectionResult.scoreForLevel(riskLevel);
        riskMeter.setScore(score, "Risk Confidence");
    }

    private void respondToSelected(boolean wasMe) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a transaction first.", "Nothing Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int txnId = (int) tableModel.getValueAt(row, 0);
        try {
            transactionDAO.updateStatus(txnId, wasMe ? "APPROVED" : "BLOCKED");
            JOptionPane.showMessageDialog(this,
                    wasMe ? "Thanks for confirming — no further action needed."
                          : "Thanks for letting us know. This transaction has been blocked and flagged for investigation.",
                    "Response Recorded", JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not update transaction: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh() {
        try {
            List<Alert> mine = alertDAO.getAlertsByUser(currentUser.getUserId());
            tableModel.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM, HH:mm:ss");
            for (Alert a : mine) {
                tableModel.addRow(new Object[]{
                        a.getTxnId(), a.getRiskLevel(), a.getReason(), a.getCreatedAt().format(fmt)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load your alerts: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

package com.frauddetection.gui;

import com.frauddetection.dao.AlertDAO;
import com.frauddetection.dao.TransactionDAO;
import com.frauddetection.model.Transaction;
import com.frauddetection.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CustomerHomePanel extends JPanel {

    private final TransactionDAO transactionDAO;
    private final AlertDAO alertDAO;
    private final User currentUser;

    private JLabel totalLabel;
    private JLabel flaggedLabel;
    private DefaultTableModel tableModel;

    public CustomerHomePanel(TransactionDAO transactionDAO, AlertDAO alertDAO, User currentUser) {
        this.transactionDAO = transactionDAO;
        this.alertDAO = alertDAO;
        this.currentUser = currentUser;

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        add(header(), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(statCardsRow());
        center.add(Box.createVerticalStrut(16));
        center.add(transactionsCard());
        add(center, BorderLayout.CENTER);

        refresh();
    }

    private JPanel header() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(UITheme.sectionKicker("Welcome back"));
        p.add(Box.createVerticalStrut(4));
        p.add(UITheme.sectionTitle(currentUser.getUsername()));
        return p;
    }

    private JPanel statCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        totalLabel = new JLabel("0");
        flaggedLabel = new JLabel("0");

        row.add(statCard("My Transactions", totalLabel, UITheme.BLUE, UITheme.BLUE_TINT));
        row.add(statCard("Flagged for Review", flaggedLabel, UITheme.PINK, UITheme.PINK_TINT));
        return row;
    }

    private RoundedPanel statCard(String label, JLabel valueLabel, Color accent, Color tint) {
        RoundedPanel card = new RoundedPanel(14, UITheme.CARD_BG, UITheme.LINE)
                .withElevation().withGradient(tint).withLeftAccent(accent);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 18));

        JLabel title = new JLabel(label.toUpperCase());
        title.setFont(UITheme.boldFont(11));
        title.setForeground(UITheme.MUTED);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(UITheme.titleFont(30));
        valueLabel.setForeground(accent);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(valueLabel);
        return card;
    }

    private RoundedPanel transactionsCard() {
        RoundedPanel card = new RoundedPanel(14, UITheme.CARD_BG, UITheme.LINE).withElevation();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("My Recent Transactions");
        title.setFont(UITheme.bodyFont(12));
        title.setForeground(UITheme.MUTED);

        tableModel = new DefaultTableModel(
                new Object[]{"Txn ID", "Amount", "Location", "Category", "Time", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = UITheme.zebraTable(tableModel);
        UITheme.styleTableHeader(table.getTableHeader());
        table.setRowHeight(30);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        card.add(title, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        try {
            List<Transaction> mine = transactionDAO.getTransactionsByUser(currentUser.getUserId());
            long flagged = mine.stream().filter(t -> "FLAGGED".equals(t.getStatus())).count();

            totalLabel.setText(String.valueOf(mine.size()));
            flaggedLabel.setText(String.valueOf(flagged));

            tableModel.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM, HH:mm");
            for (Transaction t : mine) {
                tableModel.addRow(new Object[]{
                        t.getTxnId(), String.format("%.2f", t.getAmount()),
                        t.getLocation(), t.getMerchantCategory(), t.getTimestamp().format(fmt), t.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load your transactions: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

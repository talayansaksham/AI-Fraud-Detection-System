package com.frauddetection.gui;

import com.frauddetection.dao.AlertDAO;
import com.frauddetection.dao.TransactionDAO;
import com.frauddetection.model.Transaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel {

    private final TransactionDAO transactionDAO;
    private final AlertDAO alertDAO;

    private JLabel totalValueLabel;
    private JLabel flaggedValueLabel;
    private JLabel alertsValueLabel;
    private MiniBarChart chart;
    private DefaultTableModel tableModel;

    public DashboardPanel(TransactionDAO transactionDAO, AlertDAO alertDAO) {
        this.transactionDAO = transactionDAO;
        this.alertDAO = alertDAO;

        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        add(header(), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(statCardsRow());
        center.add(Box.createVerticalStrut(16));
        center.add(chartCard());
        center.add(Box.createVerticalStrut(16));
        center.add(recentTransactionsCard());

        add(center, BorderLayout.CENTER);
        refresh();
    }

    private JPanel header() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel kicker = UITheme.sectionKicker("Overview");
        JLabel title = UITheme.sectionTitle("Dashboard");
        kicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(kicker);
        p.add(Box.createVerticalStrut(4));
        p.add(title);
        return p;
    }

    private JPanel statCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        totalValueLabel = new JLabel("0");
        flaggedValueLabel = new JLabel("0");
        alertsValueLabel = new JLabel("0");

        row.add(statCard("Total Transactions", totalValueLabel, UITheme.BLUE, UITheme.BLUE_TINT));
        row.add(statCard("Flagged", flaggedValueLabel, UITheme.PINK, UITheme.PINK_TINT));
        row.add(statCard("Alerts Raised", alertsValueLabel, UITheme.SOFT_PINK, UITheme.PINK_TINT));
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

    private RoundedPanel chartCard() {
        RoundedPanel card = new RoundedPanel(14, UITheme.CARD_BG, UITheme.LINE).withElevation();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel title = new JLabel("Recent Transaction Amounts (pink = flagged)");
        title.setFont(UITheme.bodyFont(12));
        title.setForeground(UITheme.MUTED);

        chart = new MiniBarChart();
        card.add(title, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private RoundedPanel recentTransactionsCard() {
        RoundedPanel card = new RoundedPanel(14, UITheme.CARD_BG, UITheme.LINE).withElevation();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Recent Transactions");
        title.setFont(UITheme.bodyFont(12));
        title.setForeground(UITheme.MUTED);

        tableModel = new DefaultTableModel(
                new Object[]{"Txn ID", "User ID", "Amount", "Location", "Category", "Time", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = UITheme.zebraTable(tableModel);
        UITheme.styleTableHeader(table.getTableHeader());
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        scroll.setPreferredSize(new Dimension(100, 220));

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        try {
            List<Transaction> all = transactionDAO.getAllTransactions();
            long flaggedCount = all.stream().filter(t -> "FLAGGED".equals(t.getStatus())).count();
            int alertCount = alertDAO.getAllAlerts().size();

            totalValueLabel.setText(String.valueOf(all.size()));
            flaggedValueLabel.setText(String.valueOf(flaggedCount));
            alertsValueLabel.setText(String.valueOf(alertCount));

            List<Transaction> recent = all.stream().limit(8).collect(Collectors.toList());
            chart.setBars(recent.stream()
                    .map(t -> new MiniBarChart.Bar(t.getAmount(), "FLAGGED".equals(t.getStatus())))
                    .collect(Collectors.toList()));

            tableModel.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM, HH:mm");
            for (Transaction t : recent) {
                tableModel.addRow(new Object[]{
                        t.getTxnId(), t.getUserId(), String.format("%.2f", t.getAmount()),
                        t.getLocation(), t.getMerchantCategory(), t.getTimestamp().format(fmt), t.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load dashboard data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

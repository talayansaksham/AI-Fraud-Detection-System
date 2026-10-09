package com.frauddetection.gui;

import java.awt.BorderLayout;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.frauddetection.dao.AlertDAO;
import com.frauddetection.model.Alert;

public class AlertsPanel extends JPanel {

    private final AlertDAO alertDAO;
    private DefaultTableModel tableModel;

    public AlertsPanel(AlertDAO alertDAO) {
        this.alertDAO = alertDAO;

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
        p.add(UITheme.sectionKicker("Fraud Review"));
        p.add(Box.createVerticalStrut(4));
        p.add(UITheme.sectionTitle("Alerts"));
        return p;
    }

    private JComponent tableCard() {
        RoundedPanel card = new RoundedPanel(14, UITheme.CARD_BG, UITheme.LINE).withElevation();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        tableModel = new DefaultTableModel(
                new Object[]{"Alert ID", "Txn ID", "Risk Level", "Reason", "Raised At", "Resolution"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = UITheme.zebraTable(tableModel);
        UITheme.styleTableHeader(table.getTableHeader());
        table.setRowHeight(30);
        table.getColumnModel().getColumn(2).setCellRenderer(new StatusBadgeRenderer());
       table.getColumnModel().getColumn(0).setPreferredWidth(60);
table.getColumnModel().getColumn(1).setPreferredWidth(60);
table.getColumnModel().getColumn(2).setPreferredWidth(90);
table.getColumnModel().getColumn(3).setPreferredWidth(320);
table.getColumnModel().getColumn(4).setPreferredWidth(170);
table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        try {
            List<Alert> alerts = alertDAO.getAllAlerts();
            tableModel.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM, HH:mm:ss");
            for (Alert a : alerts) {
                String resolution = "FLAGGED".equals(a.getTransactionStatus())
                        ? "PENDING" : a.getTransactionStatus();
                tableModel.addRow(new Object[]{
                        a.getAlertId(), a.getTxnId(), a.getRiskLevel(), a.getReason(),
                        a.getCreatedAt().format(fmt), resolution
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load alerts: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

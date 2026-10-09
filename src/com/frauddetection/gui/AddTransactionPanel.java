package com.frauddetection.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.frauddetection.dao.TransactionDAO;
import com.frauddetection.dao.UserDAO;
import com.frauddetection.exception.InvalidTransactionException;
import com.frauddetection.model.Transaction;
import com.frauddetection.model.User;
import com.frauddetection.service.DetectionResult;
import com.frauddetection.service.FraudDetectionService;

public class AddTransactionPanel extends JPanel {

    private final UserDAO userDAO;
    private final TransactionDAO transactionDAO;
    private final FraudDetectionService detectionService;
    private final Runnable onTransactionAdded;

    private JComboBox<User> userDropdown;
    private JTextField amountField;
    private JTextField locationField;
    private JComboBox<String> categoryDropdown;
    private JTextArea resultArea;
    private RiskMeter riskMeter;

    public AddTransactionPanel(UserDAO userDAO, TransactionDAO transactionDAO,
                                FraudDetectionService detectionService, Runnable onTransactionAdded) {
        this.userDAO = userDAO;
        this.transactionDAO = transactionDAO;
        this.detectionService = detectionService;
        this.onTransactionAdded = onTransactionAdded;

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel kicker = UITheme.sectionKicker("New Entry");
        JLabel title = UITheme.sectionTitle("Add Transaction");
        p.add(kicker);
        p.add(Box.createVerticalStrut(4));
        p.add(title);
        return p;
    }

    private JComponent buildForm() {
        RoundedPanel card = new RoundedPanel(14, UITheme.CARD_BG, UITheme.LINE);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        int row = 0;

        gbc.gridy = row++;
        card.add(fieldLabel("User"), gbc);
        gbc.gridy = row++;
        userDropdown = new JComboBox<>();
        reloadUsers();
        styleCombo(userDropdown);
        card.add(userDropdown, gbc);

        gbc.gridy = row++;
        card.add(fieldLabel("Amount"), gbc);
        gbc.gridy = row++;
        amountField = new JTextField();
        UITheme.styleTextField(amountField);
        card.add(amountField, gbc);

        gbc.gridy = row++;
        card.add(fieldLabel("Location"), gbc);
        gbc.gridy = row++;
        locationField = new JTextField();
        UITheme.styleTextField(locationField);
        card.add(locationField, gbc);

        gbc.gridy = row++;
        card.add(fieldLabel("Merchant Category"), gbc);
        gbc.gridy = row++;
        categoryDropdown = new JComboBox<>(new String[]{
                "Groceries", "Electronics", "Travel", "Dining", "ATM Withdrawal", "Other"});
        styleCombo(categoryDropdown);
        card.add(categoryDropdown, gbc);

        gbc.gridy = row++;
        StyledButton submit = new StyledButton("Add & Check for Fraud", UITheme.BLUE);
        submit.addActionListener(e -> handleSubmit());
        card.add(submit, gbc);

        gbc.gridy = row++;
        RoundedPanel meterCard = new RoundedPanel(12, UITheme.INPUT_BG, UITheme.LINE);
        meterCard.setPreferredSize(new Dimension(400, 78));
meterCard.setMinimumSize(new Dimension(100, 78));
        meterCard.setLayout(new BorderLayout());
        meterCard.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        riskMeter = new RiskMeter();
        meterCard.add(riskMeter, BorderLayout.CENTER);
        card.add(meterCard, gbc);

        gbc.gridy = row++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        resultArea = new JTextArea(4, 20);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(UITheme.bodyFont(13));
        resultArea.setBackground(UITheme.INPUT_BG);
        resultArea.setForeground(UITheme.INK);
        resultArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setPreferredSize(new Dimension(400, 90));
        resultScroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(resultScroll, gbc);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        JPanel constrained = new JPanel(new BorderLayout());
        constrained.setOpaque(false);
        constrained.setPreferredSize(new Dimension(480, 560));
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

    private void styleCombo(JComboBox<?> combo) {
        UITheme.styleComboBox(combo);
    }

    public void reloadUsers() {
        userDropdown.removeAllItems();
        try {
            List<User> users = userDAO.getAllUsers();
            for (User u : users) userDropdown.addItem(u);
        } catch (SQLException e) {
            resultArea.setText("Could not load users: " + e.getMessage());
        }
    }

    private void handleSubmit() {
        try {
            Transaction txn = buildAndValidateTransaction();
            int txnId = transactionDAO.addTransaction(txn);
            txn.setTxnId(txnId);

            DetectionResult result = detectionService.evaluate(txn);
            riskMeter.setScore(result.getRiskScore(), "Risk Confidence");

            if (result.isFlagged()) {
                resultArea.setForeground(UITheme.PINK);
                resultArea.setText("FLAGGED (" + result.getRiskLevel() + ")\n" + result.getReasonSummary());
            } else {
                resultArea.setForeground(UITheme.GREEN);
                resultArea.setText("Approved — no rules triggered.");
            }

            amountField.setText("");
            locationField.setText("");
            if (onTransactionAdded != null) onTransactionAdded.run();

        } catch (InvalidTransactionException ex) {
            resultArea.setForeground(UITheme.PINK);
            resultArea.setText("Invalid input: " + ex.getMessage());
        } catch (SQLException ex) {
            resultArea.setForeground(UITheme.PINK);
            resultArea.setText("Database error: " + ex.getMessage());
        }
    }

    private Transaction buildAndValidateTransaction() throws InvalidTransactionException {
        User selectedUser = (User) userDropdown.getSelectedItem();
        if (selectedUser == null) {
            throw new InvalidTransactionException("Please add a user first.");
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            throw new InvalidTransactionException("Amount must be a valid number.", e);
        }
        if (amount <= 0) {
            throw new InvalidTransactionException("Amount must be greater than zero.");
        }

        String location = locationField.getText().trim();
        if (location.isEmpty()) {
            throw new InvalidTransactionException("Location cannot be empty.");
        }

        String category = (String) categoryDropdown.getSelectedItem();
        return new Transaction(selectedUser.getUserId(), amount, location, category);
    }
}

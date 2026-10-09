package com.frauddetection.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.frauddetection.dao.TransactionDAO;
import com.frauddetection.exception.InvalidTransactionException;
import com.frauddetection.model.Transaction;
import com.frauddetection.model.User;
import com.frauddetection.service.DetectionResult;
import com.frauddetection.service.FraudDetectionService;

public class MakePaymentPanel extends JPanel {

    private final TransactionDAO transactionDAO;
    private final FraudDetectionService detectionService;
    private final User currentUser;
    private final Runnable onTransactionAdded;

    private JTextField amountField;
    private JTextField locationField;
    private JComboBox<String> categoryDropdown;
    private JTextArea resultArea;
    private RiskMeter riskMeter;

    public MakePaymentPanel(TransactionDAO transactionDAO, FraudDetectionService detectionService,
                             User currentUser, Runnable onTransactionAdded) {
        this.transactionDAO = transactionDAO;
        this.detectionService = detectionService;
        this.currentUser = currentUser;
        this.onTransactionAdded = onTransactionAdded;

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        add(header(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
    }

    private JPanel header() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(UITheme.sectionKicker("New Payment"));
        p.add(Box.createVerticalStrut(4));
        p.add(UITheme.sectionTitle("Make a Payment"));
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
        card.add(fieldLabel("Paying as"), gbc);
        gbc.gridy = row++;
        JLabel who = new JLabel(currentUser.getUsername() + " (" + currentUser.getEmail() + ")");
        who.setFont(UITheme.bodyFont(13));
        who.setForeground(UITheme.MUTED);
        card.add(who, gbc);

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
        UITheme.styleComboBox(categoryDropdown);
        card.add(categoryDropdown, gbc);

        gbc.gridy = row++;
        StyledButton submit = new StyledButton("Pay Now", UITheme.BLUE);
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
        constrained.setPreferredSize(new Dimension(460, 600));
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

    private void handleSubmit() {
        try {
            Transaction txn = buildAndValidateTransaction();
            int txnId = transactionDAO.addTransaction(txn);
            txn.setTxnId(txnId);

            DetectionResult result = detectionService.evaluate(txn);
            riskMeter.setScore(result.getRiskScore(), "Risk Confidence");

            if (result.isFlagged()) {
                resultArea.setForeground(UITheme.PINK);
                resultArea.setText("This payment was flagged for review (" + result.getRiskLevel() + ").\n"
                        + "Our fraud team will check: " + result.getReasonSummary());
                JOptionPane.showMessageDialog(this,
                        "⚠️ Fraud Security Alert!\n\n"
                        + "Your transaction of ₹" + String.format("%.2f", txn.getAmount()) + " has been flagged for potential fraud (" + result.getRiskLevel() + " risk).\n"
                        + "Reason: " + result.getReasonSummary() + "\n\n"
                        + "You can review and confirm this transaction in the 'Was This You?' tab.",
                        "Fraud Detection Alert", JOptionPane.WARNING_MESSAGE);
            } else {
                resultArea.setForeground(UITheme.GREEN);
                resultArea.setText("Payment successful.");
            }

            amountField.setText("");
            locationField.setText("");
            if (onTransactionAdded != null) onTransactionAdded.run();

        } catch (InvalidTransactionException ex) {
            resultArea.setForeground(UITheme.PINK);
            resultArea.setText("Invalid input: " + ex.getMessage());
        } catch (SQLException ex) {
            resultArea.setForeground(UITheme.PINK);
            resultArea.setText("Something went wrong: " + ex.getMessage());
        }
    }

    private Transaction buildAndValidateTransaction() throws InvalidTransactionException {
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
        return new Transaction(currentUser.getUserId(), amount, location, category);
    }
}

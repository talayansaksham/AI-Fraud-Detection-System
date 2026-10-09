package com.frauddetection.gui;

import com.frauddetection.dao.UserDAO;
import com.frauddetection.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class UsersPanel extends JPanel {

    private final UserDAO userDAO;
    private final Runnable onUserAdded;

    private JTextField usernameField;
    private JTextField emailField;
    private JTextField locationField;
    private JPasswordField passwordField;
    private DefaultTableModel tableModel;

    public UsersPanel(UserDAO userDAO, Runnable onUserAdded) {
        this.userDAO = userDAO;
        this.onUserAdded = onUserAdded;

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        add(header(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(20, 0));
        center.setOpaque(false);
        center.add(formCard(), BorderLayout.WEST);
        center.add(listCard(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        refresh();
    }

    private JPanel header() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(UITheme.sectionKicker("People"));
        p.add(Box.createVerticalStrut(4));
        p.add(UITheme.sectionTitle("Users"));
        return p;
    }

    private JComponent formCard() {
        RoundedPanel card = new RoundedPanel(14, UITheme.CARD_BG, UITheme.LINE);
        card.setPreferredSize(new Dimension(320, 400));
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        int row = 0;

        gbc.gridy = row++;
        card.add(label("Username"), gbc);
        gbc.gridy = row++;
        usernameField = field();
        card.add(usernameField, gbc);

        gbc.gridy = row++;
        card.add(label("Email"), gbc);
        gbc.gridy = row++;
        emailField = field();
        card.add(emailField, gbc);

        gbc.gridy = row++;
        card.add(label("Usual Location"), gbc);
        gbc.gridy = row++;
        locationField = field();
        card.add(locationField, gbc);

        gbc.gridy = row++;
        card.add(label("Login Password"), gbc);
        gbc.gridy = row++;
        passwordField = new JPasswordField();
        styleField(passwordField);
        card.add(passwordField, gbc);

        gbc.gridy = row++;
        StyledButton addBtn = new StyledButton("Add User", UITheme.BLUE);
        addBtn.addActionListener(e -> handleAdd());
        card.add(addBtn, gbc);

        return card;
    }

    private JComponent listCard() {
        RoundedPanel card = new RoundedPanel(14, UITheme.CARD_BG, UITheme.LINE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("All Users");
        title.setFont(UITheme.bodyFont(12));
        title.setForeground(UITheme.MUTED);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Username", "Email", "Usual Location"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = UITheme.zebraTable(tableModel);
        UITheme.styleTableHeader(table.getTableHeader());
        table.setRowHeight(30);

        card.add(title, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.boldFont(12));
        l.setForeground(UITheme.INK);
        return l;
    }

    private JTextField field() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        UITheme.styleTextField(f);
    }

    private void handleAdd() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String location = locationField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || email.isEmpty() || location.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Missing Info",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            userDAO.addUser(new User(username, email, location, password));
            usernameField.setText("");
            emailField.setText("");
            locationField.setText("");
            passwordField.setText("");
            refresh();
            if (onUserAdded != null) onUserAdded.run();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not add user: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh() {
        try {
            List<User> users = userDAO.getAllUsers();
            tableModel.setRowCount(0);
            for (User u : users) {
                tableModel.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getEmail(), u.getUsualLocation()});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load users: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

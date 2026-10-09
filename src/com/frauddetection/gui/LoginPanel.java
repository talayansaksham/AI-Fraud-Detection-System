package com.frauddetection.gui;

import com.frauddetection.dao.UserDAO;
import com.frauddetection.model.User;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {

    private final UserDAO userDAO;
    private final Consumer<User> onLoginSuccess;

    private final CardLayout modeLayout = new CardLayout();
    private final JPanel modeHost = new JPanel();

    private JTextField loginUsername;
    private JPasswordField loginPassword;
    private JLabel loginError;

    private JTextField suUsername;
    private JTextField suEmail;
    private JTextField suLocation;
    private JPasswordField suPassword;
    private JPasswordField suConfirmPassword;
    private JLabel signupError;

    public LoginPanel(UserDAO userDAO, Consumer<User> onLoginSuccess) {
        this.userDAO = userDAO;
        this.onLoginSuccess = onLoginSuccess;

        setLayout(new GridBagLayout());
        setBackground(UITheme.BG);

        modeHost.setOpaque(false);
        modeHost.setLayout(modeLayout);
        modeHost.add(buildLoginCard(), "login");
        modeHost.add(buildSignupCard(), "signup");

        add(modeHost);
    }

    private JComponent buildLoginCard() {
        RoundedPanel card = new RoundedPanel(16, UITheme.CARD_BG, UITheme.LINE).withElevation();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(380, 430));
        card.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        GridBagConstraints gbc = baseGbc();
        int row = 0;

        gbc.gridy = row++;
        card.add(brandRow(), gbc);

        gbc.gridy = row++;
        card.add(UITheme.sectionKicker("Customer Portal"), gbc);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 8, 20, 8);
        card.add(UITheme.sectionTitle("Log In"), gbc);
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridy = row++;
        card.add(label("Username"), gbc);
        gbc.gridy = row++;
        loginUsername = new JTextField();
        UITheme.styleTextField(loginUsername);
        card.add(loginUsername, gbc);

        gbc.gridy = row++;
        card.add(label("Password"), gbc);
        gbc.gridy = row++;
        loginPassword = new JPasswordField();
        UITheme.styleTextField(loginPassword);
        loginPassword.addActionListener(e -> handleLogin());
        card.add(loginPassword, gbc);

        gbc.gridy = row++;
        loginError = new JLabel(" ");
        loginError.setFont(UITheme.bodyFont(12));
        loginError.setForeground(UITheme.PINK);
        card.add(loginError, gbc);

        gbc.gridy = row++;
        StyledButton loginBtn = new StyledButton("Log In", UITheme.BLUE);
        loginBtn.addActionListener(e -> handleLogin());
        card.add(loginBtn, gbc);

        gbc.gridy = row++;
        JButton toSignup = linkButton("New here? Create an account");
        toSignup.addActionListener(e -> {
            loginError.setText(" ");
            modeLayout.show(modeHost, "signup");
        });
        card.add(toSignup, gbc);

        gbc.gridy = row++;
        JLabel hint = new JLabel("Demo accounts: rahul_k / priya_s / aman_v — password: pass123");
        hint.setFont(UITheme.bodyFont(11));
        hint.setForeground(UITheme.MUTED);
        card.add(hint, gbc);

        return card;
    }

    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = new String(loginPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            loginError.setText("Please enter both username and password.");
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user == null) {
                loginError.setText("Invalid username or password.");
                loginPassword.setText("");
                return;
            }
            loginError.setText(" ");
            onLoginSuccess.accept(user);
        } catch (SQLException e) {
            loginError.setText("Database error: " + e.getMessage());
        }
    }

    private JComponent buildSignupCard() {
        RoundedPanel card = new RoundedPanel(16, UITheme.CARD_BG, UITheme.LINE).withElevation();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(380, 560));
        card.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        GridBagConstraints gbc = baseGbc();
        int row = 0;

        gbc.gridy = row++;
        card.add(brandRow(), gbc);

        gbc.gridy = row++;
        card.add(UITheme.sectionKicker("Customer Portal"), gbc);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 8, 20, 8);
        card.add(UITheme.sectionTitle("Create Account"), gbc);
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridy = row++;
        card.add(label("Username"), gbc);
        gbc.gridy = row++;
        suUsername = new JTextField();
        UITheme.styleTextField(suUsername);
        card.add(suUsername, gbc);

        gbc.gridy = row++;
        card.add(label("Email"), gbc);
        gbc.gridy = row++;
        suEmail = new JTextField();
        UITheme.styleTextField(suEmail);
        card.add(suEmail, gbc);

        gbc.gridy = row++;
        card.add(label("Usual Location"), gbc);
        gbc.gridy = row++;
        suLocation = new JTextField();
        UITheme.styleTextField(suLocation);
        card.add(suLocation, gbc);

        gbc.gridy = row++;
        card.add(label("Password"), gbc);
        gbc.gridy = row++;
        suPassword = new JPasswordField();
        UITheme.styleTextField(suPassword);
        card.add(suPassword, gbc);

        gbc.gridy = row++;
        card.add(label("Confirm Password"), gbc);
        gbc.gridy = row++;
        suConfirmPassword = new JPasswordField();
        UITheme.styleTextField(suConfirmPassword);
        suConfirmPassword.addActionListener(e -> handleSignup());
        card.add(suConfirmPassword, gbc);

        gbc.gridy = row++;
        signupError = new JLabel(" ");
        signupError.setFont(UITheme.bodyFont(12));
        signupError.setForeground(UITheme.PINK);
        card.add(signupError, gbc);

        gbc.gridy = row++;
        StyledButton signupBtn = new StyledButton("Create Account", UITheme.BLUE);
        signupBtn.addActionListener(e -> handleSignup());
        card.add(signupBtn, gbc);

        gbc.gridy = row++;
        JButton toLogin = linkButton("Already have an account? Log in");
        toLogin.addActionListener(e -> {
            signupError.setText(" ");
            modeLayout.show(modeHost, "login");
        });
        card.add(toLogin, gbc);

        return card;
    }

    private void handleSignup() {
        String username = suUsername.getText().trim();
        String email = suEmail.getText().trim();
        String location = suLocation.getText().trim();
        String password = new String(suPassword.getPassword());
        String confirm = new String(suConfirmPassword.getPassword());

        if (username.isEmpty() || email.isEmpty() || location.isEmpty() || password.isEmpty()) {
            signupError.setText("All fields are required.");
            return;
        }
        if (!password.equals(confirm)) {
            signupError.setText("Passwords do not match.");
            return;
        }

        try {
            int newUserId = userDAO.addUser(new User(username, email, location, password));
            if (newUserId == -1) {
                signupError.setText("Could not create account. Try a different username.");
                return;
            }
            User newUser = userDAO.getUserById(newUserId);
            signupError.setText(" ");
            onLoginSuccess.accept(newUser);
        } catch (SQLException e) {
            signupError.setText("That username or email may already be taken.");
        }
    }

    private GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;
        return gbc;
    }

    private JComponent brandRow() {
        JLabel mark = new JLabel(UITheme.shieldIcon(UITheme.BLUE, 26));
        JLabel text = new JLabel("  Fraud Detection");
        text.setFont(UITheme.titleFont(15));
        text.setForeground(UITheme.INK);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.add(mark);
        row.add(text);
        return row;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.boldFont(12));
        l.setForeground(UITheme.INK);
        return l;
    }

    private JButton linkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.bodyFont(12));
        btn.setForeground(UITheme.BLUE);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMargin(new Insets(0, 0, 0, 0));
        return btn;
    }
}

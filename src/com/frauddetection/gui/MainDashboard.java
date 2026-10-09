package com.frauddetection.gui;

import com.frauddetection.AppContext;
import com.frauddetection.dao.AlertDAO;
import com.frauddetection.dao.TransactionDAO;
import com.frauddetection.dao.UserDAO;
import com.frauddetection.model.User;
import com.frauddetection.service.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

public class MainDashboard extends JFrame {

    private static final Color HOVER = UITheme.SIDEBAR_HOVER;

    private final AppContext ctx;
    private final UserDAO userDAO;
    private final TransactionDAO transactionDAO;
    private final AlertDAO alertDAO;
    private final FraudDetectionService detectionService;

    private DashboardPanel dashboardPanel;
    private AddTransactionPanel addTransactionPanel;
    private UsersPanel usersPanel;
    private AlertsPanel alertsPanel;
    private ConfigPanel configPanel;
    private AlgorithmPanel algorithmPanel;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel();

    private TransactionSimulator simulator;
    private Thread simulatorThread;
    private JButton simToggleButton;

    private final java.util.Map<String, JButton> navButtons = new java.util.LinkedHashMap<>();
    private String currentCard = "dashboard";

    public MainDashboard(AppContext ctx) {
        super("Fraud Detection System — Bank View");
        this.ctx = ctx;
        this.userDAO = ctx.userDAO;
        this.transactionDAO = ctx.transactionDAO;
        this.alertDAO = ctx.alertDAO;
        this.detectionService = ctx.detectionService;

        // Simple Admin authentication gate
        if (!authenticateAdmin()) {
            dispose();
            System.exit(0);
            return;
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 640));

        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildContentArea(), BorderLayout.CENTER);
        updateNavHighlight();
    }

    /** Simple admin login dialog — returns true if authenticated. */
    private boolean authenticateAdmin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(300, 100));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Admin Password (default: admin123):"), gbc);
        gbc.gridy = 1;
        JPasswordField passField = new JPasswordField(20);
        panel.add(passField, gbc);

        int result = JOptionPane.showConfirmDialog(null, panel, "Admin Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return false;

        String entered = new String(passField.getPassword());
        // Simple hardcoded admin password for this college project
        if ("admin123".equals(entered)) {
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Incorrect admin password.", "Access Denied",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private JComponent buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UITheme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        JLabel brand = new JLabel("  Fraud Detection");
        brand.setIcon(UITheme.shieldIcon(UITheme.BLUE, 22));
        brand.setIconTextGap(8);
        brand.setFont(UITheme.titleFont(15));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 16, 4, 16));

        JLabel subBrand = new JLabel("  Bank / Analyst View");
        subBrand.setFont(UITheme.bodyFont(11));
        subBrand.setForeground(new Color(0xA0, 0xAE, 0xC5));
        subBrand.setAlignmentX(Component.LEFT_ALIGNMENT);
        subBrand.setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));

        sidebar.add(brand);
        sidebar.add(subBrand);
        sidebar.add(navButton("Dashboard", "dashboard"));
        sidebar.add(navButton("Add Transaction", "add"));
        sidebar.add(navButton("Users", "users"));
        sidebar.add(navButton("Alerts", "alerts"));
        sidebar.add(navButton("System Config", "config"));
        sidebar.add(navButton("Algorithms", "algorithms"));

        sidebar.add(Box.createVerticalGlue());

        simToggleButton = new JButton("Start Live Simulation");
        simToggleButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        simToggleButton.setMaximumSize(new Dimension(200, 40));
        simToggleButton.setFocusPainted(false);
        simToggleButton.setBackground(UITheme.GREEN);
        simToggleButton.setForeground(Color.WHITE);
        simToggleButton.setFont(UITheme.boldFont(12));
        simToggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        simToggleButton.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        simToggleButton.addActionListener(e -> toggleSimulation());

        JPanel simWrap = new JPanel();
        simWrap.setOpaque(false);
        simWrap.setLayout(new BoxLayout(simWrap, BoxLayout.Y_AXIS));
        simWrap.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        simWrap.add(simToggleButton);
        sidebar.add(simWrap);

        return sidebar;
    }

    private JButton navButton(String text, String cardName) {
        JButton btn = new JButton("  " + text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 44));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(UITheme.bodyFont(14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(UITheme.SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showCard(cardName));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!cardName.equals(currentCard)) {
                    btn.setOpaque(true);
                    btn.setContentAreaFilled(true);
                    btn.setBackground(HOVER);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!cardName.equals(currentCard)) {
                    btn.setOpaque(false);
                    btn.setContentAreaFilled(false);
                    btn.setBackground(UITheme.SIDEBAR);
                }
            }
        });

        navButtons.put(cardName, btn);
        return btn;
    }

    private void updateNavHighlight() {
        for (java.util.Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(currentCard);
            JButton btn = entry.getValue();
            btn.setOpaque(active);
            btn.setContentAreaFilled(active);
            btn.setBackground(active ? UITheme.BLUE : UITheme.SIDEBAR);
        }
    }

    private JComponent buildContentArea() {
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(UITheme.BG);

        dashboardPanel = new DashboardPanel(transactionDAO, alertDAO);
        addTransactionPanel = new AddTransactionPanel(userDAO, transactionDAO, detectionService,
                this::refreshAllScreens);
        usersPanel = new UsersPanel(userDAO, () -> addTransactionPanel.reloadUsers());
        alertsPanel = new AlertsPanel(alertDAO);
        configPanel = new ConfigPanel(ctx);
        algorithmPanel = new AlgorithmPanel(ctx);

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(addTransactionPanel, "add");
        contentPanel.add(usersPanel, "users");
        contentPanel.add(alertsPanel, "alerts");
        contentPanel.add(configPanel, "config");
        contentPanel.add(algorithmPanel, "algorithms");

        return contentPanel;
    }

    private void showCard(String name) {
        currentCard = name;
        cardLayout.show(contentPanel, name);
        updateNavHighlight();
        if ("dashboard".equals(name)) dashboardPanel.refresh();
        if ("alerts".equals(name)) alertsPanel.refresh();
        if ("users".equals(name)) usersPanel.refresh();
        if ("config".equals(name)) configPanel.refresh();
        if ("algorithms".equals(name)) algorithmPanel.refresh();
    }

    private void refreshAllScreens() {
        dashboardPanel.refresh();
        alertsPanel.refresh();
    }

    private void toggleSimulation() {
        if (simulator != null && simulator.isRunning()) {
            simulator.stopSimulation();
            simToggleButton.setText("Start Live Simulation");
            simToggleButton.setBackground(UITheme.GREEN);
            return;
        }

        try {
            List<User> users = userDAO.getAllUsers();
            if (users.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Add at least one user before starting the simulation.",
                        "No Users", JOptionPane.WARNING_MESSAGE);
                return;
            }

            simulator = new TransactionSimulator(transactionDAO, detectionService, users,
                    (txn, result) -> SwingUtilities.invokeLater(this::refreshAllScreens));
            simulatorThread = new Thread(simulator, "TransactionSimulatorThread");
            simulatorThread.setDaemon(true);
            simulatorThread.start();

            simToggleButton.setText("Stop Live Simulation");
            simToggleButton.setBackground(UITheme.PINK);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not start simulation: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

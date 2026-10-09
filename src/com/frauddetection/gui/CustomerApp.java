package com.frauddetection.gui;

import com.frauddetection.AppContext;
import com.frauddetection.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomerApp extends JFrame {

    private static final Color HOVER = UITheme.SIDEBAR_HOVER;

    private final AppContext ctx;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel();

    private static final String CARD_LOGIN = "login";
    private static final String CARD_DASHBOARD = "dashboard";

    public CustomerApp(AppContext ctx) {
        super("Fraud Detection System — Customer Portal");
        this.ctx = ctx;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 760);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 620));

        root.setLayout(cardLayout);
        root.setBackground(UITheme.BG);

        LoginPanel loginPanel = new LoginPanel(ctx.userDAO, this::onLoginSuccess);
        root.add(loginPanel, CARD_LOGIN);

        setContentPane(root);
        cardLayout.show(root, CARD_LOGIN);
    }

    private void onLoginSuccess(User user) {
        JPanel dashboard = buildDashboardFor(user);
        root.add(dashboard, CARD_DASHBOARD);
        cardLayout.show(root, CARD_DASHBOARD);
    }

    private JPanel buildDashboardFor(User user) {
        CardLayout innerLayout = new CardLayout();
        JPanel content = new JPanel(innerLayout);
        content.setBackground(UITheme.BG);

        CustomerHomePanel homePanel = new CustomerHomePanel(ctx.transactionDAO, ctx.alertDAO, user);
        MakePaymentPanel paymentPanel = new MakePaymentPanel(ctx.transactionDAO, ctx.detectionService, user,
                homePanel::refresh);
        MyAlertsPanel alertsPanel = new MyAlertsPanel(ctx.alertDAO, ctx.transactionDAO, user);

        content.add(homePanel, "home");
        content.add(paymentPanel, "pay");
        content.add(alertsPanel, "alerts");

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(buildSidebar(user, content, innerLayout, homePanel, alertsPanel), BorderLayout.WEST);
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    private final java.util.Map<String, JButton> customerNavButtons = new java.util.LinkedHashMap<>();
    private String currentCustomerCard = "home";

    private JComponent buildSidebar(User user, JPanel content, CardLayout innerLayout,
                                     CustomerHomePanel homePanel, MyAlertsPanel alertsPanel) {
        customerNavButtons.clear();
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UITheme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        JLabel brand = new JLabel("  My Account");
        brand.setIcon(UITheme.shieldIcon(UITheme.BLUE, 22));
        brand.setIconTextGap(8);
        brand.setFont(UITheme.titleFont(15));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 16, 4, 16));

        JLabel who = new JLabel("  " + user.getUsername());
        who.setFont(UITheme.bodyFont(12));
        who.setForeground(UITheme.MUTED);
        who.setAlignmentX(Component.LEFT_ALIGNMENT);
        who.setBorder(BorderFactory.createEmptyBorder(0, 16, 24, 16));

        sidebar.add(brand);
        sidebar.add(who);
        sidebar.add(customerNavButton("Home", "home", () -> {
            homePanel.refresh();
            innerLayout.show(content, "home");
        }));
        sidebar.add(customerNavButton("Make a Payment", "pay", () -> innerLayout.show(content, "pay")));
        sidebar.add(customerNavButton("Was This You?", "alerts", () -> {
            alertsPanel.refresh();
            innerLayout.show(content, "alerts");
        }));
        highlightCustomerNav("home");

        sidebar.add(Box.createVerticalGlue());

        JButton logout = new JButton("  Log Out");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(220, 44));
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setFont(UITheme.bodyFont(14));
        logout.setForeground(Color.WHITE);
        logout.setBackground(UITheme.SIDEBAR);
        logout.setBorderPainted(false);
        logout.setFocusPainted(false);
        logout.setContentAreaFilled(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> logOut());
        sidebar.add(logout);

        return sidebar;
    }

    private JButton customerNavButton(String text, String key, Runnable action) {
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
        btn.addActionListener(e -> {
            action.run();
            highlightCustomerNav(key);
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!key.equals(currentCustomerCard)) {
                    btn.setOpaque(true);
                    btn.setContentAreaFilled(true);
                    btn.setBackground(HOVER);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!key.equals(currentCustomerCard)) {
                    btn.setOpaque(false);
                    btn.setContentAreaFilled(false);
                    btn.setBackground(UITheme.SIDEBAR);
                }
            }
        });

        customerNavButtons.put(key, btn);
        return btn;
    }

    private void highlightCustomerNav(String activeKey) {
        currentCustomerCard = activeKey;
        for (java.util.Map.Entry<String, JButton> entry : customerNavButtons.entrySet()) {
            boolean active = entry.getKey().equals(activeKey);
            JButton btn = entry.getValue();
            btn.setOpaque(active);
            btn.setContentAreaFilled(active);
            btn.setBackground(active ? UITheme.BLUE : UITheme.SIDEBAR);
        }
    }

    private void logOut() {
        root.removeAll();
        LoginPanel freshLogin = new LoginPanel(ctx.userDAO, this::onLoginSuccess);
        root.add(freshLogin, CARD_LOGIN);
        root.revalidate();
        root.repaint();
        cardLayout.show(root, CARD_LOGIN);
    }
}

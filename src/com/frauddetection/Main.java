package com.frauddetection;

import com.frauddetection.gui.MainDashboard;
import com.frauddetection.gui.UITheme;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        UITheme.apply();
        AppContext ctx = AppContext.getInstance();
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Admin / Bank Portal", "Customer Portal", "Both (Admin + Customer)", "Exit"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Welcome to the AI-Powered Fraud Detection System!\n\nPlease select the portal you want to launch:",
                    "Fraud Detection System — Select Portal",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    UITheme.shieldIcon(UITheme.BLUE, 36),
                    options,
                    options[0]
            );

            if (choice == 0) {
                MainDashboard dashboard = new MainDashboard(ctx);
                dashboard.setVisible(true);
            } else if (choice == 1) {
                com.frauddetection.gui.CustomerApp app = new com.frauddetection.gui.CustomerApp(ctx);
                app.setVisible(true);
            } else if (choice == 2) {
                MainDashboard dashboard = new MainDashboard(ctx);
                dashboard.setVisible(true);
                com.frauddetection.gui.CustomerApp app = new com.frauddetection.gui.CustomerApp(ctx);
                app.setVisible(true);
            }
        });
    }
}

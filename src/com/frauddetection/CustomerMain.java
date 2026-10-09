package com.frauddetection;

import com.frauddetection.gui.CustomerApp;
import com.frauddetection.gui.UITheme;
import javax.swing.*;

public class CustomerMain {
    public static void main(String[] args) {
        UITheme.apply();
        AppContext ctx = AppContext.getInstance();
        SwingUtilities.invokeLater(() -> {
            CustomerApp app = new CustomerApp(ctx);
            app.setVisible(true);
        });
    }
}

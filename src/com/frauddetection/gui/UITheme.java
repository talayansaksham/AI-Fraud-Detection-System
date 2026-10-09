package com.frauddetection.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class UITheme {

    // --- Cyber Fraud Detection Palette (From Dribbble Design) ---
    public static final Color BLUE         = new Color(0x30, 0x7A, 0xFF); // Primary Electric Blue (#307AFF)
    public static final Color PINK         = new Color(0xFF, 0x1D, 0x75); // Neon Pink / Magenta (#FF1D75)
    public static final Color SOFT_PINK    = new Color(0xFF, 0xBC, 0xD7); // Soft Rose Accent (#FFBCD7)
    public static final Color BG           = new Color(0x02, 0x02, 0x08); // Obsidian Dark Canvas (#020208)
    public static final Color INK          = new Color(0xFF, 0xFF, 0xFF); // Pure White Typography (#FFFFFF)

    // --- Complementary Dark Surfaces & Accents ---
    public static final Color CARD_BG      = new Color(0x0C, 0x12, 0x20); // Frosted Card / Panel Background
    public static final Color SIDEBAR      = new Color(0x05, 0x08, 0x13); // Sidebar Background
    public static final Color SIDEBAR_HOVER= new Color(0x13, 0x1C, 0x32); // Sidebar Item Hover
    public static final Color INPUT_BG     = new Color(0x07, 0x0B, 0x16); // Form Input Background
    public static final Color LINE         = new Color(0x17, 0x23, 0x3D); // Border / Line Divider
    public static final Color MUTED        = new Color(0x8E, 0x9C, 0xAE); // Slate Gray Secondary Text

    public static final Color BLUE_TINT    = new Color(0x11, 0x21, 0x42); // Blue Ambient Tint
    public static final Color PINK_TINT    = new Color(0x38, 0x0F, 0x22); // Pink Ambient Tint
    public static final Color VIOLET       = new Color(0x9D, 0x5C, 0xFF); // Violet Accent
    public static final Color VIOLET_TINT  = new Color(0x28, 0x17, 0x48); // Violet Tint
    public static final Color ORANGE       = new Color(0xFF, 0x9F, 0x43); // Warning Amber
    public static final Color ORANGE_TINT  = new Color(0x3B, 0x23, 0x10); // Amber Tint
    public static final Color GREEN        = new Color(0x00, 0xE5, 0x99); // Cyber Emerald Green
    public static final Color GREEN_TINT   = new Color(0x06, 0x2E, 0x20); // Emerald Tint

    private static final String FONT_FAMILY = pickAvailableFont();

    private static String pickAvailableFont() {
        String[] preferred = {"Planc", "Inter", "Segoe UI", "SF Pro Display", "SF Pro Text", "Ubuntu", "Arial"};
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String want : preferred) {
            for (String have : available) {
                if (have.equalsIgnoreCase(want)) return have;
            }
        }
        return Font.SANS_SERIF;
    }

    public static Font titleFont(float size)   { return new Font(FONT_FAMILY, Font.BOLD, Math.round(size)); }
    public static Font bodyFont(float size)    { return new Font(FONT_FAMILY, Font.PLAIN, Math.round(size)); }
    public static Font boldFont(float size)    { return new Font(FONT_FAMILY, Font.BOLD, Math.round(size)); }

    public static void apply() {
        try {
            MetalLookAndFeel.setCurrentTheme(new CyberDarkTheme());
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception ignored) {
        }

        UIManager.put("Panel.background", BG);
        UIManager.put("control", BG);
        UIManager.put("info", CARD_BG);
        UIManager.put("text", INK);
        UIManager.put("Label.foreground", INK);
        UIManager.put("Table.background", CARD_BG);
        UIManager.put("Table.foreground", INK);
        UIManager.put("Table.font", bodyFont(13));
        UIManager.put("Table.rowHeight", 32);
        UIManager.put("Table.gridColor", new Color(0x13, 0x1C, 0x30));
        UIManager.put("Table.selectionBackground", new Color(0x1A, 0x35, 0x6E));
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.font", boldFont(12));
        UIManager.put("TableHeader.background", new Color(0x06, 0x0A, 0x14));
        UIManager.put("TableHeader.foreground", MUTED);
        UIManager.put("TextField.background", INPUT_BG);
        UIManager.put("TextField.foreground", INK);
        UIManager.put("TextField.caretForeground", BLUE);
        UIManager.put("PasswordField.background", INPUT_BG);
        UIManager.put("PasswordField.foreground", INK);
        UIManager.put("PasswordField.caretForeground", BLUE);
        UIManager.put("ComboBox.background", INPUT_BG);
        UIManager.put("ComboBox.foreground", INK);
        UIManager.put("ComboBox.selectionBackground", BLUE);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ScrollPane.background", CARD_BG);
        UIManager.put("Viewport.background", CARD_BG);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("TextField.font", bodyFont(13));
        UIManager.put("ComboBox.font", bodyFont(13));
        UIManager.put("Button.font", bodyFont(13));
        UIManager.put("Label.font", bodyFont(13));
    }

    private static class CyberDarkTheme extends DefaultMetalTheme {
        @Override public ColorUIResource getPrimary1() { return new ColorUIResource(BLUE); }
        @Override public ColorUIResource getPrimary2() { return new ColorUIResource(BLUE_TINT); }
        @Override public ColorUIResource getPrimary3() { return new ColorUIResource(BLUE_TINT); }
        @Override public ColorUIResource getSecondary1() { return new ColorUIResource(LINE); }
        @Override public ColorUIResource getSecondary2() { return new ColorUIResource(CARD_BG); }
        @Override public ColorUIResource getSecondary3() { return new ColorUIResource(BG); }
        @Override public ColorUIResource getControlTextColor() { return new ColorUIResource(INK); }
        @Override public ColorUIResource getSystemTextColor() { return new ColorUIResource(INK); }
        @Override public ColorUIResource getUserTextColor() { return new ColorUIResource(INK); }
        @Override public ColorUIResource getBlack() { return new ColorUIResource(INK); }
        @Override public ColorUIResource getWhite() { return new ColorUIResource(CARD_BG); }
        @Override public FontUIResource getControlTextFont() { return new FontUIResource(bodyFont(13)); }
        @Override public FontUIResource getSystemTextFont() { return new FontUIResource(bodyFont(13)); }
        @Override public FontUIResource getUserTextFont() { return new FontUIResource(bodyFont(13)); }
        @Override public FontUIResource getMenuTextFont() { return new FontUIResource(bodyFont(13)); }
        @Override public FontUIResource getWindowTitleFont() { return new FontUIResource(boldFont(13)); }
        @Override public FontUIResource getSubTextFont() { return new FontUIResource(bodyFont(11)); }
    }

    public static JTable zebraTable(javax.swing.table.TableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : new Color(0x08, 0x0D, 0x18));
                    c.setForeground(INK);
                } else {
                    c.setBackground(new Color(0x1B, 0x33, 0x66));
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        };
        table.setBackground(CARD_BG);
        table.setForeground(INK);
        table.setGridColor(new Color(0x13, 0x1C, 0x30));
        table.setShowGrid(true);
        table.setShowVerticalLines(false);
        return table;
    }

    public static void styleTableHeader(JTableHeader header) {
        header.setBackground(new Color(0x05, 0x09, 0x14));
        header.setForeground(MUTED);
        header.setFont(boldFont(12));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE));
    }

    public static void styleTextField(JTextField field) {
        field.setFont(bodyFont(13));
        field.setBackground(INPUT_BG);
        field.setForeground(INK);
        field.setCaretColor(BLUE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(bodyFont(13));
        combo.setBackground(INPUT_BG);
        combo.setForeground(INK);
        combo.setBorder(BorderFactory.createLineBorder(LINE, 1, true));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBackground(isSelected ? BLUE : INPUT_BG);
                c.setForeground(isSelected ? Color.WHITE : INK);
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return c;
            }
        });
    }

    public static JLabel sectionKicker(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(boldFont(11));
        l.setForeground(BLUE);
        return l;
    }

    public static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(titleFont(22));
        l.setForeground(INK);
        return l;
    }

    public static Icon shieldIcon(Color color, int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);

                Polygon shield = new Polygon();
                shield.addPoint(size / 2, 0);
                shield.addPoint(size, size / 5);
                shield.addPoint(size, size / 2);
                shield.addPoint(size / 2, size);
                shield.addPoint(0, size / 2);
                shield.addPoint(0, size / 5);
                g2.setColor(color);
                g2.fillPolygon(shield);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(Math.max(1.5f, size / 9f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine((int) (size * 0.28), (int) (size * 0.50), (int) (size * 0.45), (int) (size * 0.65));
                g2.drawLine((int) (size * 0.45), (int) (size * 0.65), (int) (size * 0.75), (int) (size * 0.30));

                g2.dispose();
            }
            @Override public int getIconWidth() { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    public static JPanel padded(JComponent inner, int top, int left, int bottom, int right) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(top, left, bottom, right));
        wrap.add(inner, BorderLayout.CENTER);
        return wrap;
    }

    private UITheme() {}
}

package com.smartcity.navigator.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * Enterprise Design System &amp; Style Tokens.
 * Defines standard colors, typography, sizing, scaling, and custom styling
 * boundaries to achieve a premium, modern, cohesive dashboard user experience.
 * <p>
 * This revision adds rounded geometry, hover/pressed/focus feedback, and a
 * soft elevation treatment for cards, while keeping every public method
 * name and signature identical so calling code needs no changes.
 *
 * @author Smart City Route Navigator Team
 */
public final class UITheme {

    // --- Core Premium Colors ---
    public static final Color WINDOW_BACKGROUND = new Color(248, 250, 252);     // Slate 50
    public static final Color PANEL_BACKGROUND = new Color(255, 255, 255);      // Pure White
    public static final Color SUBPANEL_BACKGROUND = new Color(241, 245, 249);   // Slate 100
    public static final Color BORDER_COLOR = new Color(226, 232, 240);          // Slate 200

    // --- Enterprise Palette Tokens ---
    public static final Color ACCENT_PRIMARY = new Color(37, 99, 235);          // Royal Blue (Brand Core)
    public static final Color ACCENT_PRIMARY_HOVER = new Color(29, 78, 216);    // Darker Blue
    public static final Color ACCENT_PRIMARY_PRESSED = new Color(23, 64, 179);  // Deepest Blue
    public static final Color ACCENT_PRIMARY_SOFT = new Color(219, 234, 254);   // Blue 100 (chips/badges)
    public static final Color SIDEBAR_BACKGROUND = new Color(15, 23, 42);       // Slate 900
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);            // Slate 800
    public static final Color SIDEBAR_SELECTION = new Color(51, 65, 85);        // Slate 700
    public static final Color SIDEBAR_TEXT_ACTIVE = Color.WHITE;
    public static final Color SIDEBAR_TEXT_MUTED = new Color(148, 163, 184);    // Slate 400

    // --- Text Hierarchy & Contrast Colors ---
    public static final Color HEADING_COLOR = new Color(15, 23, 42);            // Slate 900
    public static final Color LABEL_COLOR = new Color(71, 85, 105);             // Slate 600
    public static final Color SECONDARY_TEXT = new Color(100, 116, 139);        // Slate 500
    public static final Color MENU_BAR_BACKGROUND = new Color(248, 250, 252);   // Slate 50

    // --- Button Colors ---
    public static final Color PRIMARY_BUTTON = ACCENT_PRIMARY;
    public static final Color SECONDARY_BUTTON = new Color(100, 116, 139);      // Slate 500
    public static final Color BUTTON_TEXT = Color.WHITE;

    // --- System Status & Feedback Palette ---
    public static final Color INFO_COLOR = new Color(14, 165, 233);             // Light Blue
    public static final Color SUCCESS_COLOR = new Color(16, 185, 129);          // Emerald Green
    public static final Color WARNING_COLOR = new Color(245, 158, 11);          // Amber Orange
    public static final Color ERROR_COLOR = new Color(239, 68, 68);             // Crimson Red

    // --- Typographical System (Segoe UI Style) ---
    public static final Font APPLICATION_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    // --- Geometry Tokens ---
    private static final int RADIUS_LARGE = 14;
    private static final int RADIUS_MEDIUM = 10;
    private static final int RADIUS_SMALL = 8;

    // Client property keys used to avoid stacking duplicate listeners when a
    // style method is invoked more than once on the same component.
    private static final String HOVER_LISTENER_KEY = "uitheme.hoverListener";
    private static final String FOCUS_LISTENER_KEY = "uitheme.focusListener";

    private UITheme() {
        // Utility class: not instantiable.
    }

    // ------------------------------------------------------------------
    // Structural helpers
    // ------------------------------------------------------------------

    public static Border createSectionBorder(String title) {
        return BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, PANEL_BACKGROUND, 1, RADIUS_LARGE, 0),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(5, 5, 5, 5),
                        title,
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        SECTION_FONT,
                        HEADING_COLOR
                )
        );
    }

    public static void stylePanel(JComponent component) {
        component.setBackground(PANEL_BACKGROUND);
        component.setOpaque(true);
    }

    public static void styleHeading(JLabel label) {
        label.setFont(SECTION_FONT);
        label.setForeground(HEADING_COLOR);
    }

    public static void styleLabel(JLabel label) {
        label.setFont(LABEL_FONT);
        label.setForeground(LABEL_COLOR);
    }

    public static void styleStatusLabel(JLabel label) {
        label.setFont(BODY_FONT);
        label.setForeground(HEADING_COLOR);
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setForeground(HEADING_COLOR);
        comboBox.setBackground(SUBPANEL_BACKGROUND);
        comboBox.setOpaque(true);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, null, 1, RADIUS_MEDIUM, 0),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        comboBox.setPreferredSize(new Dimension(0, 40));
        comboBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(HEADING_COLOR);
        field.setBackground(PANEL_BACKGROUND);
        field.setOpaque(true);

        RoundedBorder idleBorder = new RoundedBorder(BORDER_COLOR, null, 1, RADIUS_MEDIUM, 0);
        RoundedBorder focusBorder = new RoundedBorder(ACCENT_PRIMARY, null, 2, RADIUS_MEDIUM, 0);
        field.setBorder(BorderFactory.createCompoundBorder(idleBorder,
                BorderFactory.createEmptyBorder(9, 11, 9, 11)));

        removeExistingListener(field, FOCUS_LISTENER_KEY);
        FocusAdapter focusAdapter = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(focusBorder,
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(idleBorder,
                        BorderFactory.createEmptyBorder(9, 11, 9, 11)));
            }
        };
        field.putClientProperty(FOCUS_LISTENER_KEY, focusAdapter);
        field.addFocusListener(focusAdapter);
    }

    public static void styleCheckBox(JCheckBox checkBox) {
        checkBox.setFont(BODY_FONT);
        checkBox.setForeground(HEADING_COLOR);
        checkBox.setBackground(WINDOW_BACKGROUND);
        checkBox.setOpaque(true);
        checkBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void styleRadioButton(JRadioButton radioButton) {
        radioButton.setFont(BODY_FONT);
        radioButton.setForeground(HEADING_COLOR);
        radioButton.setBackground(WINDOW_BACKGROUND);
        radioButton.setOpaque(true);
        radioButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void styleMenuBar(JMenuBar menuBar) {
        menuBar.setOpaque(true);
        menuBar.setBackground(MENU_BAR_BACKGROUND);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        menuBar.setFont(BODY_FONT);
    }

    public static void styleMenu(JMenu menu) {
        menu.setFont(SECTION_FONT);
        menu.setForeground(HEADING_COLOR);
    }

    public static void styleMenuItem(JMenuItem menuItem) {
        menuItem.setFont(BODY_FONT);
        menuItem.setForeground(LABEL_COLOR);
    }

    public static void styleStatusPanel(JPanel panel) {
        panel.setBackground(WINDOW_BACKGROUND);
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
    }

    public static void styleSectionPanel(JComponent component) {
        component.setBorder(createSectionBorder(""));
        component.setBackground(PANEL_BACKGROUND);
        component.setOpaque(true);
    }

    /** Applies the card treatment: rounded corners, soft border, subtle elevation shadow. */
    public static void styleCardPanel(JComponent component) {
        component.setBackground(PANEL_BACKGROUND);
        component.setOpaque(false);
        component.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, PANEL_BACKGROUND, 1, RADIUS_LARGE, 3),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
    }

    public static void styleCardHeader(JLabel label) {
        label.setFont(SECTION_FONT);
        label.setForeground(HEADING_COLOR);
    }

    public static void styleFieldLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(LABEL_COLOR);
    }

    // ------------------------------------------------------------------
    // Buttons
    // ------------------------------------------------------------------

    public static void stylePrimaryActionButton(JButton button) {
        applyRoundedButton(button, PRIMARY_BUTTON, ACCENT_PRIMARY_HOVER, ACCENT_PRIMARY_PRESSED,
                BUTTON_TEXT, new Font("Segoe UI", Font.BOLD, 13), RADIUS_MEDIUM,
                BorderFactory.createEmptyBorder(11, 18, 11, 18));
    }

    public static void styleSecondaryActionButton(JButton button) {
        Color base = SECONDARY_BUTTON;
        applyRoundedButton(button, base, base.darker(), base.darker().darker(),
                BUTTON_TEXT, new Font("Segoe UI", Font.BOLD, 13), RADIUS_MEDIUM,
                BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }

    public static void styleButton(JButton button) {
        stylePrimaryActionButton(button);
    }

    public static void stylePlaceholderLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(SECONDARY_TEXT);
    }

    public static void stylePrimaryToolBarButton(JButton button) {
        applyRoundedButton(button, PRIMARY_BUTTON, ACCENT_PRIMARY_HOVER, ACCENT_PRIMARY_PRESSED,
                BUTTON_TEXT, BODY_FONT, RADIUS_MEDIUM,
                BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }

    public static void styleSecondaryButton(JButton button) {
        Color base = SECONDARY_BUTTON;
        applyRoundedButton(button, base, base.darker(), base.darker().darker(),
                BUTTON_TEXT, BODY_FONT, RADIUS_MEDIUM,
                BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }

    public static void styleSecondaryToolBarButton(JButton button) {
        applyRoundedOutlineButton(button, SUBPANEL_BACKGROUND, BORDER_COLOR, ACCENT_PRIMARY,
                HEADING_COLOR, BODY_FONT, RADIUS_MEDIUM,
                BorderFactory.createEmptyBorder(9, 16, 9, 16));
    }

    /** Applies the dark-sidebar navigation treatment, selected state, and hover feedback. */
    public static void styleSidebarNavigationButton(JToggleButton button, boolean selected) {
        button.setFont(BODY_FONT);
        button.setForeground(selected ? SIDEBAR_TEXT_ACTIVE : SIDEBAR_TEXT_MUTED);
        button.setBackground(selected ? SIDEBAR_SELECTION : SIDEBAR_BACKGROUND);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(12);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        removeExistingListener(button, HOVER_LISTENER_KEY);
        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.isSelected()) {
                    paintSidebarBackground(button, SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!button.isSelected()) {
                    paintSidebarBackground(button, SIDEBAR_BACKGROUND);
                }
            }
        };
        button.putClientProperty(HOVER_LISTENER_KEY, hoverAdapter);
        button.addMouseListener(hoverAdapter);
    }

    // ------------------------------------------------------------------
    // Internal button/border plumbing
    // ------------------------------------------------------------------

    private static void paintSidebarBackground(JToggleButton button, Color color) {
        button.setBackground(color);
        button.repaint();
    }

    private static void applyRoundedButton(AbstractButton button, Color base, Color hover, Color pressed,
            Color textColor, Font font, int radius, Border padding) {
        button.setFont(font);
        button.setForeground(textColor);
        button.setBackground(base);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(padding);

        removeExistingListener(button, HOVER_LISTENER_KEY);
        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hover);
                    button.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(base);
                button.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(pressed);
                    button.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Color restore = button.contains(e.getPoint()) ? hover : base;
                button.setBackground(button.isEnabled() ? restore : base);
                button.repaint();
            }
        };
        button.putClientProperty(HOVER_LISTENER_KEY, hoverAdapter);
        button.addMouseListener(hoverAdapter);
    }

    private static void applyRoundedOutlineButton(AbstractButton button, Color base, Color outline,
            Color hoverOutline, Color textColor, Font font, int radius, Border padding) {
        button.setFont(font);
        button.setForeground(textColor);
        button.setBackground(base);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Border idleBorder = BorderFactory.createCompoundBorder(
                new RoundedBorder(outline, null, 1, radius, 0), padding);
        Border hoverBorder = BorderFactory.createCompoundBorder(
                new RoundedBorder(hoverOutline, null, 1, radius, 0), padding);
        button.setBorder(idleBorder);

        removeExistingListener(button, HOVER_LISTENER_KEY);
        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBorder(hoverBorder);
                button.setBackground(ACCENT_PRIMARY_SOFT);
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBorder(idleBorder);
                button.setBackground(base);
                button.repaint();
            }
        };
        button.putClientProperty(HOVER_LISTENER_KEY, hoverAdapter);
        button.addMouseListener(hoverAdapter);
    }

    private static void removeExistingListener(JComponent component, String key) {
        Object existing = component.getClientProperty(key);
        if (existing instanceof MouseAdapter mouseAdapter) {
            component.removeMouseListener(mouseAdapter);
        } else if (existing instanceof FocusAdapter focusAdapter) {
            component.removeFocusListener(focusAdapter);
        }
    }

    /**
     * A lightweight, mutable, rounded-rectangle border that also paints its
     * own background fill. This lets buttons and panels appear rounded
     * without subclassing Swing components: callers set
     * {@code setContentAreaFilled(false)} / {@code setOpaque(false)} and let
     * this border draw the shape, optionally with a soft drop shadow.
     */
    private static final class RoundedBorder extends AbstractBorder {
        private Color lineColor;
        private Color fillColor;
        private final int lineThickness;
        private final int radius;
        private final int shadowSize;

        private RoundedBorder(Color lineColor, Color fillColor, int lineThickness, int radius, int shadowSize) {
            this.lineColor = lineColor;
            this.fillColor = fillColor;
            this.lineThickness = lineThickness;
            this.radius = radius;
            this.shadowSize = shadowSize;
        }

        private void setFillColor(Color color) {
            this.fillColor = color;
        }

        private void setLineColor(Color color) {
            this.lineColor = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int shapeWidth = width - shadowSize;
                int shapeHeight = height - shadowSize;

                if (shadowSize > 0) {
                    g2.setColor(new Color(15, 23, 42, 18));
                    g2.fillRoundRect(x + shadowSize, y + shadowSize, shapeWidth, shapeHeight, radius, radius);
                }

                if (fillColor != null) {
                    g2.setColor(fillColor);
                    g2.fillRoundRect(x, y, shapeWidth, shapeHeight, radius, radius);
                }

                if (lineColor != null && lineThickness > 0) {
                    g2.setColor(lineColor);
                    g2.setStroke(new java.awt.BasicStroke(lineThickness));
                    int inset = lineThickness / 2;
                    g2.drawRoundRect(x + inset, y + inset, shapeWidth - lineThickness, shapeHeight - lineThickness,
                            radius, radius);
                }
            } finally {
                g2.dispose();
            }
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int pad = Math.max(lineThickness, 1);
            return new Insets(pad, pad, pad + shadowSize, pad + shadowSize);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            Insets computed = getBorderInsets(c);
            insets.set(computed.top, computed.left, computed.bottom, computed.right);
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}

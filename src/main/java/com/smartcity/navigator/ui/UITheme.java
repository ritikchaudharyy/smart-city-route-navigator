package com.smartcity.navigator.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;

public final class UITheme {

    public static final Color WINDOW_BACKGROUND = new Color(246, 248, 250);
    public static final Color PANEL_BACKGROUND = new Color(255, 255, 255);
    public static final Color SUBPANEL_BACKGROUND = new Color(245, 247, 250);
    public static final Color HEADING_COLOR = new Color(34, 41, 57);
    public static final Color LABEL_COLOR = new Color(92, 102, 118);
    public static final Color SECONDARY_TEXT = new Color(118, 129, 145);
    public static final Color BORDER_COLOR = new Color(215, 221, 229);
    public static final Color MENU_BAR_BACKGROUND = new Color(246, 248, 250);
    public static final Color PRIMARY_BUTTON = new Color(15, 95, 185);
    public static final Color SECONDARY_BUTTON = new Color(120, 130, 145);
    public static final Color BUTTON_TEXT = Color.WHITE;
    public static final Color INFO_COLOR = new Color(26, 115, 232);
    public static final Color SUCCESS_COLOR = new Color(33, 150, 83);
    public static final Color WARNING_COLOR = new Color(235, 124, 0);
    public static final Color ERROR_COLOR = new Color(220, 70, 70);
    public static final Font APPLICATION_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private UITheme() {
        // Utility class.
    }

    public static Border createSectionBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createTitledBorder(title)
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

    public static void styleComboBox(javax.swing.JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setForeground(HEADING_COLOR);
        comboBox.setBackground(SUBPANEL_BACKGROUND);
        comboBox.setOpaque(true);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        comboBox.setPreferredSize(new java.awt.Dimension(0, 40));
    }

    public static void styleTextField(javax.swing.JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(HEADING_COLOR);
        field.setBackground(PANEL_BACKGROUND);
        field.setOpaque(true);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
    }

    public static void styleCheckBox(javax.swing.JCheckBox checkBox) {
        checkBox.setFont(BODY_FONT);
        checkBox.setForeground(HEADING_COLOR);
        checkBox.setBackground(WINDOW_BACKGROUND);
        checkBox.setOpaque(true);
    }

    public static void styleRadioButton(javax.swing.JRadioButton radioButton) {
        radioButton.setFont(BODY_FONT);
        radioButton.setForeground(HEADING_COLOR);
        radioButton.setBackground(WINDOW_BACKGROUND);
        radioButton.setOpaque(true);
    }

    public static void styleMenuBar(javax.swing.JMenuBar menuBar) {
        menuBar.setOpaque(true);
        menuBar.setBackground(MENU_BAR_BACKGROUND);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        menuBar.setFont(BODY_FONT);
    }

    public static void styleMenu(javax.swing.JMenu menu) {
        menu.setFont(SECTION_FONT);
        menu.setForeground(HEADING_COLOR);
    }

    public static void styleMenuItem(javax.swing.JMenuItem menuItem) {
        menuItem.setFont(BODY_FONT);
        menuItem.setForeground(LABEL_COLOR);
    }

    public static void styleStatusPanel(javax.swing.JPanel panel) {
        panel.setBackground(WINDOW_BACKGROUND);
        panel.setOpaque(true);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
    }

    public static void styleSectionPanel(JComponent component) {
        component.setBorder(createSectionBorder(""));
        component.setBackground(PANEL_BACKGROUND);
        component.setOpaque(true);
    }

    public static void styleCardPanel(JComponent component) {
        component.setBackground(PANEL_BACKGROUND);
        component.setOpaque(true);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
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

    public static void stylePrimaryActionButton(JButton button) {
        button.setBackground(PRIMARY_BUTTON);
        button.setForeground(BUTTON_TEXT);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
    }

    public static void styleSecondaryActionButton(JButton button) {
        button.setBackground(SECONDARY_BUTTON);
        button.setForeground(BUTTON_TEXT);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
    }

    public static void stylePlaceholderLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(SECONDARY_TEXT);
    }

    public static void stylePrimaryToolBarButton(JButton button) {
        button.setBackground(PRIMARY_BUTTON);
        button.setForeground(BUTTON_TEXT);
        button.setFocusPainted(false);
        button.setFont(BODY_FONT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    public static void styleSecondaryButton(JButton button) {
        button.setBackground(SECONDARY_BUTTON);
        button.setForeground(BUTTON_TEXT);
        button.setFocusPainted(false);
        button.setFont(BODY_FONT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    public static void styleSecondaryToolBarButton(JButton button) {
        button.setBackground(new Color(238, 243, 248));
        button.setForeground(HEADING_COLOR);
        button.setFocusPainted(false);
        button.setFont(BODY_FONT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }
}

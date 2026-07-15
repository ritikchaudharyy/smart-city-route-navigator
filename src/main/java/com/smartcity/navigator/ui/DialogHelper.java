package com.smartcity.navigator.ui;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Small UI helper for shared form-building logic used by graph-editing dialogs.
 */
public final class DialogHelper {

    private DialogHelper() {
    }

    public static JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(UITheme.SUBPANEL_BACKGROUND);
        return panel;
    }

    public static JTextField addLabeledField(JPanel panel, String label) {
        JLabel titleLabel = new JLabel(label);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.styleLabel(titleLabel);
        panel.add(titleLabel);

        JTextField field = new JTextField();
        field.setColumns(18);
        UITheme.styleTextField(field);
        panel.add(field);
        return field;
    }
}

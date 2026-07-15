package com.smartcity.navigator.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.smartcity.navigator.utils.AppSettings;

/**
 * Application settings dialog: dark mode and preferred distance unit.
 * Changes are only committed to {@link AppSettings} when the user clicks
 * OK; Cancel discards them. Callers check {@link #isConfirmed()} after
 * the (modal) dialog closes to decide whether to refresh dependent views.
 *
 * @author Smart City Route Navigator Team
 */
public class SettingsDialog extends JDialog {

    private boolean confirmed = false;
    private final JCheckBox darkModeCheck;
    private final JRadioButton kilometersRadio;
    private final JRadioButton milesRadio;

    public SettingsDialog(Frame owner) {
        super(owner, "Settings", true);
        AppSettings settings = AppSettings.getInstance();

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UITheme.WINDOW_BACKGROUND);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        form.setBackground(UITheme.WINDOW_BACKGROUND);

        JLabel titleLabel = new JLabel("Preferences");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.styleHeading(titleLabel);

        darkModeCheck = new JCheckBox("Enable dark mode", settings.isDarkMode());
        darkModeCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.styleCheckBox(darkModeCheck);

        JLabel unitLabel = new JLabel("Distance unit:");
        unitLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.styleLabel(unitLabel);

        kilometersRadio = new JRadioButton("Kilometers", settings.isMetricUnits());
        milesRadio = new JRadioButton("Miles", !settings.isMetricUnits());
        kilometersRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        milesRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.styleRadioButton(kilometersRadio);
        UITheme.styleRadioButton(milesRadio);

        ButtonGroup unitGroup = new ButtonGroup();
        unitGroup.add(kilometersRadio);
        unitGroup.add(milesRadio);

        form.add(darkModeCheck);
        form.add(Box.createVerticalStrut(15));
        form.add(unitLabel);
        form.add(kilometersRadio);
        form.add(milesRadio);

        JButton okButton = new JButton("OK");
        UITheme.styleButton(okButton);
        okButton.addActionListener(e -> {
            settings.setDarkMode(darkModeCheck.isSelected());
            settings.setMetricUnits(kilometersRadio.isSelected());
            confirmed = true;
            dispose();
        });

        JButton cancelButton = new JButton("Cancel");
        UITheme.styleSecondaryButton(cancelButton);
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(UITheme.WINDOW_BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(320, 260);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    /**
     * @return {@code true} if the user clicked OK (settings were applied),
     *         {@code false} if they cancelled or closed the dialog
     */
    public boolean isConfirmed() {
        return confirmed;
    }
}

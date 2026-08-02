package com.smartcity.navigator.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyEvent;

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
import javax.swing.KeyStroke;

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

    private static final Font SECTION_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 11);
    private static final Font DIALOG_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);

    private boolean confirmed = false;
    private final JCheckBox darkModeCheck;
    private final JRadioButton kilometersRadio;
    private final JRadioButton milesRadio;

    public SettingsDialog(Frame owner) {
        super(owner, "Settings", true);
        AppSettings settings = AppSettings.getInstance();

        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.WINDOW_BACKGROUND);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(22, 22, 10, 22));
        form.setBackground(UITheme.WINDOW_BACKGROUND);

        JLabel titleLabel = new JLabel("Preferences");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setFont(DIALOG_TITLE_FONT);
        titleLabel.setForeground(UITheme.HEADING_COLOR);

        JLabel titleSubtext = new JLabel("Customize the map canvas and how route distances are reported.");
        titleSubtext.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleSubtext.setFont(UITheme.LABEL_FONT);
        titleSubtext.setForeground(UITheme.SECONDARY_TEXT);
        titleSubtext.setBorder(BorderFactory.createEmptyBorder(2, 0, 18, 0));

        darkModeCheck = new JCheckBox("Use a dark map canvas", settings.isDarkMode());
        UITheme.styleCheckBox(darkModeCheck);
        darkModeCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        darkModeCheck.setBackground(Color.WHITE);

        JLabel appearanceHint = new JLabel("Useful for low-light use; the dashboard keeps its high-contrast layout.");
        styleHint(appearanceHint);

        JPanel appearanceCard = buildSettingsCard(IconFactory.IconType.SETTINGS, "APPEARANCE",
                darkModeCheck, Box.createVerticalStrut(6), appearanceHint);

        kilometersRadio = new JRadioButton("Kilometers", settings.isMetricUnits());
        milesRadio = new JRadioButton("Miles", !settings.isMetricUnits());
        UITheme.styleRadioButton(kilometersRadio);
        UITheme.styleRadioButton(milesRadio);
        kilometersRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        milesRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        kilometersRadio.setBackground(Color.WHITE);
        milesRadio.setBackground(Color.WHITE);

        ButtonGroup unitGroup = new ButtonGroup();
        unitGroup.add(kilometersRadio);
        unitGroup.add(milesRadio);

        JLabel unitHint = new JLabel("Applies to distance and route summaries across the app.");
        styleHint(unitHint);

        JPanel unitsCard = buildSettingsCard(IconFactory.IconType.MAP, "DISTANCE UNIT",
                kilometersRadio, milesRadio, Box.createVerticalStrut(6), unitHint);

        boolean aiConfigured = isGeminiApiKeyConfigured();
        JLabel aiStatus = new JLabel(aiConfigured
                ? "Configured for this application session"
                : "Not configured");
        aiStatus.setFont(UITheme.BODY_FONT);
        aiStatus.setForeground(aiConfigured ? UITheme.SUCCESS_COLOR : UITheme.WARNING_COLOR);
        aiStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel aiHint = new JLabel(aiConfigured
                ? "The API key is detected securely and is never displayed or stored by the app."
                : "Set GEMINI_API_KEY, then restart the app to enable natural-language route requests.");
        styleHint(aiHint);

        JPanel aiCard = buildSettingsCard(IconFactory.IconType.AI_SPARKLE, "AI ROUTE ASSISTANT",
                aiStatus, Box.createVerticalStrut(6), aiHint);

        form.add(titleLabel);
        form.add(titleSubtext);
        form.add(appearanceCard);
        form.add(Box.createVerticalStrut(14));
        form.add(unitsCard);
        form.add(Box.createVerticalStrut(14));
        form.add(aiCard);

        JButton okButton = new JButton("Save changes");
        UITheme.stylePrimaryActionButton(okButton);
        okButton.addActionListener(e -> {
            settings.setDarkMode(darkModeCheck.isSelected());
            settings.setMetricUnits(kilometersRadio.isSelected());
            confirmed = true;
            dispose();
        });

        JButton cancelButton = new JButton("Cancel");
        UITheme.styleSecondaryToolBarButton(cancelButton);
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBackground(UITheme.WINDOW_BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)));
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(430, 510);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private JPanel buildSettingsCard(IconFactory.IconType iconType, String sectionLabel, Component... rows) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.styleCardPanel(card);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(new JLabel(IconFactory.getIcon(iconType, 14, UITheme.ACCENT_PRIMARY)));

        JLabel sectionTitle = new JLabel(sectionLabel);
        sectionTitle.setFont(SECTION_TITLE_FONT);
        sectionTitle.setForeground(UITheme.ACCENT_PRIMARY);
        header.add(sectionTitle);

        card.add(header);
        card.add(Box.createVerticalStrut(10));
        for (Component row : rows) {
            if (row instanceof javax.swing.JComponent swingComponent) {
                swingComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            card.add(row);
        }
        return card;
    }

    private void styleHint(JLabel label) {
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(UITheme.LABEL_FONT);
        label.setForeground(UITheme.SECONDARY_TEXT);
    }

    private boolean isGeminiApiKeyConfigured() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * @return {@code true} if the user clicked OK (settings were applied),
     *         {@code false} if they cancelled or closed the dialog
     */
    public boolean isConfirmed() {
        return confirmed;
    }
}

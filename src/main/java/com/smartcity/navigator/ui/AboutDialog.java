package com.smartcity.navigator.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.smartcity.navigator.utils.Constants;

/**
 * "Help &gt; About" dialog: shows the application name, version, and a
 * short description. Purely informational — a single OK button closes it.
 *
 * @author Smart City Route Navigator Team
 */
public class AboutDialog extends JDialog {

    public AboutDialog(Frame owner) {
        super(owner, "About " + Constants.APP_TITLE, true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.WINDOW_BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.WINDOW_BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 12, 24));

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(16));
        content.add(buildDescriptionCard());
        content.add(Box.createVerticalStrut(12));
        content.add(buildEngineCard());

        JButton okButton = new JButton("Close");
        UITheme.stylePrimaryActionButton(okButton);
        okButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBackground(UITheme.WINDOW_BACKGROUND);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)));
        buttonPanel.add(okButton);

        add(content, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(420, 400);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel badge = new JLabel(IconFactory.getIcon(IconFactory.IconType.MAP, 28, Color.WHITE));
        badge.setOpaque(true);
        badge.setBackground(UITheme.ACCENT_PRIMARY);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setPreferredSize(new java.awt.Dimension(52, 52));
        badge.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_PRIMARY, 6, true));

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setOpaque(false);

        JLabel titleLabel = new JLabel(Constants.APP_TITLE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(UITheme.HEADING_COLOR);

        JLabel versionChip = new JLabel("Version " + Constants.APP_VERSION);
        versionChip.setAlignmentX(Component.LEFT_ALIGNMENT);
        versionChip.setFont(new Font("Segoe UI", Font.BOLD, 11));
        versionChip.setForeground(UITheme.SUCCESS_COLOR);
        versionChip.setOpaque(true);
        versionChip.setBackground(new Color(236, 253, 245));
        versionChip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(167, 243, 208), 1, true),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));

        titleGroup.add(titleLabel);
        titleGroup.add(Box.createVerticalStrut(6));
        titleGroup.add(versionChip);

        header.add(badge);
        header.add(titleGroup);
        return header;
    }

    private JPanel buildDescriptionCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.styleCardPanel(card);
        card.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel description = new JLabel("<html>A desktop application that models a city road network as a "
                + "weighted graph and computes the shortest route between locations "
                + "using Dijkstra's Algorithm with a hand-built binary min-heap "
                + "priority queue.</html>");
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        description.setFont(UITheme.BODY_FONT);
        description.setForeground(UITheme.LABEL_COLOR);

        card.add(description);
        return card;
    }

    private JPanel buildEngineCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.styleCardPanel(card);
        card.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel sectionTitle = new JLabel("BUILT WITH");
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sectionTitle.setForeground(UITheme.ACCENT_PRIMARY);

        card.add(sectionTitle);
        card.add(Box.createVerticalStrut(8));
        card.add(buildEngineRow(IconFactory.IconType.SETTINGS, "Java 17 runtime"));
        card.add(Box.createVerticalStrut(4));
        card.add(buildEngineRow(IconFactory.IconType.MAP, "Swing desktop UI"));
        card.add(Box.createVerticalStrut(4));
        card.add(buildEngineRow(IconFactory.IconType.SEARCH_ROUTE, "Dijkstra pathfinding engine"));
        return card;
    }

    private JPanel buildEngineRow(IconFactory.IconType iconType, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel(IconFactory.getIcon(iconType, 14, UITheme.SECONDARY_TEXT));
        JLabel label = new JLabel(text);
        label.setFont(UITheme.LABEL_FONT);
        label.setForeground(UITheme.LABEL_COLOR);

        row.add(icon);
        row.add(label);
        return row;
    }
}
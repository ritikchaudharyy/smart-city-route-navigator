package com.smartcity.navigator.ui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel(Constants.APP_TITLE, SwingConstants.CENTER);
        titleLabel.setFont(UITheme.HEADER_FONT);
        titleLabel.setForeground(UITheme.HEADING_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(22, 20, 10, 20));

        JTextArea infoArea = new JTextArea(
                "Version " + Constants.APP_VERSION + "\n\n"
                        + "A desktop application that models a city road network as a\n"
                        + "weighted graph and computes the shortest route between\n"
                        + "locations using Dijkstra's Algorithm with a hand-built\n"
                        + "binary min-heap priority queue.\n\n"
                        + "Built with Java 17 and Swing.");
        infoArea.setEditable(false);
        infoArea.setOpaque(false);
        infoArea.setFont(UITheme.BODY_FONT);
        infoArea.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        JButton okButton = new JButton("OK");
        UITheme.styleSecondaryButton(okButton);
        okButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(UITheme.WINDOW_BACKGROUND);
        buttonPanel.add(okButton);

        add(titleLabel, BorderLayout.NORTH);
        add(infoArea, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 280);
        setLocationRelativeTo(owner);
        setResizable(false);
    }
}

package com.smartcity.navigator.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.smartcity.navigator.model.PathResult;
import com.smartcity.navigator.utils.Formatter;

/**
 * Displays the outcome of the most recent route search: the total
 * distance, the route itself, and a running, timestamped activity log
 * in a scrollable text area (search history, load/save events, etc.).
 *
 * @author Smart City Route Navigator Team
 */
public class ResultPanel extends JPanel {

    private final JLabel distanceValueLabel = new JLabel("--");
    private final JLabel routeValueLabel = new JLabel(" ");
    private final JTextArea logArea = new JTextArea();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public ResultPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(UITheme.PANEL_BACKGROUND);
        setOpaque(true);
        setBorder(UITheme.createSectionBorder("Route Result"));

        JPanel summary = new JPanel(new GridLayout(2, 1, 6, 6));
        UITheme.styleSectionPanel(summary);

        JPanel distanceRow = new JPanel(new BorderLayout());
        UITheme.stylePanel(distanceRow);
        JLabel distanceLabel = new JLabel("Total Distance:");
        UITheme.styleLabel(distanceLabel);
        distanceValueLabel.setFont(UITheme.HEADER_FONT);
        distanceValueLabel.setForeground(UITheme.HEADING_COLOR);
        distanceRow.add(distanceLabel, BorderLayout.WEST);
        distanceRow.add(distanceValueLabel, BorderLayout.CENTER);

        JPanel routeRow = new JPanel(new BorderLayout());
        UITheme.stylePanel(routeRow);
        JLabel routeLabel = new JLabel("Route:");
        UITheme.styleLabel(routeLabel);
        routeValueLabel.setFont(UITheme.BODY_FONT);
        routeValueLabel.setForeground(UITheme.LABEL_COLOR);
        routeRow.add(routeLabel, BorderLayout.WEST);
        routeRow.add(routeValueLabel, BorderLayout.CENTER);

        summary.add(distanceRow);
        summary.add(routeRow);

        logArea.setEditable(false);
        logArea.setFont(UITheme.MONO_FONT);
        logArea.setBackground(UITheme.SUBPANEL_BACKGROUND);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Activity Log"),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        scrollPane.setPreferredSize(new Dimension(260, 200));

        add(summary, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Updates the distance/route sections and appends a log entry for
     * the given search outcome, whether it succeeded or not.
     */
    public void displayResult(PathResult result) {
        if (result.isPathFound()) {
            distanceValueLabel.setText(Formatter.formatDistance(result.getTotalDistance()));
            routeValueLabel.setText(result.getFormattedRoute());
            appendLog("Route found: " + result.getFormattedRoute()
                    + " (" + Formatter.formatDistance(result.getTotalDistance()) + ")");
        } else {
            distanceValueLabel.setText("N/A");
            routeValueLabel.setText("-");
            appendLog("No route: " + result.getMessage());
        }
    }

    /** Resets the distance/route sections to their empty state (log is preserved). */
    public void clear() {
        distanceValueLabel.setText("--");
        routeValueLabel.setText(" ");
    }

    /** Appends a timestamped line to the scrollable activity log. */
    public void appendLog(String message) {
        logArea.append("[" + timeFormat.format(new Date()) + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}

package com.smartcity.navigator.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
 * distance, estimated travel time, stops, path details, and activity log.
 *
 * @author Smart City Route Navigator Team
 */
public class ResultPanel extends JPanel {

    private static final String EMPTY_ROUTE_MESSAGE =
            "No route calculated yet. Choose an origin and destination, then click \"Find shortest route\".";

    private final JLabel distanceValueLabel = new JLabel("--");
    private final JLabel travelTimeValueLabel = new JLabel("--");
    private final JLabel stopsValueLabel = new JLabel("--");
    private final JTextArea routeArea = new JTextArea();
    private final JTextArea logArea = new JTextArea();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public ResultPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.WINDOW_BACKGROUND);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        JLabel titleIcon = new JLabel(IconFactory.getIcon(IconFactory.IconType.METRICS, 15, UITheme.HEADING_COLOR));
        JLabel title = new JLabel("Route Summary");
        UITheme.styleCardHeader(title);
        titleRow.add(titleIcon);
        titleRow.add(title);

        JPanel summaryCard = new JPanel(new GridBagLayout());
        UITheme.styleCardPanel(summaryCard);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        summaryCard.add(createStatSection("Total Distance", distanceValueLabel), c);

        c.gridy = 1;
        summaryCard.add(createStatSection("Estimated Time", travelTimeValueLabel), c);

        c.gridy = 2;
        summaryCard.add(createStatSection("Stops", stopsValueLabel), c);

        c.gridy = 3;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        summaryCard.add(createRouteSection(), c);

        logArea.setEditable(false);
        logArea.setFont(UITheme.MONO_FONT);
        logArea.setBackground(UITheme.SUBPANEL_BACKGROUND);
        logArea.setForeground(UITheme.HEADING_COLOR);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                    "Activity Log",
                    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION,
                    UITheme.LABEL_FONT,
                    UITheme.LABEL_COLOR
                ),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        logScrollPane.setPreferredSize(new Dimension(0, 160));

        add(titleRow, BorderLayout.NORTH);
        add(summaryCard, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.SOUTH);

        resetRouteAreaToEmptyState();
    }

    private JPanel createStatSection(String labelText, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(UITheme.SUBPANEL_BACKGROUND);
        row.setOpaque(true);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JLabel label = new JLabel(labelText + ":");
        UITheme.styleFieldLabel(label);
        valueLabel.setFont(UITheme.HEADER_FONT);
        valueLabel.setForeground(UITheme.HEADING_COLOR);

        row.add(label, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel createRouteSection() {
        JPanel container = new JPanel(new BorderLayout(6, 6));
        container.setBackground(UITheme.SUBPANEL_BACKGROUND);
        container.setOpaque(true);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel label = new JLabel("Route Path Details");
        UITheme.styleFieldLabel(label);

        routeArea.setEditable(false);
        routeArea.setFont(UITheme.BODY_FONT);
        routeArea.setForeground(UITheme.HEADING_COLOR);
        routeArea.setBackground(UITheme.PANEL_BACKGROUND);
        routeArea.setLineWrap(true);
        routeArea.setWrapStyleWord(true);
        routeArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane scrollPane = new JScrollPane(routeArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        container.add(label, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    /**
     * Updates distance, time, and stop count displays, appending a log entry for the route search.
     */
    public void displayResult(PathResult result) {
        if (result.isPathFound()) {
            distanceValueLabel.setText(Formatter.formatDistance(result.getTotalDistance()));
            travelTimeValueLabel.setText(formatTravelTime(result.getTotalDistance()));
            stopsValueLabel.setText(String.valueOf(result.getRoute().size()));
            routeArea.setForeground(UITheme.HEADING_COLOR);
            routeArea.setText(result.getFormattedRoute());
            appendLog("Route found: " + result.getFormattedRoute()
                    + " (" + Formatter.formatDistance(result.getTotalDistance()) + ")");
        } else {
            distanceValueLabel.setText("N/A");
            travelTimeValueLabel.setText("N/A");
            stopsValueLabel.setText("0");
            routeArea.setForeground(UITheme.ERROR_COLOR);
            routeArea.setText("No route available. " + result.getMessage());
            appendLog("No route: " + result.getMessage());
        }
    }

    private String formatTravelTime(double distanceKm) {
        if (Double.isInfinite(distanceKm)) {
            return "--";
        }
        int minutes = (int) Math.round(distanceKm / 50.0 * 60.0);
        if (minutes < 60) {
            return minutes + " min";
        }
        int hours = minutes / 60;
        int remaining = minutes % 60;
        return hours + " h " + remaining + " min";
    }

    /** Resets the distance/route sections to their empty state (log is preserved). */
    public void clear() {
        distanceValueLabel.setText("--");
        travelTimeValueLabel.setText("--");
        stopsValueLabel.setText("--");
        resetRouteAreaToEmptyState();
    }

    private void resetRouteAreaToEmptyState() {
        routeArea.setForeground(UITheme.SECONDARY_TEXT);
        routeArea.setText(EMPTY_ROUTE_MESSAGE);
    }

    /** Appends a timestamped line to the scrollable activity log. */
    public void appendLog(String message) {
        logArea.append("[" + timeFormat.format(new Date()) + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
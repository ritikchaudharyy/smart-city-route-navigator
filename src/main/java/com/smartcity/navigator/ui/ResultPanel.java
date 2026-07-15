package com.smartcity.navigator.ui;

import com.smartcity.navigator.model.PathResult;
import com.smartcity.navigator.utils.Formatter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        setBorder(BorderFactory.createTitledBorder("Route Result"));

        JPanel summary = new JPanel(new GridLayout(2, 1, 4, 4));

        JPanel distanceRow = new JPanel(new BorderLayout());
        distanceRow.add(new JLabel("Total Distance: "), BorderLayout.WEST);
        distanceValueLabel.setFont(distanceValueLabel.getFont().deriveFont(Font.BOLD, 15f));
        distanceRow.add(distanceValueLabel, BorderLayout.CENTER);

        JPanel routeRow = new JPanel(new BorderLayout());
        routeRow.add(new JLabel("Route: "), BorderLayout.WEST);
        routeValueLabel.setFont(routeValueLabel.getFont().deriveFont(Font.PLAIN, 13f));
        routeRow.add(routeValueLabel, BorderLayout.CENTER);

        summary.add(distanceRow);
        summary.add(routeRow);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Activity Log"));
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

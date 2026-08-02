package com.smartcity.navigator.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.smartcity.navigator.model.Location;
import com.smartcity.navigator.service.RouteService;
import com.smartcity.navigator.service.ai.GeminiService;
import com.smartcity.navigator.utils.AppLogger;

/**
 * Route controls for both deterministic manual selection and AI-assisted
 * location extraction and graph-edit command parsing. Gemini supplies only
 * validated parameters; this panel always delegates graph changes and routing
 * to the local {@link RouteService}.
 */
public final class RoutePanel extends JPanel {

    private final MainFrame mainFrame;
    private final RouteService routeService;
    private final GeminiService geminiService;

    private final JComboBox<LocationWrapper> sourceCombo = new JComboBox<>();
    private final JComboBox<LocationWrapper> destinationCombo = new JComboBox<>();
    private final JTextField aiInputField = new JTextField();
    private final JButton askAiButton = new JButton("Interpret request");
    private final JButton findRouteButton = new JButton("Find shortest route");
    private final JButton clearButton = new JButton("Clear route");
    private final JButton swapButton = new JButton();
    private final JProgressBar loadingBar = new JProgressBar();
    private final JLabel statusLabel = new JLabel("Ready");

    public RoutePanel(MainFrame mainFrame, RouteService routeService, GeminiService geminiService) {
        this.mainFrame = mainFrame;
        this.routeService = routeService;
        this.geminiService = geminiService;

        setLayout(new BorderLayout());
        setBackground(UITheme.WINDOW_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        configureComponents();
        JPanel content = buildContent();
        add(content, BorderLayout.CENTER);
        // The previous fixed height of zero collapsed this panel inside the
        // planner drawer, clipping the AI and manual route controls. Let the
        // content determine its natural height while retaining a stable width.
        Dimension preferredContentSize = content.getPreferredSize();
        setPreferredSize(new Dimension(340, preferredContentSize.height + 24));
        setMinimumSize(new Dimension(300, 0));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        setupEventHandlers();
        loadLocations();
    }

    private void configureComponents() {
        UITheme.styleComboBox(sourceCombo);
        UITheme.styleComboBox(destinationCombo);

        UITheme.styleTextField(aiInputField);
        aiInputField.putClientProperty("JTextField.placeholderText", "Route or edit: Library ko Hospital ke paas 4 km add karo");
        aiInputField.setToolTipText("Describe a route or graph edit in English, Hindi, or a mixture of both.");

        UITheme.stylePrimaryActionButton(askAiButton);
        askAiButton.setIcon(IconFactory.getIcon(IconFactory.IconType.AI_SPARKLE, 16, Color.WHITE));
        askAiButton.setToolTipText("Use Gemini to interpret a route or a safe graph-edit command.");

        UITheme.stylePrimaryActionButton(findRouteButton);
        findRouteButton.setIcon(IconFactory.getIcon(IconFactory.IconType.SEARCH_ROUTE, 16, Color.WHITE));
        findRouteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        UITheme.styleSecondaryToolBarButton(clearButton);
        clearButton.setIcon(IconFactory.getIcon(IconFactory.IconType.CLEAR, 14, UITheme.LABEL_COLOR));

        UITheme.styleSecondaryToolBarButton(swapButton);
        swapButton.setIcon(IconFactory.getIcon(IconFactory.IconType.SWAP, 15, UITheme.LABEL_COLOR));
        swapButton.setToolTipText("Swap the selected origin and destination.");
        swapButton.setMargin(new Insets(6, 10, 6, 10));

        loadingBar.setIndeterminate(true);
        loadingBar.setForeground(UITheme.ACCENT_PRIMARY);
        loadingBar.setVisible(false);

        statusLabel.setFont(UITheme.LABEL_FONT);
        showStatus("Ready", UITheme.SECONDARY_TEXT);
    }

    private JPanel buildContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.WINDOW_BACKGROUND);
        content.add(buildAiCommandCard());
        content.add(Box.createVerticalStrut(14));
        content.add(buildManualSelectionCard());
        return content;
    }

    private JPanel buildAiCommandCard() {
        JPanel card = createCard();
        GridBagConstraints constraints = createConstraints();

        card.add(buildCardTitle("AI ROUTE ASSISTANT", IconFactory.IconType.AI_SPARKLE, UITheme.ACCENT_PRIMARY), constraints);

        constraints.gridy++;
        constraints.insets = new Insets(4, 0, 10, 0);
        JLabel description = new JLabel("Route, add/remove location, or add/remove two-way road.");
        description.setFont(UITheme.LABEL_FONT);
        description.setForeground(UITheme.SECONDARY_TEXT);
        card.add(description, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 10, 0);
        card.add(aiInputField, constraints);

        constraints.gridy++;
        card.add(askAiButton, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(8, 0, 2, 0);
        card.add(loadingBar, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(4, 0, 0, 0);
        card.add(statusLabel, constraints);
        return card;
    }

    private JPanel buildManualSelectionCard() {
        JPanel card = createCard();
        GridBagConstraints constraints = createConstraints();

        card.add(buildCardTitle("MANUAL ROUTE PLANNER", IconFactory.IconType.MAP, UITheme.HEADING_COLOR), constraints);

        constraints.gridy++;
        constraints.insets = new Insets(12, 0, 4, 0);
        card.add(createFieldLabel("Origin"), constraints);

        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 6, 0);
        card.add(sourceCombo, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 0, 0);
        card.add(buildSwapRow(), constraints);

        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 4, 0);
        card.add(createFieldLabel("Destination"), constraints);

        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 14, 0);
        card.add(destinationCombo, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 8, 0);
        card.add(findRouteButton, constraints);

        constraints.gridy++;
        constraints.insets = new Insets(0, 0, 0, 0);
        card.add(clearButton, constraints);
        return card;
    }

    private JPanel buildSwapRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        row.setOpaque(false);
        row.add(swapButton);
        return row;
    }

    private JPanel buildCardTitle(String text, IconFactory.IconType iconType, Color color) {
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JLabel icon = new JLabel(IconFactory.getIcon(iconType, 15, color));
        JLabel title = new JLabel(text);
        title.setFont(UITheme.SECTION_FONT);
        title.setForeground(color);

        titleRow.add(icon);
        titleRow.add(title);
        return titleRow;
    }

    private JPanel createCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setAlignmentX(LEFT_ALIGNMENT);
        UITheme.styleCardPanel(card);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private GridBagConstraints createConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 0, 0, 0);
        return constraints;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        UITheme.styleFieldLabel(label);
        return label;
    }

    private void setupEventHandlers() {
        askAiButton.addActionListener(this::handleAiSearch);
        aiInputField.addActionListener(this::handleAiSearch);
        findRouteButton.addActionListener(event -> triggerRouteCalculation());
        clearButton.addActionListener(event -> clearNavigation());
        swapButton.addActionListener(event -> swapSelections());
    }

    /** Reloads the current graph locations into both manual selectors. */
    public void loadLocations() {
        try {
            Collection<Location> locations = routeService.getAllLocations();
            refreshLocations(locations);
            showStatus(locations.isEmpty()
                    ? "No locations are loaded."
                    : "Ready - " + locations.size() + " locations available.",
                    locations.isEmpty() ? UITheme.WARNING_COLOR : UITheme.SECONDARY_TEXT);
        } catch (RuntimeException exception) {
            AppLogger.error("Failed to load graph locations into route controls", exception);
            showStatus("Unable to read the loaded city graph.", UITheme.ERROR_COLOR);
        }
    }

    /** Refreshes the selectors while preserving any still-valid selections. */
    public void refreshLocations(Collection<Location> locations) {
        String previousSourceId = getSelectedSourceId();
        String previousDestinationId = getSelectedDestinationId();

        sourceCombo.removeAllItems();
        destinationCombo.removeAllItems();
        if (locations != null) {
            for (Location location : locations) {
                sourceCombo.addItem(new LocationWrapper(location));
                destinationCombo.addItem(new LocationWrapper(location));
            }
        }

        boolean sourceRestored = selectItemById(sourceCombo, previousSourceId);
        boolean destinationRestored = selectItemById(destinationCombo, previousDestinationId);
        if (!sourceRestored && sourceCombo.getItemCount() > 0) {
            sourceCombo.setSelectedIndex(0);
        }
        if (!destinationRestored && destinationCombo.getItemCount() > 1) {
            destinationCombo.setSelectedIndex(1);
        } else if (!destinationRestored) {
            destinationCombo.setSelectedIndex(-1);
        }
    }

    public String getSelectedSourceId() {
        return getSelectedId(sourceCombo);
    }

    public String getSelectedDestinationId() {
        return getSelectedId(destinationCombo);
    }

    /** Restores the controls to an empty state without mutating the graph. */
    public void clearSelection() {
        sourceCombo.setSelectedIndex(-1);
        destinationCombo.setSelectedIndex(-1);
        aiInputField.setText("");
        showStatus("Route cleared.", UITheme.SECONDARY_TEXT);
    }

    private void handleAiSearch(ActionEvent ignored) {
        String query = aiInputField.getText().trim();
        if (query.isEmpty()) {
            showStatus("Describe the origin and destination for the AI assistant.", UITheme.WARNING_COLOR);
            aiInputField.requestFocusInWindow();
            return;
        }

        setLoadingState(true);
        showStatus("AI is matching your request to the city map...", UITheme.ACCENT_PRIMARY);
        geminiService.parseRouteQueryAsync(query, routeService.getAllLocations(), new GeminiService.AIParserCallback() {
            @Override
            public void onSuccess(String sourceId, String destinationId) {
                setLoadingState(false);
                boolean sourceMatched = selectItemById(sourceCombo, sourceId);
                boolean destinationMatched = selectItemById(destinationCombo, destinationId);
                if (!sourceMatched || !destinationMatched) {
                    showStatus("AI returned a location that is not in the current city map.", UITheme.ERROR_COLOR);
                    return;
                }
                showStatus("Route identified. Calculating the local shortest path...", UITheme.SUCCESS_COLOR);
                triggerRouteCalculation();
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoadingState(false);
                showStatus(errorMessage, UITheme.ERROR_COLOR);
            }
        });
    }

    private void triggerRouteCalculation() {
        String sourceId = getSelectedSourceId();
        String destinationId = getSelectedDestinationId();
        if (sourceId == null || destinationId == null) {
            showManualSelectionWarning("Select both an origin and destination first.");
            return;
        }
        if (sourceId.equals(destinationId)) {
            showManualSelectionWarning("Origin and destination must be different locations.");
            return;
        }

        showStatus("Calculating route with the local Dijkstra engine...", UITheme.ACCENT_PRIMARY);
        mainFrame.calculateAndDisplayRoute(sourceId, destinationId);
    }

    private void swapSelections() {
        LocationWrapper source = (LocationWrapper) sourceCombo.getSelectedItem();
        LocationWrapper destination = (LocationWrapper) destinationCombo.getSelectedItem();
        sourceCombo.setSelectedItem(destination);
        destinationCombo.setSelectedItem(source);
        showStatus("Origin and destination swapped.", UITheme.SECONDARY_TEXT);
    }

    private void clearNavigation() {
        clearSelection();
        mainFrame.clearActiveRoute();
    }

    private void setLoadingState(boolean loading) {
        askAiButton.setEnabled(!loading);
        findRouteButton.setEnabled(!loading);
        clearButton.setEnabled(!loading);
        swapButton.setEnabled(!loading);
        aiInputField.setEnabled(!loading);
        sourceCombo.setEnabled(!loading);
        destinationCombo.setEnabled(!loading);
        loadingBar.setVisible(loading);
    }

    private void showManualSelectionWarning(String message) {
        showStatus(message, UITheme.WARNING_COLOR);
        JOptionPane.showMessageDialog(this, message, "Route selection", JOptionPane.WARNING_MESSAGE);
    }

    private void showStatus(String message, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setToolTipText(message);
        statusLabel.setText("<html><div style='width: 300px;'>" + escapeHtml(message) + "</div></html>");
        revalidate();
        repaint();
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br>");
    }

    private static String getSelectedId(JComboBox<LocationWrapper> comboBox) {
        LocationWrapper selected = (LocationWrapper) comboBox.getSelectedItem();
        return selected == null ? null : selected.location.getId();
    }

    private static boolean selectItemById(JComboBox<LocationWrapper> comboBox, String targetId) {
        if (targetId == null || targetId.isBlank()) {
            return false;
        }
        for (int index = 0; index < comboBox.getItemCount(); index++) {
            LocationWrapper item = comboBox.getItemAt(index);
            if (item.location.getId().equalsIgnoreCase(targetId.trim())) {
                comboBox.setSelectedIndex(index);
                return true;
            }
        }
        return false;
    }

    private static final class LocationWrapper {
        private final Location location;

        private LocationWrapper(Location location) {
            this.location = location;
        }

        @Override
        public String toString() {
            return location.getName() + " (" + location.getId() + ")";
        }
    }
}

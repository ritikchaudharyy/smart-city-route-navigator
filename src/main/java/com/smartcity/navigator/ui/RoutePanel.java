package com.smartcity.navigator.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.smartcity.navigator.model.Location;

/**
 * Lets the user pick a source and destination location and trigger a
 * route search. Holds no business logic itself — it only reports the
 * user's selection and forwards button clicks to whatever callbacks
 * {@link MainFrame} registers via {@link #setOnFindRoute} and
 * {@link #setOnReset}; the actual pathfinding happens in
 * {@code RouteService}.
 *
 * @author Smart City Route Navigator Team
 */
public class RoutePanel extends JPanel {

    private final JComboBox<Location> sourceCombo = new JComboBox<>();
    private final JComboBox<Location> destinationCombo = new JComboBox<>();
    private final JButton findRouteButton = new JButton("Find Route", IconFactory.create(IconFactory.IconType.FIND, 16, Color.WHITE));
    private final JButton resetButton = new JButton("Reset");

    public RoutePanel() {
        setLayout(new GridBagLayout());
        setBackground(UITheme.PANEL_BACKGROUND);
        setOpaque(true);
        setBorder(UITheme.createSectionBorder("Route Selection"));

        JLabel sourceLabel = new JLabel("Source:");
        JLabel destinationLabel = new JLabel("Destination:");

        UITheme.styleLabel(sourceLabel);
        UITheme.styleLabel(destinationLabel);

        UITheme.styleComboBox(sourceCombo);
        UITheme.styleComboBox(destinationCombo);

        UITheme.styleButton(findRouteButton);
        UITheme.styleSecondaryButton(resetButton);

        findRouteButton.setToolTipText("Calculate the best route between the selected locations.");
        resetButton.setToolTipText("Clear both source and destination selections.");
        findRouteButton.setMnemonic('F');
        resetButton.setMnemonic('R');

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 12, 10, 12);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        add(sourceLabel, c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        add(sourceCombo, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        add(destinationLabel, c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        add(destinationCombo, c);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        buttonRow.setOpaque(false);
        buttonRow.add(resetButton);
        buttonRow.add(findRouteButton);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.weightx = 1;
        add(buttonRow, c);
    }

    /**
     * Repopulates both dropdowns from the current graph's locations,
     * preserving the previous selection if it's still present (e.g. after
     * a "Refresh" that didn't change the underlying data).
     *
     * @param locations current locations, in the order they should be listed
     */
    public void refreshLocations(Collection<Location> locations) {
        Location previousSource = (Location) sourceCombo.getSelectedItem();
        Location previousDestination = (Location) destinationCombo.getSelectedItem();

        List<Location> locationList = new ArrayList<>(locations);
        sourceCombo.setModel(new DefaultComboBoxModel<>(locationList.toArray(Location[]::new)));
        destinationCombo.setModel(new DefaultComboBoxModel<>(locationList.toArray(Location[]::new)));

        if (previousSource != null && locationList.contains(previousSource)) {
            sourceCombo.setSelectedItem(previousSource);
        }
        if (previousDestination != null && locationList.contains(previousDestination)) {
            destinationCombo.setSelectedItem(previousDestination);
        }
    }

    /** @return the id of the currently selected source, or {@code null} if none is selected */
    public String getSelectedSourceId() {
        Location selected = (Location) sourceCombo.getSelectedItem();
        return selected == null ? null : selected.getId();
    }

    /** @return the id of the currently selected destination, or {@code null} if none is selected */
    public String getSelectedDestinationId() {
        Location selected = (Location) destinationCombo.getSelectedItem();
        return selected == null ? null : selected.getId();
    }

    /** Registers the action run when "Find Route" is clicked. */
    public void setOnFindRoute(Runnable action) {
        findRouteButton.addActionListener(e -> action.run());
    }

    /** Registers the action run when "Reset" is clicked. */
    public void setOnReset(Runnable action) {
        resetButton.addActionListener(e -> action.run());
    }

    /** Clears both dropdown selections (used by Reset/Clear). */
    public void clearSelection() {
        sourceCombo.setSelectedIndex(-1);
        destinationCombo.setSelectedIndex(-1);
    }
}

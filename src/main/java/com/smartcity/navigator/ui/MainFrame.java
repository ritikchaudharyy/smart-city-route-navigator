package com.smartcity.navigator.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.smartcity.navigator.graph.GraphLoadException;
import com.smartcity.navigator.model.Location;
import com.smartcity.navigator.model.PathResult;
import com.smartcity.navigator.service.RouteService;
import com.smartcity.navigator.utils.Constants;
import com.smartcity.navigator.utils.Formatter;
import com.smartcity.navigator.utils.Helpers;

/**
 * The application's main window. Assembles the menu bar, toolbar, route
 * selection panel, result panel, and map panel, and wires every menu
 * item, toolbar button, and panel callback to {@link RouteService}.
 * <p>
 * Per the project architecture, this class contains no pathfinding or
 * validation logic of its own — every action here does at most: read
 * user input, call one {@code RouteService} method, and display the
 * result via {@link ResultPanel}, {@link MapPanel}, or a dialog.
 *
 * @author Smart City Route Navigator Team
 */
public class MainFrame extends JFrame {

    private final RouteService routeService;
    private final RoutePanel routePanel = new RoutePanel();
    private final ResultPanel resultPanel = new ResultPanel();
    private final MapPanel mapPanel = new MapPanel();
    private final JLabel statusBar = new JLabel("Ready");

    public MainFrame(RouteService routeService) {
        super(Constants.APP_TITLE);
        this.routeService = routeService;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        setJMenuBar(buildMenuBar());
        add(buildToolBar(), BorderLayout.NORTH);
        add(buildCenterLayout(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        routePanel.setOnFindRoute(this::onFindRoute);
        routePanel.setOnReset(this::onClear);

        mapPanel.setGraph(routeService.getGraph());
        routePanel.refreshLocations(routeService.getAllLocations());

        setSize(Constants.DEFAULT_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_HEIGHT);
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------
    // Layout construction
    // ------------------------------------------------------------------

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(buildFileMenu());
        menuBar.add(buildEditMenu());
        menuBar.add(buildViewMenu());
        menuBar.add(buildHelpMenu());
        return menuBar;
    }

    private JMenu buildFileMenu() {
        JMenu fileMenu = new JMenu("File");

        JMenuItem newCity = new JMenuItem("New City", IconFactory.create(IconFactory.IconType.NEW_CITY, 16, Color.DARK_GRAY));
        JMenuItem loadGraph = new JMenuItem("Load Graph", IconFactory.create(IconFactory.IconType.LOAD, 16, Color.DARK_GRAY));
        JMenuItem saveGraph = new JMenuItem("Save Graph", IconFactory.create(IconFactory.IconType.SAVE, 16, Color.DARK_GRAY));
        JMenuItem exitItem = new JMenuItem("Exit", IconFactory.create(IconFactory.IconType.EXIT, 16, Color.DARK_GRAY));

        newCity.addActionListener(e -> onNewCity());
        loadGraph.addActionListener(e -> onLoadGraph());
        saveGraph.addActionListener(e -> onSaveGraph());
        exitItem.addActionListener(e -> confirmExit());

        fileMenu.add(newCity);
        fileMenu.add(loadGraph);
        fileMenu.add(saveGraph);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        return fileMenu;
    }

    private JMenu buildEditMenu() {
        JMenu editMenu = new JMenu("Edit Graph");

        JMenuItem addLocation = new JMenuItem("Add Location...");
        JMenuItem removeLocation = new JMenuItem("Remove Location...");
        JMenuItem addRoad = new JMenuItem("Add Road...");
        JMenuItem removeRoad = new JMenuItem("Remove Road...");

        addLocation.addActionListener(e -> onAddLocation());
        removeLocation.addActionListener(e -> onRemoveLocation());
        addRoad.addActionListener(e -> onAddRoad());
        removeRoad.addActionListener(e -> onRemoveRoad());

        editMenu.add(addLocation);
        editMenu.add(removeLocation);
        editMenu.addSeparator();
        editMenu.add(addRoad);
        editMenu.add(removeRoad);
        return editMenu;
    }

    private JMenu buildViewMenu() {
        JMenu viewMenu = new JMenu("View");

        JMenuItem zoomIn = new JMenuItem("Zoom In", IconFactory.create(IconFactory.IconType.ZOOM_IN, 16, Color.DARK_GRAY));
        JMenuItem zoomOut = new JMenuItem("Zoom Out", IconFactory.create(IconFactory.IconType.ZOOM_OUT, 16, Color.DARK_GRAY));
        JMenuItem resetView = new JMenuItem("Reset", IconFactory.create(IconFactory.IconType.RESET_VIEW, 16, Color.DARK_GRAY));
        JMenuItem settingsItem = new JMenuItem("Settings...", IconFactory.create(IconFactory.IconType.SETTINGS, 16, Color.DARK_GRAY));

        zoomIn.addActionListener(e -> {
            mapPanel.zoomIn();
            statusBar.setText("Zoomed in");
        });
        zoomOut.addActionListener(e -> {
            mapPanel.zoomOut();
            statusBar.setText("Zoomed out");
        });
        resetView.addActionListener(e -> {
            mapPanel.resetZoom();
            statusBar.setText("View reset");
        });
        settingsItem.addActionListener(e -> onSettings());

        viewMenu.add(zoomIn);
        viewMenu.add(zoomOut);
        viewMenu.add(resetView);
        viewMenu.addSeparator();
        viewMenu.add(settingsItem);
        return viewMenu;
    }

    private JMenu buildHelpMenu() {
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About", IconFactory.create(IconFactory.IconType.INFO, 16, Color.DARK_GRAY));
        aboutItem.addActionListener(e -> new AboutDialog(this).setVisible(true));
        helpMenu.add(aboutItem);
        return helpMenu;
    }

    private JToolBar buildToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton findButton = new JButton("Find Route", IconFactory.create(IconFactory.IconType.FIND, 18, new Color(30, 110, 40)));
        JButton clearButton = new JButton("Clear", IconFactory.create(IconFactory.IconType.CLEAR, 18, new Color(150, 40, 40)));
        JButton refreshButton = new JButton("Refresh", IconFactory.create(IconFactory.IconType.REFRESH, 18, new Color(30, 80, 150)));
        JButton exitButton = new JButton("Exit", IconFactory.create(IconFactory.IconType.EXIT, 18, Color.DARK_GRAY));

        findButton.addActionListener(e -> onFindRoute());
        clearButton.addActionListener(e -> onClear());
        refreshButton.addActionListener(e -> onRefresh());
        exitButton.addActionListener(e -> confirmExit());

        toolBar.add(findButton);
        toolBar.add(clearButton);
        toolBar.add(refreshButton);
        toolBar.addSeparator();
        toolBar.add(exitButton);
        return toolBar;
    }

    private JComponent buildCenterLayout() {
        JPanel westPanel = new JPanel(new BorderLayout(8, 8));
        westPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 4));
        westPanel.add(routePanel, BorderLayout.NORTH);
        westPanel.add(resultPanel, BorderLayout.CENTER);
        westPanel.setPreferredSize(new Dimension(320, 0));

        JScrollPane mapScroll = new JScrollPane(mapPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westPanel, mapScroll);
        splitPane.setDividerLocation(340);
        splitPane.setResizeWeight(0);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JComponent buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusBar.setText(Formatter.formatLocationStatus("Ready", routeService.getAllLocations().size()));
        panel.add(statusBar, BorderLayout.WEST);
        return panel;
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void onFindRoute() {
        String sourceId = routePanel.getSelectedSourceId();
        String destinationId = routePanel.getSelectedDestinationId();

        PathResult result = routeService.findRoute(sourceId, destinationId);
        resultPanel.displayResult(result);
        statusBar.setText(Formatter.formatStatusMessage(result));

        if (result.isPathFound()) {
            mapPanel.highlightRoute(result.getRoute());
        } else {
            mapPanel.clearHighlight();
            JOptionPane.showMessageDialog(this, result.getMessage(), "Route Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onClear() {
        routePanel.clearSelection();
        resultPanel.clear();
        mapPanel.clearHighlight();
        statusBar.setText("Ready");
    }

    private void onRefresh() {
        routePanel.refreshLocations(routeService.getAllLocations());
        mapPanel.setGraph(routeService.getGraph());
        statusBar.setText(Formatter.formatLocationStatus("Refreshed", routeService.getAllLocations().size()));
    }

    private void onAddLocation() {
        JPanel panel = buildInputPanel();
        JTextField idField = addLabeledField(panel, "Location ID:");
        JTextField nameField = addLabeledField(panel, "Name:");
        JTextField xField = addLabeledField(panel, "X:");
        JTextField yField = addLabeledField(panel, "Y:");

        xField.setText("0");
        yField.setText("0");

        int confirm = showDialog(panel, "Add Location");
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            routeService.addLocation(idField.getText(), nameField.getText(),
                    Helpers.safeParseDouble(xField.getText(), 0.0),
                    Helpers.safeParseDouble(yField.getText(), 0.0));
            onRefresh();
            onClear();
            resultPanel.appendLog("Added location " + idField.getText().trim());
            statusBar.setText("Location added");
        } catch (IllegalArgumentException ex) {
            showError("Add Location", ex.getMessage());
        }
    }

    private void onRemoveLocation() {
        if (routeService.getAllLocations().isEmpty()) {
            showError("Remove Location", "No locations are available to remove.");
            return;
        }

        String[] locationIds = routeService.getAllLocations().stream()
                .map(Location::getId)
                .sorted(Comparator.naturalOrder())
                .toArray(String[]::new);
        JComboBox<String> comboBox = new JComboBox<>(locationIds);
        int confirm = showDialog(comboBox, "Remove Location");
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        String selectedId = (String) comboBox.getSelectedItem();
        if (selectedId == null) {
            return;
        }

        boolean removed = routeService.removeLocation(selectedId);
        if (!removed) {
            showError("Remove Location", "The selected location could not be removed.");
            return;
        }

        onRefresh();
        onClear();
        resultPanel.appendLog("Removed location " + selectedId);
        statusBar.setText("Location removed");
    }

    private void onAddRoad() {
        if (routeService.getAllLocations().size() < 2) {
            showError("Add Road", "At least two locations are required to add a road.");
            return;
        }

        JPanel panel = buildInputPanel();
        String[] locationIds = routeService.getAllLocations().stream()
                .map(Location::getId)
                .sorted(Comparator.naturalOrder())
                .toArray(String[]::new);
        JComboBox<String> sourceCombo = new JComboBox<>(locationIds);
        JComboBox<String> destinationCombo = new JComboBox<>(locationIds);
        JTextField weightField = addLabeledField(panel, "Weight:");
        weightField.setText("1");

        panel.add(new JLabel("Source:"));
        panel.add(sourceCombo);
        panel.add(new JLabel("Destination:"));
        panel.add(destinationCombo);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        int confirm = showDialog(panel, "Add Road");
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        String sourceId = (String) sourceCombo.getSelectedItem();
        String destinationId = (String) destinationCombo.getSelectedItem();
        try {
            routeService.addRoad(sourceId, destinationId, Helpers.safeParseDouble(weightField.getText(), 1.0));
            onRefresh();
            onClear();
            resultPanel.appendLog("Added road " + sourceId + " -> " + destinationId);
            statusBar.setText("Road added");
        } catch (IllegalArgumentException ex) {
            showError("Add Road", ex.getMessage());
        }
    }

    private void onRemoveRoad() {
        if (routeService.getAllLocations().size() < 2) {
            showError("Remove Road", "At least two locations are required to remove a road.");
            return;
        }

        String[] locationIds = routeService.getAllLocations().stream()
                .map(Location::getId)
                .sorted(Comparator.naturalOrder())
                .toArray(String[]::new);
        JPanel panel = buildInputPanel();
        JComboBox<String> sourceCombo = new JComboBox<>(locationIds);
        JComboBox<String> destinationCombo = new JComboBox<>(locationIds);

        panel.add(new JLabel("Source:"));
        panel.add(sourceCombo);
        panel.add(new JLabel("Destination:"));
        panel.add(destinationCombo);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        int confirm = showDialog(panel, "Remove Road");
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        String sourceId = (String) sourceCombo.getSelectedItem();
        String destinationId = (String) destinationCombo.getSelectedItem();
        boolean removed = routeService.removeRoad(sourceId, destinationId);
        if (!removed) {
            showError("Remove Road", "The selected road could not be removed.");
            return;
        }

        onRefresh();
        onClear();
        resultPanel.appendLog("Removed road " + sourceId + " -> " + destinationId);
        statusBar.setText("Road removed");
    }

    private JPanel buildInputPanel() {
        return DialogHelper.createInputPanel();
    }

    private int showDialog(JComponent content, String title) {
        return JOptionPane.showConfirmDialog(this, content, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
    }

    private JTextField addLabeledField(JPanel panel, String label) {
        return DialogHelper.addLabeledField(panel, label);
    }

    private void onNewCity() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "This will reset to the default city and discard any unsaved changes. Continue?",
                "New City", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            routeService.resetToDefaultCity();
            onRefresh();
            onClear();
            resultPanel.appendLog("Loaded default city.");
        } catch (GraphLoadException ex) {
            showError("Failed to load default city", ex.getMessage());
        }
    }

    private void onLoadGraph() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("City Graph Data (*.dat)", "dat"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            routeService.loadGraphFromFile(chooser.getSelectedFile());
            onRefresh();
            onClear();
            resultPanel.appendLog("Loaded graph from " + chooser.getSelectedFile().getName());
        } catch (GraphLoadException ex) {
            showError("Failed to load graph", ex.getMessage());
        }
    }

    private void onSaveGraph() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("City Graph Data (*.dat)", "dat"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".dat")) {
            file = new File(file.getParentFile(), file.getName() + ".dat");
        }

        try {
            routeService.saveGraphToFile(file);
            statusBar.setText("Graph saved to " + file.getName());
            resultPanel.appendLog("Saved graph to " + file.getName());
        } catch (GraphLoadException ex) {
            showError("Failed to save graph", ex.getMessage());
        }
    }

    private void onSettings() {
        SettingsDialog dialog = new SettingsDialog(this);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            mapPanel.repaint();
            resultPanel.appendLog("Settings updated.");
            statusBar.setText("Settings updated");
        }
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
}

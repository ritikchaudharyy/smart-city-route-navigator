package com.smartcity.navigator.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import com.smartcity.navigator.graph.CityGraph;
import com.smartcity.navigator.model.Edge;
import com.smartcity.navigator.model.Location;
import com.smartcity.navigator.model.PathResult;
import com.smartcity.navigator.service.RouteService;
import com.smartcity.navigator.service.ai.GeminiService;
import com.smartcity.navigator.utils.AppLogger;

/**
 * Main application dashboard window assembling the Navigation Sidebar,
 * Integrated Route Controls (AI + Manual), Graph Visualization Canvas,
 * and Metrics Summary panel.
 */
public class MainFrame extends JFrame {

    private final RouteService routeService;
    private final GeminiService geminiService;

    // Core UI Panels
    private RoutePanel routePanel;
    private ResultPanel resultPanel;
    private MapPanel plannerMapPanel;
    private MapPanel fullMapPanel;

    // View Switching
    private final CardLayout workspaceCardLayout = new CardLayout();
    private final JPanel workspaceContainer = new JPanel(workspaceCardLayout);

    // Sidebar Navigation Controls
    private JToggleButton navPlannerBtn;
    private JToggleButton navMapBtn;
    private JLabel statusLabel;

    // Workspace header
    private JLabel workspaceTitle;
    private JLabel workspaceSubtitle;
    private JLabel graphStatusChip;
    private boolean graphDirty;
    private File currentGraphFile;

    public MainFrame(RouteService routeService, GeminiService geminiService) {
        this.routeService = routeService;
        this.geminiService = geminiService;

        setTitle("Smart City Route Navigator - Enterprise Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 800));
        setSize(new Dimension(1280, 800));
        setLocationRelativeTo(null);

        initComponents();
        setupMenuBar();
        setupLayout();
        loadGraphData();
    }

    private void initComponents() {
        routePanel = new RoutePanel(this, routeService, geminiService);
        resultPanel = new ResultPanel();
        plannerMapPanel = new MapPanel();
        fullMapPanel = new MapPanel();
        MapPanel.GraphInteractionHandler handler = new MapPanel.GraphInteractionHandler() {
            @Override
            public void addLocation(double x, double y) {
                showAddLocationDialog(x, y);
            }

            @Override
            public void deleteLocation(Location location) {
                if (JOptionPane.showConfirmDialog(MainFrame.this,
                        "Remove " + location.getName() + " and all connected roads?", "Confirm removal",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION
                        && routeService.removeLocation(location.getId())) {
                    graphWasModified("Location removed.");
                }
            }

            @Override
            public void addRoad(String sourceId, String destinationId) {
                showAddRoadDialog(sourceId, destinationId);
            }

            @Override
            public void deleteRoad(Edge edge) {
                if (JOptionPane.showConfirmDialog(MainFrame.this,
                        "Remove road " + edge.getSourceId() + " ↔ " + edge.getDestinationId() + "?",
                        "Confirm removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION
                        && routeService.removeRoad(edge.getSourceId(), edge.getDestinationId())) {
                    graphWasModified("Road removed.");
                }
            }
        };
        plannerMapPanel.setInteractionHandler(handler);
        fullMapPanel.setInteractionHandler(handler);

        statusLabel = new JLabel("Ready");
        UITheme.styleStatusLabel(statusLabel);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        UITheme.styleMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        UITheme.styleMenu(fileMenu);
        JMenuItem newCityItem = createMenuItem("New City", IconFactory.IconType.NEW_CITY, e -> createNewCity());
        JMenuItem loadItem = createMenuItem("Load Graph...", IconFactory.IconType.LOAD, e -> loadGraphFromFile());
        JMenuItem saveItem = createMenuItem("Save Graph", IconFactory.IconType.SAVE, e -> saveGraphToFile(false));
        JMenuItem saveAsItem = createMenuItem("Save Graph As...", IconFactory.IconType.SAVE, e -> saveGraphToFile(true));
        fileMenu.add(newCityItem);
        fileMenu.add(loadItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Exit", IconFactory.getIcon(IconFactory.IconType.EXIT, 15, UITheme.LABEL_COLOR));
        UITheme.styleMenuItem(exitItem);
        exitItem.addActionListener(e -> {
            if (confirmDiscardChanges("exit the application")) {
                dispose();
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);

        JMenu graphMenu = new JMenu("Graph");
        UITheme.styleMenu(graphMenu);
        JMenuItem addLocationItem = createMenuItem("Add Location...", IconFactory.IconType.NEW_CITY, e -> showAddLocationDialog());
        JMenuItem removeLocationItem = createMenuItem("Remove Location...", IconFactory.IconType.CLEAR, e -> showRemoveLocationDialog());
        JMenuItem addRoadItem = createMenuItem("Add Road...", IconFactory.IconType.MAP, e -> showAddRoadDialog());
        JMenuItem removeRoadItem = createMenuItem("Remove Road...", IconFactory.IconType.CLEAR, e -> showRemoveRoadDialog());
        JMenuItem refreshItem = createMenuItem("Reload Graph", IconFactory.IconType.REFRESH, e -> reloadGraph());
        graphMenu.add(addLocationItem);
        graphMenu.add(removeLocationItem);
        graphMenu.addSeparator();
        graphMenu.add(addRoadItem);
        graphMenu.add(removeRoadItem);
        graphMenu.addSeparator();
        graphMenu.add(refreshItem);

        JMenu viewMenu = new JMenu("View");
        UITheme.styleMenu(viewMenu);
        viewMenu.add(createMenuItem("Zoom In", IconFactory.IconType.ZOOM_IN, e -> zoomActiveMap(true)));
        viewMenu.add(createMenuItem("Zoom Out", IconFactory.IconType.ZOOM_OUT, e -> zoomActiveMap(false)));
        viewMenu.add(createMenuItem("Reset Map View", IconFactory.IconType.ZOOM_RESET, e -> resetActiveMapZoom()));
        viewMenu.addSeparator();
        viewMenu.add(createMenuItem("Light Map", IconFactory.IconType.MAP, e -> setDarkMode(false)));
        viewMenu.add(createMenuItem("Dark Map", IconFactory.IconType.MAP, e -> setDarkMode(true)));
        viewMenu.add(createMenuItem("Settings...", IconFactory.IconType.SETTINGS, e -> showSettingsDialog()));

        JMenu helpMenu = new JMenu("Help");
        UITheme.styleMenu(helpMenu);
        JMenuItem aboutItem = new JMenuItem("About Navigator", IconFactory.getIcon(IconFactory.IconType.INFO, 15, UITheme.LABEL_COLOR));
        UITheme.styleMenuItem(aboutItem);
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(graphMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private JMenuItem createMenuItem(String text, IconFactory.IconType iconType, java.awt.event.ActionListener listener) {
        JMenuItem item = new JMenuItem(text, IconFactory.getIcon(iconType, 15, UITheme.LABEL_COLOR));
        UITheme.styleMenuItem(item);
        item.addActionListener(listener);
        return item;
    }

    private void setupLayout() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.WINDOW_BACKGROUND);

        rootPanel.add(buildMainToolBar(), BorderLayout.NORTH);
        rootPanel.add(buildSidebar(), BorderLayout.WEST);

        JPanel centerColumn = new JPanel(new BorderLayout());
        centerColumn.setBackground(UITheme.WINDOW_BACKGROUND);
        centerColumn.add(buildWorkspaceHeader(), BorderLayout.NORTH);

        workspaceContainer.setOpaque(false);
        workspaceContainer.add(buildPlannerWorkspace(), "PLANNER_VIEW");
        workspaceContainer.add(buildFullMapWorkspace(), "FULL_MAP_VIEW");
        centerColumn.add(workspaceContainer, BorderLayout.CENTER);

        rootPanel.add(centerColumn, BorderLayout.CENTER);
        rootPanel.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private JPanel buildMainToolBar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        toolbar.setBackground(UITheme.WINDOW_BACKGROUND);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));
        addToolBarButton(toolbar, "New", IconFactory.IconType.NEW_CITY, e -> createNewCity());
        addToolBarButton(toolbar, "Load", IconFactory.IconType.LOAD, e -> loadGraphFromFile());
        addToolBarButton(toolbar, "Save", IconFactory.IconType.SAVE, e -> saveGraphToFile(false));
        toolbar.add(Box.createHorizontalStrut(8));
        addToolBarButton(toolbar, "Add Location", IconFactory.IconType.NEW_CITY, e -> showAddLocationDialog());
        addToolBarButton(toolbar, "Delete Location", IconFactory.IconType.CLEAR, e -> showRemoveLocationDialog());
        addToolBarButton(toolbar, "Add Road", IconFactory.IconType.MAP, e -> showAddRoadDialog());
        addToolBarButton(toolbar, "Delete Road", IconFactory.IconType.CLEAR, e -> showRemoveRoadDialog());
        toolbar.add(Box.createHorizontalStrut(8));
        addToolBarButton(toolbar, "Settings", IconFactory.IconType.SETTINGS, e -> showSettingsDialog());
        addToolBarButton(toolbar, "About", IconFactory.IconType.INFO, e -> showAboutDialog());
        return toolbar;
    }

    private void addToolBarButton(JPanel toolbar, String text, IconFactory.IconType iconType,
            java.awt.event.ActionListener listener) {
        JButton button = new JButton(text, IconFactory.getIcon(iconType, 15, UITheme.HEADING_COLOR));
        UITheme.styleSecondaryToolBarButton(button);
        button.addActionListener(listener);
        toolbar.add(button);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.SIDEBAR_BACKGROUND);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 14, 18, 14));

        JLabel brandTag = new JLabel("SMART MOBILITY");
        brandTag.setAlignmentX(LEFT_ALIGNMENT);
        brandTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        brandTag.setForeground(UITheme.SUCCESS_COLOR);

        JLabel brandTitle = new JLabel("City Navigator");
        brandTitle.setAlignmentX(LEFT_ALIGNMENT);
        brandTitle.setFont(UITheme.APPLICATION_TITLE_FONT);
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSubtitle = new JLabel("Operations dashboard");
        brandSubtitle.setAlignmentX(LEFT_ALIGNMENT);
        brandSubtitle.setFont(UITheme.LABEL_FONT);
        brandSubtitle.setForeground(UITheme.SIDEBAR_TEXT_MUTED);

        sidebar.add(brandTag);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(brandTitle);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(brandSubtitle);
        sidebar.add(Box.createVerticalStrut(36));

        JLabel navLabel = new JLabel("WORKSPACE");
        navLabel.setAlignmentX(LEFT_ALIGNMENT);
        navLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        navLabel.setForeground(UITheme.SIDEBAR_TEXT_MUTED);
        sidebar.add(navLabel);
        sidebar.add(Box.createVerticalStrut(10));

        ButtonGroup navGroup = new ButtonGroup();

        navPlannerBtn = new JToggleButton("Route Planner", IconFactory.getIcon(IconFactory.IconType.SEARCH_ROUTE, 16, UITheme.SIDEBAR_TEXT_ACTIVE));
        navPlannerBtn.setAlignmentX(LEFT_ALIGNMENT);
        UITheme.styleSidebarNavigationButton(navPlannerBtn, true);
        navPlannerBtn.addActionListener(e -> switchWorkspace("PLANNER_VIEW", navPlannerBtn));

        navMapBtn = new JToggleButton("City Map", IconFactory.getIcon(IconFactory.IconType.MAP, 16, UITheme.SIDEBAR_TEXT_MUTED));
        navMapBtn.setAlignmentX(LEFT_ALIGNMENT);
        UITheme.styleSidebarNavigationButton(navMapBtn, false);
        navMapBtn.addActionListener(e -> switchWorkspace("FULL_MAP_VIEW", navMapBtn));

        navGroup.add(navPlannerBtn);
        navGroup.add(navMapBtn);

        sidebar.add(navPlannerBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navMapBtn);

        sidebar.add(Box.createVerticalGlue());

        JPanel divider = new JPanel();
        divider.setAlignmentX(LEFT_ALIGNMENT);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setBackground(UITheme.SIDEBAR_HOVER);
        sidebar.add(divider);
        sidebar.add(Box.createVerticalStrut(14));

        JLabel engineStatus = new JLabel("● GRAPH ENGINE ONLINE");
        engineStatus.setAlignmentX(LEFT_ALIGNMENT);
        engineStatus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        engineStatus.setForeground(UITheme.SUCCESS_COLOR);

        JLabel sessionStatus = new JLabel("Local authenticated session");
        sessionStatus.setAlignmentX(LEFT_ALIGNMENT);
        sessionStatus.setFont(UITheme.LABEL_FONT);
        sessionStatus.setForeground(UITheme.SIDEBAR_TEXT_MUTED);

        sidebar.add(engineStatus);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(sessionStatus);

        return sidebar;
    }

    private JPanel buildWorkspaceHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(UITheme.WINDOW_BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 14, 24));

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setBackground(UITheme.WINDOW_BACKGROUND);

        workspaceTitle = new JLabel("Route Planner");
        workspaceTitle.setAlignmentX(LEFT_ALIGNMENT);
        workspaceTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        workspaceTitle.setForeground(UITheme.HEADING_COLOR);

        workspaceSubtitle = new JLabel("Plan deterministic shortest routes with the city graph.");
        workspaceSubtitle.setAlignmentX(LEFT_ALIGNMENT);
        workspaceSubtitle.setFont(UITheme.BODY_FONT);
        workspaceSubtitle.setForeground(UITheme.SECONDARY_TEXT);

        titleGroup.add(workspaceTitle);
        titleGroup.add(Box.createVerticalStrut(4));
        titleGroup.add(workspaceSubtitle);

        graphStatusChip = new JLabel("GRAPH READY", SwingConstants.CENTER);
        graphStatusChip.setFont(new Font("Segoe UI", Font.BOLD, 11));
        styleStatusChip(graphStatusChip, UITheme.SUCCESS_COLOR, new Color(236, 253, 245), new Color(167, 243, 208));

        header.add(titleGroup, BorderLayout.WEST);
        header.add(graphStatusChip, BorderLayout.EAST);
        return header;
    }

    private void styleStatusChip(JLabel chip, Color textColor, Color background, Color borderColor) {
        chip.setForeground(textColor);
        chip.setOpaque(true);
        chip.setBackground(background);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
    }

    private JPanel buildPlannerWorkspace() {
        JPanel container = new JPanel(new BorderLayout(12, 12));
        container.setBackground(UITheme.WINDOW_BACKGROUND);
        container.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        JPanel leftDrawer = new JPanel();
        leftDrawer.setLayout(new BoxLayout(leftDrawer, BoxLayout.Y_AXIS));
        leftDrawer.setBackground(UITheme.WINDOW_BACKGROUND);

        routePanel.setAlignmentX(LEFT_ALIGNMENT);
        resultPanel.setAlignmentX(LEFT_ALIGNMENT);

        leftDrawer.add(routePanel);
        leftDrawer.add(Box.createVerticalStrut(12));
        leftDrawer.add(resultPanel);

        JScrollPane leftScroll = new JScrollPane(leftDrawer);
        leftScroll.setBorder(null);
        leftScroll.getViewport().setBackground(UITheme.WINDOW_BACKGROUND);
        leftScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftScroll.getVerticalScrollBar().setUnitIncrement(12);
        leftScroll.setPreferredSize(new Dimension(380, 0));

        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.setBackground(UITheme.WINDOW_BACKGROUND);
        mapContainer.add(buildMapToolbar(plannerMapPanel), BorderLayout.NORTH);
        mapContainer.add(plannerMapPanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, mapContainer);
        splitPane.setDividerLocation(390);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        container.add(splitPane, BorderLayout.CENTER);
        return container;
    }

    private JPanel buildFullMapWorkspace() {
        JPanel container = new JPanel(new BorderLayout(12, 12));
        container.setBackground(UITheme.WINDOW_BACKGROUND);
        container.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        container.add(buildMapToolbar(fullMapPanel), BorderLayout.NORTH);
        container.add(fullMapPanel, BorderLayout.CENTER);
        return container;
    }

    private JPanel buildMapToolbar(MapPanel targetMapPanel) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        toolbar.setBackground(UITheme.WINDOW_BACKGROUND);

        JButton zoomIn = new JButton("Zoom In", IconFactory.getIcon(IconFactory.IconType.ZOOM_IN, 15, UITheme.HEADING_COLOR));
        UITheme.styleSecondaryToolBarButton(zoomIn);
        zoomIn.addActionListener(e -> {
            targetMapPanel.zoomIn();
            updateStatus("Zoomed to " + targetMapPanel.getZoomPercentage() + "%.", UITheme.SECONDARY_TEXT);
        });

        JButton zoomOut = new JButton("Zoom Out", IconFactory.getIcon(IconFactory.IconType.ZOOM_OUT, 15, UITheme.HEADING_COLOR));
        UITheme.styleSecondaryToolBarButton(zoomOut);
        zoomOut.addActionListener(e -> {
            targetMapPanel.zoomOut();
            updateStatus("Zoomed to " + targetMapPanel.getZoomPercentage() + "%.", UITheme.SECONDARY_TEXT);
        });

        JButton zoomReset = new JButton("Reset View", IconFactory.getIcon(IconFactory.IconType.ZOOM_RESET, 15, UITheme.HEADING_COLOR));
        UITheme.styleSecondaryToolBarButton(zoomReset);
        zoomReset.addActionListener(e -> {
            targetMapPanel.resetZoom();
            updateStatus("View reset to 100%.", UITheme.SECONDARY_TEXT);
        });

        toolbar.add(zoomIn);
        toolbar.add(zoomOut);
        toolbar.add(zoomReset);
        return toolbar;
    }

    private JPanel buildStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        UITheme.styleStatusPanel(statusPanel);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));

        statusPanel.add(statusLabel, BorderLayout.WEST);
        return statusPanel;
    }

    private void switchWorkspace(String cardName, JToggleButton activeBtn) {
        workspaceCardLayout.show(workspaceContainer, cardName);
        UITheme.styleSidebarNavigationButton(navPlannerBtn, activeBtn == navPlannerBtn);
        UITheme.styleSidebarNavigationButton(navMapBtn, activeBtn == navMapBtn);

        if (activeBtn == navMapBtn) {
            workspaceTitle.setText("City Map");
            workspaceSubtitle.setText("Explore the loaded graph and inspect highlighted routes.");
        } else {
            workspaceTitle.setText("Route Planner");
            workspaceSubtitle.setText("Plan deterministic shortest routes with the city graph.");
        }
    }

    public final void loadGraphData() {
        try {
            CityGraph graph = routeService.getGraph();
            resultPanel.clear();
            plannerMapPanel.setGraph(graph);
            fullMapPanel.setGraph(graph);
            routePanel.loadLocations();
            int locationCount = graph.getAllLocations().size();
            updateStatus("Graph loaded with " + locationCount + " locations.", UITheme.SUCCESS_COLOR);

            if (locationCount == 0) {
                styleStatusChip(graphStatusChip, UITheme.WARNING_COLOR, new Color(255, 251, 235), new Color(253, 230, 138));
                graphStatusChip.setText("NO DATA");
            } else {
                styleStatusChip(graphStatusChip, UITheme.SUCCESS_COLOR, new Color(236, 253, 245), new Color(167, 243, 208));
                graphStatusChip.setText("GRAPH READY");
            }
        } catch (Exception ex) {
            AppLogger.error("Error loading graph data", ex);
            updateStatus("Failed to load graph data.", UITheme.ERROR_COLOR);
            styleStatusChip(graphStatusChip, UITheme.ERROR_COLOR, new Color(254, 242, 242), new Color(254, 202, 202));
            graphStatusChip.setText("LOAD ERROR");
        }
    }

    private void reloadGraph() {
        if (currentGraphFile == null) {
            loadGraphData();
            return;
        }
        if (!confirmDiscardChanges("reload the current graph file")) {
            return;
        }
        try {
            routeService.loadGraphFromFile(currentGraphFile);
            graphDirty = false;
            loadGraphData();
            updateStatus("Graph reloaded from " + currentGraphFile.getName() + ".", UITheme.SUCCESS_COLOR);
        } catch (Exception exception) {
            showActionError("Reload Graph", exception);
        }
    }

    public void calculateAndDisplayRoute(String sourceId, String destinationId) {
        try {
            PathResult result = routeService.findRoute(sourceId, destinationId);
            resultPanel.displayResult(result);

            if (result.isPathFound()) {
                plannerMapPanel.highlightRoute(result.getRoute());
                fullMapPanel.highlightRoute(result.getRoute());
                updateStatus("Shortest route calculated successfully.", UITheme.SUCCESS_COLOR);
            } else {
                plannerMapPanel.clearHighlight();
                fullMapPanel.clearHighlight();
                updateStatus("No valid route found between selected points.", UITheme.WARNING_COLOR);
            }
        } catch (Exception ex) {
            AppLogger.error("Error during path calculation", ex);
            updateStatus("Error calculating route.", UITheme.ERROR_COLOR);
        }
    }

    public void clearActiveRoute() {
        plannerMapPanel.clearHighlight();
        fullMapPanel.clearHighlight();
        resultPanel.clear();
        updateStatus("Active route cleared.", UITheme.SECONDARY_TEXT);
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setText(message);
    }

    private void showAboutDialog() {
        new AboutDialog(this).setVisible(true);
    }

    private void createNewCity() {
        if (!confirmDiscardChanges("replace the current graph with the bundled default city")) {
            return;
        }
        try {
            routeService.createNewCity();
            currentGraphFile = null;
            graphDirty = false;
            loadGraphData();
            updateStatus("New empty city created.", UITheme.SUCCESS_COLOR);
        } catch (Exception exception) {
            showActionError("New City", exception);
        }
    }

    private void loadGraphFromFile() {
        if (!confirmDiscardChanges("load a different graph")) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load Graph");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            routeService.loadGraphFromFile(chooser.getSelectedFile());
            currentGraphFile = chooser.getSelectedFile();
            graphDirty = false;
            loadGraphData();
            updateStatus("Loaded " + currentGraphFile.getName() + ".", UITheme.SUCCESS_COLOR);
        } catch (Exception exception) {
            showActionError("Load Graph", exception);
        }
    }

    private void saveGraphToFile(boolean saveAs) {
        File file = saveAs ? null : currentGraphFile;
        if (file == null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Graph");
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            file = chooser.getSelectedFile();
            if (!file.getName().contains(".")) {
                file = new File(file.getParentFile(), file.getName() + ".dat");
            }
        }
        try {
            routeService.saveGraphToFile(file);
            currentGraphFile = file;
            graphDirty = false;
            updateStatus("Graph saved to " + file.getName() + ".", UITheme.SUCCESS_COLOR);
        } catch (Exception exception) {
            showActionError("Save Graph", exception);
        }
    }

    private void showAddLocationDialog() {
        showAddLocationDialog(Double.NaN, Double.NaN);
    }

    private void showAddLocationDialog(double initialX, double initialY) {
        JPanel form = DialogHelper.createInputPanel();
        JTextField idField = DialogHelper.addLabeledField(form, "ID");
        JTextField nameField = DialogHelper.addLabeledField(form, "Name");
        JTextField xField = DialogHelper.addLabeledField(form, "X coordinate");
        JTextField yField = DialogHelper.addLabeledField(form, "Y coordinate");
        if (Double.isFinite(initialX)) xField.setText(String.format("%.1f", initialX));
        if (Double.isFinite(initialY)) yField.setText(String.format("%.1f", initialY));
        if (JOptionPane.showConfirmDialog(this, form, "Add Location", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            double x = Double.parseDouble(xField.getText().trim());
            double y = Double.parseDouble(yField.getText().trim());
            routeService.addLocation(idField.getText(), nameField.getText(), x, y);
            graphWasModified("Location added.");
        } catch (Exception exception) {
            showActionError("Add Location", exception);
        }
    }

    private void showRemoveLocationDialog() {
        JComboBox<String> locations = new JComboBox<>(locationIds());
        if (locations.getItemCount() == 0) {
            showNoGraphMessage();
            return;
        }
        JPanel form = new JPanel(new BorderLayout(8, 0));
        form.add(new JLabel("Location to remove:"), BorderLayout.WEST);
        form.add(locations, BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, form, "Remove Location", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        String id = (String) locations.getSelectedItem();
        if (JOptionPane.showConfirmDialog(this, "Remove " + id + " and all connected roads?", "Confirm removal",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION
                && routeService.removeLocation(id)) {
            graphWasModified("Location removed.");
        }
    }

    private void showAddRoadDialog() {
        showAddRoadDialog(null, null);
    }

    private void showAddRoadDialog(String initialSourceId, String initialDestinationId) {
        String[] ids = locationIds();
        if (ids.length < 2) {
            showNoGraphMessage();
            return;
        }
        JPanel form = DialogHelper.createInputPanel();
        JComboBox<String> source = new JComboBox<>(ids);
        JComboBox<String> destination = new JComboBox<>(ids);
        if (initialSourceId != null) source.setSelectedItem(initialSourceId);
        if (initialDestinationId != null) destination.setSelectedItem(initialDestinationId);
        JTextField weight = DialogHelper.addLabeledField(form, "Distance (km)");
        form.add(new JLabel("From")); form.add(source);
        form.add(new JLabel("To")); form.add(destination);
        // Keep the distance field last and visually consistent with other inputs.
        UITheme.styleComboBox(source);
        UITheme.styleComboBox(destination);
        if (JOptionPane.showConfirmDialog(this, form, "Add Two-way Road", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            routeService.addRoad((String) source.getSelectedItem(), (String) destination.getSelectedItem(),
                    Double.parseDouble(weight.getText().trim()));
            graphWasModified("Road added.");
        } catch (Exception exception) {
            showActionError("Add Road", exception);
        }
    }

    private void showRemoveRoadDialog() {
        CityGraph graph = routeService.getGraph();
        if (graph.roadCount() == 0) {
            JOptionPane.showMessageDialog(this, "The current graph has no roads.", "Remove Road", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] roads = graph.getAllEdges().stream()
                .map(edge -> edge.getSourceId() + " ↔ " + edge.getDestinationId() + " (" + edge.getWeight() + " km)")
                .toArray(String[]::new);
        JComboBox<String> choices = new JComboBox<>(roads);
        if (JOptionPane.showConfirmDialog(this, choices, "Remove Road", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        int selected = choices.getSelectedIndex();
        var edge = graph.getAllEdges().stream().skip(selected).findFirst().orElse(null);
        if (edge != null && routeService.removeRoad(edge.getSourceId(), edge.getDestinationId())) {
            graphWasModified("Road removed.");
        }
    }

    private String[] locationIds() {
        return routeService.getAllLocations().stream().map(location -> location.getId()).toArray(String[]::new);
    }

    public void graphWasModified(String message) {
        graphDirty = true;
        loadGraphData();
        updateStatus(message, UITheme.SUCCESS_COLOR);
    }

    private void showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(this);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            plannerMapPanel.repaint();
            fullMapPanel.repaint();
            updateStatus("Settings applied.", UITheme.SUCCESS_COLOR);
        }
    }

    private void setDarkMode(boolean enabled) {
        com.smartcity.navigator.utils.AppSettings.getInstance().setDarkMode(enabled);
        plannerMapPanel.repaint();
        fullMapPanel.repaint();
        updateStatus(enabled ? "Dark map mode enabled." : "Light map mode enabled.", UITheme.SUCCESS_COLOR);
    }

    private MapPanel activeMap() {
        return navMapBtn != null && navMapBtn.isSelected() ? fullMapPanel : plannerMapPanel;
    }

    private void zoomActiveMap(boolean in) {
        MapPanel map = activeMap();
        if (in) map.zoomIn(); else map.zoomOut();
        updateStatus("Map zoom: " + map.getZoomPercentage() + "%.", UITheme.SECONDARY_TEXT);
    }

    private void resetActiveMapZoom() {
        activeMap().resetZoom();
        updateStatus("Map view reset to 100%.", UITheme.SECONDARY_TEXT);
    }

    private boolean confirmDiscardChanges(String action) {
        if (!graphDirty) return true;
        return JOptionPane.showConfirmDialog(this, "Unsaved graph changes will be lost. Continue to " + action + "?",
                "Unsaved changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void showNoGraphMessage() {
        JOptionPane.showMessageDialog(this, "Add at least two locations before editing roads.", "Graph editor",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showActionError(String action, Exception exception) {
        AppLogger.error(action + " failed", exception);
        JOptionPane.showMessageDialog(this, exception.getMessage(), action, JOptionPane.ERROR_MESSAGE);
    }
}
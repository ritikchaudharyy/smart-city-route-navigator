package com.smartcity.navigator.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
import javax.swing.JToggleButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.smartcity.navigator.graph.CityGraph;
import com.smartcity.navigator.graph.GraphLoadException;
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
    private MapPanel mapPanel;
    private JPanel sharedMapWorkspace;
    private JPanel plannerMapHost;
    private JPanel fullMapHost;

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

    /** Preserves the original application entry-point contract. */
    public MainFrame(RouteService routeService) {
        this(routeService, new GeminiService());
    }

    public MainFrame(RouteService routeService, GeminiService geminiService) {
        this.routeService = routeService;
        this.geminiService = geminiService;

        setTitle("Smart City Route Navigator - Enterprise Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                confirmExit();
            }
        });
        setMinimumSize(new Dimension(1280, 800));
        setSize(1440, 900);
        setLocationRelativeTo(null);

        initComponents();
        setupMenuBar();
        setupLayout();
        loadGraphData();
    }

    private void initComponents() {
        routePanel = new RoutePanel(this, routeService, geminiService);
        resultPanel = new ResultPanel();
        mapPanel = new MapPanel();
        sharedMapWorkspace = new JPanel(new BorderLayout());
        sharedMapWorkspace.setBackground(UITheme.WINDOW_BACKGROUND);
        sharedMapWorkspace.add(buildMapToolbar(), BorderLayout.NORTH);
        sharedMapWorkspace.add(mapPanel, BorderLayout.CENTER);

        statusLabel = new JLabel("Ready");
        UITheme.styleStatusLabel(statusLabel);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        UITheme.styleMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        UITheme.styleMenu(fileMenu);
        JMenuItem newCityItem = new JMenuItem("New City", IconFactory.getIcon(IconFactory.IconType.NEW_CITY, 15, UITheme.LABEL_COLOR));
        JMenuItem loadGraphItem = new JMenuItem("Load Graph", IconFactory.getIcon(IconFactory.IconType.LOAD, 15, UITheme.LABEL_COLOR));
        JMenuItem saveGraphItem = new JMenuItem("Save Graph", IconFactory.getIcon(IconFactory.IconType.SAVE, 15, UITheme.LABEL_COLOR));
        JMenuItem exitItem = new JMenuItem("Exit", IconFactory.getIcon(IconFactory.IconType.EXIT, 15, UITheme.LABEL_COLOR));
        UITheme.styleMenuItem(newCityItem);
        UITheme.styleMenuItem(loadGraphItem);
        UITheme.styleMenuItem(saveGraphItem);
        UITheme.styleMenuItem(exitItem);
        newCityItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke("control N"));
        loadGraphItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke("control O"));
        saveGraphItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke("control S"));
        exitItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke("control Q"));
        newCityItem.addActionListener(event -> onNewCity());
        loadGraphItem.addActionListener(event -> onLoadGraph());
        saveGraphItem.addActionListener(event -> onSaveGraph());
        exitItem.addActionListener(event -> confirmExit());
        fileMenu.add(newCityItem);
        fileMenu.add(loadGraphItem);
        fileMenu.add(saveGraphItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu graphMenu = new JMenu("Graph");
        UITheme.styleMenu(graphMenu);
        JMenuItem refreshItem = new JMenuItem("Reload Graph", IconFactory.getIcon(IconFactory.IconType.REFRESH, 15, UITheme.LABEL_COLOR));
        JMenuItem addLocationItem = new JMenuItem("Add Location...");
        JMenuItem removeLocationItem = new JMenuItem("Remove Location...");
        JMenuItem addRoadItem = new JMenuItem("Add Road...");
        JMenuItem removeRoadItem = new JMenuItem("Remove Road...");
        UITheme.styleMenuItem(refreshItem);
        UITheme.styleMenuItem(addLocationItem);
        UITheme.styleMenuItem(removeLocationItem);
        UITheme.styleMenuItem(addRoadItem);
        UITheme.styleMenuItem(removeRoadItem);
        refreshItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke("F5"));
        addLocationItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke("control shift N"));
        removeLocationItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke("control shift R"));
        addRoadItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke("control shift A"));
        removeRoadItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke("control shift D"));
        refreshItem.addActionListener(event -> onRefresh());
        addLocationItem.addActionListener(event -> onAddLocation());
        removeLocationItem.addActionListener(event -> onRemoveLocation());
        addRoadItem.addActionListener(event -> onAddRoad());
        removeRoadItem.addActionListener(event -> onRemoveRoad());
        graphMenu.add(refreshItem);
        graphMenu.addSeparator();
        graphMenu.add(addLocationItem);
        graphMenu.add(removeLocationItem);
        graphMenu.addSeparator();
        graphMenu.add(addRoadItem);
        graphMenu.add(removeRoadItem);

        JMenu viewMenu = new JMenu("View");
        UITheme.styleMenu(viewMenu);
        JMenuItem zoomInItem = new JMenuItem("Zoom In", IconFactory.getIcon(IconFactory.IconType.ZOOM_IN, 15, UITheme.LABEL_COLOR));
        JMenuItem zoomOutItem = new JMenuItem("Zoom Out", IconFactory.getIcon(IconFactory.IconType.ZOOM_OUT, 15, UITheme.LABEL_COLOR));
        JMenuItem resetViewItem = new JMenuItem("Reset View", IconFactory.getIcon(IconFactory.IconType.ZOOM_RESET, 15, UITheme.LABEL_COLOR));
        JMenuItem settingsItem = new JMenuItem("Settings...", IconFactory.getIcon(IconFactory.IconType.SETTINGS, 15, UITheme.LABEL_COLOR));
        UITheme.styleMenuItem(zoomInItem);
        UITheme.styleMenuItem(zoomOutItem);
        UITheme.styleMenuItem(resetViewItem);
        UITheme.styleMenuItem(settingsItem);
        zoomInItem.addActionListener(event -> zoomMapIn());
        zoomOutItem.addActionListener(event -> zoomMapOut());
        resetViewItem.addActionListener(event -> resetMapView());
        settingsItem.addActionListener(event -> onSettings());
        viewMenu.add(zoomInItem);
        viewMenu.add(zoomOutItem);
        viewMenu.add(resetViewItem);
        viewMenu.addSeparator();
        viewMenu.add(settingsItem);

        JMenu helpMenu = new JMenu("Help");
        UITheme.styleMenu(helpMenu);
        JMenuItem aboutItem = new JMenuItem("About Navigator", IconFactory.getIcon(IconFactory.IconType.INFO, 15, UITheme.LABEL_COLOR));
        UITheme.styleMenuItem(aboutItem);
        aboutItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke("F1"));
        aboutItem.addActionListener(event -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(graphMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void setupLayout() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.WINDOW_BACKGROUND);

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
        navPlannerBtn.setSelected(true);

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

        plannerMapHost = new JPanel(new BorderLayout());
        plannerMapHost.setBackground(UITheme.WINDOW_BACKGROUND);
        plannerMapHost.add(sharedMapWorkspace, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, plannerMapHost);
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
        fullMapHost = new JPanel(new BorderLayout());
        fullMapHost.setBackground(UITheme.WINDOW_BACKGROUND);
        container.add(fullMapHost, BorderLayout.CENTER);
        return container;
    }

    private JPanel buildMapToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        toolbar.setBackground(UITheme.WINDOW_BACKGROUND);

        JButton zoomIn = new JButton("Zoom In", IconFactory.getIcon(IconFactory.IconType.ZOOM_IN, 15, UITheme.HEADING_COLOR));
        UITheme.styleSecondaryToolBarButton(zoomIn);
        zoomIn.addActionListener(e -> {
            mapPanel.zoomIn();
            updateStatus("Zoomed to " + mapPanel.getZoomPercentage() + "%.", UITheme.SECONDARY_TEXT);
        });

        JButton zoomOut = new JButton("Zoom Out", IconFactory.getIcon(IconFactory.IconType.ZOOM_OUT, 15, UITheme.HEADING_COLOR));
        UITheme.styleSecondaryToolBarButton(zoomOut);
        zoomOut.addActionListener(e -> {
            mapPanel.zoomOut();
            updateStatus("Zoomed to " + mapPanel.getZoomPercentage() + "%.", UITheme.SECONDARY_TEXT);
        });

        JButton zoomReset = new JButton("Reset View", IconFactory.getIcon(IconFactory.IconType.ZOOM_RESET, 15, UITheme.HEADING_COLOR));
        UITheme.styleSecondaryToolBarButton(zoomReset);
        zoomReset.addActionListener(e -> {
            mapPanel.resetZoom();
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
        moveMapWorkspace(activeBtn == navMapBtn ? fullMapHost : plannerMapHost);
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

    /**
     * A Swing component can belong to only one parent. Reparent the shared
     * graph canvas as the user changes workspace so the same graph, zoom, and
     * highlighted route remain visible in both views.
     */
    private void moveMapWorkspace(JPanel destinationHost) {
        if (destinationHost == null || sharedMapWorkspace.getParent() == destinationHost) {
            return;
        }

        Container currentHost = sharedMapWorkspace.getParent();
        if (currentHost != null) {
            currentHost.remove(sharedMapWorkspace);
            currentHost.revalidate();
            currentHost.repaint();
        }
        destinationHost.add(sharedMapWorkspace, BorderLayout.CENTER);
        destinationHost.revalidate();
        destinationHost.repaint();
    }

    public void loadGraphData() {
        try {
            CityGraph graph = routeService.getGraph();
            mapPanel.setGraph(graph);
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

    public void calculateAndDisplayRoute(String sourceId, String destinationId) {
        try {
            PathResult result = routeService.findRoute(sourceId, destinationId);
            resultPanel.displayResult(result);

            if (result.isPathFound()) {
                mapPanel.highlightRoute(result.getRoute());
                updateStatus("Shortest route calculated successfully.", UITheme.SUCCESS_COLOR);
            } else {
                mapPanel.clearHighlight();
                updateStatus("No valid route found between selected points.", UITheme.WARNING_COLOR);
            }
        } catch (Exception ex) {
            AppLogger.error("Error during path calculation", ex);
            updateStatus("Error calculating route.", UITheme.ERROR_COLOR);
        }
    }

    public void clearActiveRoute() {
        mapPanel.clearHighlight();
        resultPanel.clear();
        updateStatus("Active route cleared.", UITheme.SECONDARY_TEXT);
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setText(message);
    }

    private void zoomMapIn() {
        mapPanel.zoomIn();
        updateStatus("Zoomed to " + mapPanel.getZoomPercentage() + "%.", UITheme.SECONDARY_TEXT);
    }

    private void zoomMapOut() {
        mapPanel.zoomOut();
        updateStatus("Zoomed to " + mapPanel.getZoomPercentage() + "%.", UITheme.SECONDARY_TEXT);
    }

    private void resetMapView() {
        mapPanel.resetZoom();
        updateStatus("View reset to 100%.", UITheme.SECONDARY_TEXT);
    }

    private void onRefresh() {
        loadGraphData();
        clearActiveRoute();
    }

    private void onAddLocation() {
        JPanel panel = DialogHelper.createInputPanel();
        JTextField idField = DialogHelper.addLabeledField(panel, "Location ID:");
        JTextField nameField = DialogHelper.addLabeledField(panel, "Name:");
        JTextField xField = DialogHelper.addLabeledField(panel, "X coordinate:");
        JTextField yField = DialogHelper.addLabeledField(panel, "Y coordinate:");
        xField.setText("0");
        yField.setText("0");

        if (showDialog(panel, "Add Location") != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            routeService.addLocation(idField.getText(), nameField.getText(),
                    parseCoordinate(xField.getText(), "X coordinate"),
                    parseCoordinate(yField.getText(), "Y coordinate"));
            onRefresh();
            resultPanel.appendLog("Added location " + idField.getText().trim());
            updateStatus("Location added.", UITheme.SUCCESS_COLOR);
        } catch (IllegalArgumentException exception) {
            showError("Add Location", exception.getMessage());
        }
    }

    private void onRemoveLocation() {
        if (routeService.getAllLocations().isEmpty()) {
            showError("Remove Location", "No locations are available to remove.");
            return;
        }

        JComboBox<String> locationSelector = createLocationSelector();
        if (showDialog(locationSelector, "Remove Location") != JOptionPane.OK_OPTION) {
            return;
        }
        String selectedId = (String) locationSelector.getSelectedItem();
        if (selectedId == null) {
            return;
        }
        if (!routeService.removeLocation(selectedId)) {
            showError("Remove Location", "The selected location could not be removed.");
            return;
        }

        onRefresh();
        resultPanel.appendLog("Removed location " + selectedId);
        updateStatus("Location removed.", UITheme.SUCCESS_COLOR);
    }

    private void onAddRoad() {
        if (routeService.getAllLocations().size() < 2) {
            showError("Add Road", "At least two locations are required to add a road.");
            return;
        }

        JPanel panel = DialogHelper.createInputPanel();
        JComboBox<String> sourceSelector = createLocationSelector();
        JComboBox<String> destinationSelector = createLocationSelector();
        panel.add(new JLabel("Source:"));
        panel.add(sourceSelector);
        panel.add(new JLabel("Destination:"));
        panel.add(destinationSelector);
        JTextField weightField = DialogHelper.addLabeledField(panel, "Distance (km):");
        weightField.setText("1");

        if (showDialog(panel, "Add Road") != JOptionPane.OK_OPTION) {
            return;
        }
        String sourceId = (String) sourceSelector.getSelectedItem();
        String destinationId = (String) destinationSelector.getSelectedItem();
        try {
            routeService.addRoad(sourceId, destinationId, parsePositiveDistance(weightField.getText()));
            onRefresh();
            resultPanel.appendLog("Added road " + sourceId + " to " + destinationId);
            updateStatus("Road added.", UITheme.SUCCESS_COLOR);
        } catch (IllegalArgumentException exception) {
            showError("Add Road", exception.getMessage());
        }
    }

    private void onRemoveRoad() {
        if (routeService.getAllLocations().size() < 2) {
            showError("Remove Road", "At least two locations are required to remove a road.");
            return;
        }

        JPanel panel = DialogHelper.createInputPanel();
        JComboBox<String> sourceSelector = createLocationSelector();
        JComboBox<String> destinationSelector = createLocationSelector();
        panel.add(new JLabel("Source:"));
        panel.add(sourceSelector);
        panel.add(new JLabel("Destination:"));
        panel.add(destinationSelector);

        if (showDialog(panel, "Remove Road") != JOptionPane.OK_OPTION) {
            return;
        }
        String sourceId = (String) sourceSelector.getSelectedItem();
        String destinationId = (String) destinationSelector.getSelectedItem();
        if (!routeService.removeRoad(sourceId, destinationId)) {
            showError("Remove Road", "The selected road could not be removed.");
            return;
        }

        onRefresh();
        resultPanel.appendLog("Removed road " + sourceId + " to " + destinationId);
        updateStatus("Road removed.", UITheme.SUCCESS_COLOR);
    }

    private JComboBox<String> createLocationSelector() {
        String[] locationIds = routeService.getAllLocations().stream()
                .map(Location::getId)
                .sorted(Comparator.naturalOrder())
                .toArray(String[]::new);
        JComboBox<String> selector = new JComboBox<>(locationIds);
        UITheme.styleComboBox(selector);
        return selector;
    }

    private int showDialog(JComponent content, String title) {
        return JOptionPane.showConfirmDialog(this, content, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    private void onNewCity() {
        int confirmation = JOptionPane.showConfirmDialog(this,
                "This resets the graph to the bundled default city and discards unsaved changes. Continue?",
                "New City", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            routeService.resetToDefaultCity();
            onRefresh();
            resultPanel.appendLog("Loaded the default city.");
        } catch (GraphLoadException exception) {
            showError("New City", exception.getMessage());
        } catch (RuntimeException exception) {
            AppLogger.error("Unable to reset the default city graph", exception);
            showError("New City", "The default city could not be loaded. Please try again.");
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
            resultPanel.appendLog("Loaded graph from " + chooser.getSelectedFile().getName());
        } catch (GraphLoadException exception) {
            showError("Load Graph", exception.getMessage());
        } catch (RuntimeException exception) {
            AppLogger.error("Unable to load graph from the selected file", exception);
            showError("Load Graph", "The selected graph could not be loaded. Check the file and try again.");
        }
    }

    private void onSaveGraph() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("City Graph Data (*.dat)", "dat"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        File targetFile = selectedFile.getName().toLowerCase().endsWith(".dat")
                ? selectedFile
                : new File(selectedFile.getParentFile(), selectedFile.getName() + ".dat");
        if (targetFile.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(this,
                    "A file named '" + targetFile.getName() + "' already exists. Replace it?",
                    "Replace existing graph file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (overwrite != JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {
            routeService.saveGraphToFile(targetFile);
            resultPanel.appendLog("Saved graph to " + targetFile.getName());
            updateStatus("Graph saved to " + targetFile.getName() + ".", UITheme.SUCCESS_COLOR);
        } catch (GraphLoadException exception) {
            showError("Save Graph", exception.getMessage());
        } catch (RuntimeException exception) {
            AppLogger.error("Unable to save graph to the selected file", exception);
            showError("Save Graph", "The graph could not be saved to that location. Please try again.");
        }
    }

    private void onSettings() {
        SettingsDialog dialog = new SettingsDialog(this);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            mapPanel.repaint();
            resultPanel.appendLog("Settings updated.");
            updateStatus("Settings updated.", UITheme.SUCCESS_COLOR);
        }
    }

    private void confirmExit() {
        int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmation == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void showError(String title, String message) {
        String safeMessage = message == null || message.isBlank()
                ? "The requested action could not be completed. Please try again."
                : message;
        JOptionPane.showMessageDialog(this, safeMessage, title, JOptionPane.ERROR_MESSAGE);
    }

    private double parseCoordinate(String value, String label) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            double coordinate = Double.parseDouble(normalized);
            if (!Double.isFinite(coordinate)) {
                throw new IllegalArgumentException(label + " must be a finite number.");
            }
            return coordinate;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a valid number.");
        }
    }

    private double parsePositiveDistance(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Road distance is required.");
        }
        try {
            double distance = Double.parseDouble(normalized);
            if (!Double.isFinite(distance) || distance <= 0) {
                throw new IllegalArgumentException("Road distance must be a positive number.");
            }
            return distance;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Road distance must be a valid number.");
        }
    }

    private void showAboutDialog() {
        new AboutDialog(this).setVisible(true);
    }
}

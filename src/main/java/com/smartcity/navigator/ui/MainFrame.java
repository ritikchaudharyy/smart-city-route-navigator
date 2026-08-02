package com.smartcity.navigator.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
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
import javax.swing.SwingConstants;

import com.smartcity.navigator.graph.CityGraph;
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

    public MainFrame(RouteService routeService, GeminiService geminiService) {
        this.routeService = routeService;
        this.geminiService = geminiService;

        setTitle("Smart City Route Navigator - Enterprise Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 800));
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

        statusLabel = new JLabel("Ready");
        UITheme.styleStatusLabel(statusLabel);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        UITheme.styleMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        UITheme.styleMenu(fileMenu);
        JMenuItem exitItem = new JMenuItem("Exit", IconFactory.getIcon(IconFactory.IconType.EXIT, 15, UITheme.LABEL_COLOR));
        UITheme.styleMenuItem(exitItem);
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenu graphMenu = new JMenu("Graph");
        UITheme.styleMenu(graphMenu);
        JMenuItem refreshItem = new JMenuItem("Reload Graph", IconFactory.getIcon(IconFactory.IconType.REFRESH, 15, UITheme.LABEL_COLOR));
        UITheme.styleMenuItem(refreshItem);
        refreshItem.addActionListener(e -> loadGraphData());
        graphMenu.add(refreshItem);

        JMenu helpMenu = new JMenu("Help");
        UITheme.styleMenu(helpMenu);
        JMenuItem aboutItem = new JMenuItem("About Navigator", IconFactory.getIcon(IconFactory.IconType.INFO, 15, UITheme.LABEL_COLOR));
        UITheme.styleMenuItem(aboutItem);
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(graphMenu);
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
        mapContainer.add(buildMapToolbar(), BorderLayout.NORTH);
        mapContainer.add(mapPanel, BorderLayout.CENTER);

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
        container.add(buildMapToolbar(), BorderLayout.NORTH);
        container.add(mapPanel, BorderLayout.CENTER);
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

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Smart City Route Navigator\nVersion 2.0 Enterprise\n\nBuilt with Java 17, Swing, Dijkstra Engine & Gemini AI Integration.",
                "About Application",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
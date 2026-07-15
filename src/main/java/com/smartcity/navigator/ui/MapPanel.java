package com.smartcity.navigator.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.smartcity.navigator.graph.CityGraph;
import com.smartcity.navigator.model.Edge;
import com.smartcity.navigator.model.Location;
import com.smartcity.navigator.utils.AppSettings;

/**
 * Draws the city road network — locations as circles, roads as lines
 * labeled with their distance — and highlights the most recently
 * computed shortest route: the source in green, the destination in red,
 * intermediate stops and roads along the path in orange.
 * <p>
 * Supports basic zoom (View &gt; Zoom In / Zoom Out / Reset) via a simple
 * scale transform, satisfying the "MapPanel" and zoom menu requirements
 * without needing an external mapping library.
 *
 * @author Smart City Route Navigator Team
 */
public class MapPanel extends JPanel {

    private static final int NODE_RADIUS = 10;
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 2.5;
    private static final double ZOOM_STEP = 0.15;

    private CityGraph graph;
    private List<Location> highlightedRoute = List.of();
    private double zoom = 1.0;

    public MapPanel() {
        setPreferredSize(new Dimension(600, 420));
        setBackground(UITheme.WINDOW_BACKGROUND);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    }

    /** Sets (or replaces) the graph to render, clearing any existing highlight. */
    public void setGraph(CityGraph graph) {
        this.graph = graph;
        this.highlightedRoute = List.of();
        repaint();
    }

    /** Highlights the given route (ordered source-to-destination) on the map. */
    public void highlightRoute(List<Location> route) {
        this.highlightedRoute = route;
        repaint();
    }

    /** Clears any highlighted route, returning the map to its plain state. */
    public void clearHighlight() {
        this.highlightedRoute = List.of();
        repaint();
    }

    public void zoomIn() {
        setZoom(zoom + ZOOM_STEP);
    }

    public void zoomOut() {
        setZoom(zoom - ZOOM_STEP);
    }

    public void resetZoom() {
        setZoom(1.0);
    }

    private void setZoom(double newZoom) {
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        boolean dark = AppSettings.getInstance().isDarkMode();
        setBackground(dark ? new Color(20, 24, 32) : UITheme.WINDOW_BACKGROUND);

        if (graph == null || graph.isEmpty()) {
            paintEmptyState(g, dark);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.scale(zoom, zoom);

        Color edgeColor = dark ? new Color(95, 100, 115) : new Color(160, 170, 185);
        Color nodeColor = dark ? new Color(70, 130, 220) : new Color(60, 115, 195);
        Color textColor = dark ? Color.WHITE : UITheme.HEADING_COLOR;
        Color routeColor = new Color(235, 130, 40);
        Color sourceColor = new Color(75, 190, 110);
        Color destinationColor = new Color(220, 70, 70);

        Set<String> routeIds = new HashSet<>();
        for (Location location : highlightedRoute) {
            routeIds.add(location.getId());
        }

        // Roads are drawn first so location circles render on top of them.
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (Edge edge : graph.getAllEdges()) {
            Location a = graph.getLocation(edge.getSourceId()).orElse(null);
            Location b = graph.getLocation(edge.getDestinationId()).orElse(null);
            if (a == null || b == null) {
                continue;
            }

            boolean onRoute = isEdgeOnRoute(edge);
            g2.setColor(onRoute ? routeColor : edgeColor);
            g2.setStroke(new BasicStroke(onRoute ? 3f : 1.5f));
            g2.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());

            int midX = (int) ((a.getX() + b.getX()) / 2);
            int midY = (int) ((a.getY() + b.getY()) / 2);
            g2.setColor(textColor);
            g2.drawString(formatWeight(edge.getWeight()), midX + 4, midY - 4);
        }

        for (Location location : graph.getAllLocations()) {
            int x = (int) location.getX();
            int y = (int) location.getY();
            boolean onRoute = routeIds.contains(location.getId());

            Color fill = nodeColor;
            if (onRoute) {
                if (isRouteEndpoint(location, 0)) {
                    fill = sourceColor;
                } else if (isRouteEndpoint(location, highlightedRoute.size() - 1)) {
                    fill = destinationColor;
                } else {
                    fill = routeColor;
                }
            }

            g2.setColor(fill);
            g2.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            g2.setColor(dark ? new Color(200, 200, 210) : new Color(70, 70, 90));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

            g2.setColor(textColor);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(location.getName(), x - NODE_RADIUS, y - NODE_RADIUS - 6);
        }

        g2.dispose();
    }

    private boolean isRouteEndpoint(Location location, int endpointIndex) {
        return !highlightedRoute.isEmpty()
                && endpointIndex >= 0
                && endpointIndex < highlightedRoute.size()
                && highlightedRoute.get(endpointIndex).getId().equals(location.getId());
    }

    private boolean isEdgeOnRoute(Edge edge) {
        if (highlightedRoute.size() < 2) {
            return false;
        }
        for (int i = 0; i < highlightedRoute.size() - 1; i++) {
            String a = highlightedRoute.get(i).getId();
            String b = highlightedRoute.get(i + 1).getId();
            if (edge.connects(a, b)) {
                return true;
            }
        }
        return false;
    }

    private String formatWeight(double weight) {
        return weight == Math.floor(weight) ? String.valueOf((int) weight) : String.valueOf(weight);
    }

    private void paintEmptyState(Graphics g, boolean dark) {
        g.setColor(dark ? new Color(130, 140, 155) : UITheme.LABEL_COLOR);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("No city graph loaded.", 20, 30);
    }
}

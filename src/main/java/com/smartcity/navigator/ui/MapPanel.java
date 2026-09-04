package com.smartcity.navigator.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.smartcity.navigator.graph.CityGraph;
import com.smartcity.navigator.model.Edge;
import com.smartcity.navigator.model.Location;
import com.smartcity.navigator.utils.AppSettings;

/**
 * Double-buffered graph canvas. Automatically fits the graph viewport and applies
 * zoom without altering underlying graph data or routing logic.
 */
public final class MapPanel extends JPanel {

    private static final int NODE_RADIUS = 11;
    private static final int MAP_PADDING = 72;
    private static final double MIN_ZOOM = 0.65;
    private static final double MAX_ZOOM = 2.5;
    private static final double ZOOM_STEP = 0.15;

    private CityGraph graph;
    private List<Location> highlightedRoute = List.of();
    private double zoom = 1.0;
    private GraphInteractionHandler interactionHandler;

    public MapPanel() {
        setPreferredSize(new Dimension(600, 420));
        setBackground(UITheme.WINDOW_BACKGROUND);
        setDoubleBuffered(true);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent event) {
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mousePressed(MouseEvent event) {
                showContextMenu(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                showContextMenu(event);
            }
        });
    }

    /** Installs the owner callback used by the map's context menus. */
    public void setInteractionHandler(GraphInteractionHandler handler) {
        interactionHandler = handler;
    }

    /** Sets (or replaces) the graph to render, clearing any existing route highlight. */
    public void setGraph(CityGraph graph) {
        this.graph = graph;
        clearHighlight();
    }

    /** Highlights an ordered local-Dijkstra route. */
    public void highlightRoute(List<Location> route) {
        highlightedRoute = route == null ? List.of() : List.copyOf(route);
        repaint();
    }

    /** Clears only the visual route overlay. */
    public void clearHighlight() {
        highlightedRoute = List.of();
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

    /** Current zoom level as a whole-number percentage, e.g. 100 for 1.0x. */
    public int getZoomPercentage() {
        return (int) Math.round(zoom * 100);
    }

    private void setZoom(double requestedZoom) {
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, requestedZoom));
        repaint();
    }

    private void showContextMenu(MouseEvent event) {
        if (!event.isPopupTrigger() || interactionHandler == null) {
            return;
        }
        Hit hit = hitTest(event.getX(), event.getY());
        JPopupMenu menu = new JPopupMenu();
        if (hit.location != null) {
            addMenuItem(menu, "Add Location Here", () -> interactionHandler.addLocation(hit.mapPoint.getX(), hit.mapPoint.getY()));
            addMenuItem(menu, "Add Road from " + hit.location.getName(),
                    () -> interactionHandler.addRoad(hit.location.getId(), null));
            menu.addSeparator();
            addMenuItem(menu, "Delete Location", () -> interactionHandler.deleteLocation(hit.location));
        } else if (hit.edge != null) {
            addMenuItem(menu, "Add Road", () -> interactionHandler.addRoad(null, null));
            menu.addSeparator();
            addMenuItem(menu, "Delete Road", () -> interactionHandler.deleteRoad(hit.edge));
        } else {
            addMenuItem(menu, "Add Location Here", () -> interactionHandler.addLocation(hit.mapPoint.getX(), hit.mapPoint.getY()));
            if (graph != null && graph.locationCount() >= 2) {
                addMenuItem(menu, "Add Road", () -> interactionHandler.addRoad(null, null));
            }
        }
        menu.show(this, event.getX(), event.getY());
    }

    private void addMenuItem(JPopupMenu menu, String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(event -> action.run());
        menu.add(item);
    }

    /** Finds the nearest rendered node or road under a screen coordinate. */
    Hit hitTest(int screenX, int screenY) {
        Point2D point = toMapPoint(screenX, screenY);
        if (point == null) {
            return new Hit(null, null, point == null ? new Point2D.Double(screenX, screenY) : point);
        }
        for (Location location : graph.getAllLocations()) {
            double distance = point.distance(location.getX(), location.getY());
            if (distance <= NODE_RADIUS + 5.0 / currentScale()) {
                return new Hit(location, null, point);
            }
        }
        Edge nearestEdge = null;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (Edge edge : graph.getAllEdges()) {
            Location source = graph.getLocation(edge.getSourceId()).orElse(null);
            Location destination = graph.getLocation(edge.getDestinationId()).orElse(null);
            if (source == null || destination == null) continue;
            double distance = pointToSegmentDistance(point.getX(), point.getY(), source.getX(), source.getY(),
                    destination.getX(), destination.getY());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestEdge = edge;
            }
        }
        return nearestDistance <= 7.0 / currentScale()
                ? new Hit(null, nearestEdge, point) : new Hit(null, null, point);
    }

    private Point2D toMapPoint(int screenX, int screenY) {
        if (graph == null || graph.isEmpty()) return null;
        GraphBounds bounds = GraphBounds.from(graph.getAllLocations());
        double scale = calculateScale(bounds);
        return new Point2D.Double((screenX - getWidth() / 2.0 + bounds.centerX() * scale) / scale,
                (screenY - getHeight() / 2.0 + bounds.centerY() * scale) / scale);
    }

    private double currentScale() {
        return graph == null || graph.isEmpty() ? 1.0 : calculateScale(GraphBounds.from(graph.getAllLocations()));
    }

    private static double pointToSegmentDistance(double px, double py, double ax, double ay, double bx, double by) {
        double dx = bx - ax;
        double dy = by - ay;
        if (dx == 0 && dy == 0) return Point2D.distance(px, py, ax, ay);
        double t = Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)));
        return Point2D.distance(px, py, ax + t * dx, ay + t * dy);
    }

    public interface GraphInteractionHandler {
        void addLocation(double x, double y);
        void deleteLocation(Location location);
        void addRoad(String sourceId, String destinationId);
        void deleteRoad(Edge edge);
    }

    static final class Hit {
        final Location location;
        final Edge edge;
        final Point2D mapPoint;

        Hit(Location location, Edge edge, Point2D mapPoint) {
            this.location = location;
            this.edge = edge;
            this.mapPoint = mapPoint;
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        boolean darkMode = AppSettings.getInstance().isDarkMode();
        Graphics2D canvas = (Graphics2D) graphics.create();
        try {
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            canvas.setColor(darkMode ? new Color(20, 24, 32) : UITheme.WINDOW_BACKGROUND);
            canvas.fillRect(0, 0, getWidth(), getHeight());

            if (graph == null || graph.isEmpty()) {
                paintEmptyState(canvas, darkMode);
                return;
            }

            GraphBounds bounds = GraphBounds.from(graph.getAllLocations());
            double scale = calculateScale(bounds);
            double translateX = getWidth() / 2.0 - bounds.centerX() * scale;
            double translateY = getHeight() / 2.0 - bounds.centerY() * scale;

            Graphics2D mapGraphics = (Graphics2D) canvas.create();
            try {
                mapGraphics.translate(translateX, translateY);
                mapGraphics.scale(scale, scale);
                paintGraph(mapGraphics, darkMode);
            } finally {
                mapGraphics.dispose();
            }
            paintCanvasLegend(canvas, darkMode);
        } finally {
            canvas.dispose();
        }
    }

    private double calculateScale(GraphBounds bounds) {
        double availableWidth = Math.max(1, getWidth() - MAP_PADDING * 2.0);
        double availableHeight = Math.max(1, getHeight() - MAP_PADDING * 2.0);
        double fittedScale = Math.min(availableWidth / bounds.width(), availableHeight / bounds.height());
        return Math.max(0.1, fittedScale) * zoom;
    }

    private void paintGraph(Graphics2D graphics, boolean darkMode) {
        Color edgeColor = darkMode ? new Color(100, 116, 139) : new Color(165, 176, 191);
        Color nodeColor = darkMode ? new Color(96, 165, 250) : UITheme.ACCENT_PRIMARY;
        Color textColor = darkMode ? new Color(226, 232, 240) : UITheme.HEADING_COLOR;
        Color routeColor = new Color(249, 115, 22);

        Set<String> routeIds = new HashSet<>();
        Set<String> routeEdges = new HashSet<>();
        for (Location location : highlightedRoute) {
            routeIds.add(location.getId());
        }
        for (int index = 0; index < highlightedRoute.size() - 1; index++) {
            routeEdges.add(edgeKey(highlightedRoute.get(index).getId(), highlightedRoute.get(index + 1).getId()));
        }

        graphics.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (Edge edge : graph.getAllEdges()) {
            Location source = graph.getLocation(edge.getSourceId()).orElse(null);
            Location destination = graph.getLocation(edge.getDestinationId()).orElse(null);
            if (source == null || destination == null) {
                continue;
            }

            boolean onRoute = routeEdges.contains(edgeKey(edge.getSourceId(), edge.getDestinationId()));
            graphics.setColor(onRoute ? routeColor : edgeColor);
            graphics.setStroke(new BasicStroke(onRoute ? 3.4f : 1.6f,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.drawLine((int) source.getX(), (int) source.getY(),
                    (int) destination.getX(), (int) destination.getY());

            int midpointX = (int) Math.round((source.getX() + destination.getX()) / 2.0);
            int midpointY = (int) Math.round((source.getY() + destination.getY()) / 2.0);
            drawEdgeLabel(graphics, formatWeight(edge.getWeight()), midpointX + 5, midpointY - 5, darkMode, onRoute, routeColor);
        }

        for (Location location : graph.getAllLocations()) {
            drawLocation(graphics, location, routeIds.contains(location.getId()), nodeColor, textColor, routeColor, darkMode);
        }
    }

    private void drawEdgeLabel(Graphics2D graphics, String text, int x, int y, boolean darkMode,
            boolean onRoute, Color routeColor) {
        Font originalFont = graphics.getFont();
        graphics.setFont(new Font("Segoe UI", Font.BOLD, 10));
        int width = graphics.getFontMetrics().stringWidth(text);

        graphics.setColor(darkMode ? new Color(30, 41, 59, 235) : new Color(255, 255, 255, 240));
        graphics.fillRoundRect(x - 4, y - 11, width + 8, 15, 8, 8);
        if (onRoute) {
            graphics.setColor(routeColor);
            graphics.setStroke(new BasicStroke(1.2f));
            graphics.drawRoundRect(x - 4, y - 11, width + 8, 15, 8, 8);
        }
        graphics.setColor(onRoute ? routeColor : (darkMode ? new Color(226, 232, 240) : UITheme.LABEL_COLOR));
        graphics.drawString(text, x, y);
        graphics.setFont(originalFont);
    }

    private void drawLocation(Graphics2D graphics, Location location, boolean onRoute,
            Color nodeColor, Color textColor, Color routeColor, boolean darkMode) {
        int x = (int) Math.round(location.getX());
        int y = (int) Math.round(location.getY());
        Color fillColor = nodeColor;
        String badge = null;
        if (onRoute) {
            if (isRouteEndpoint(location, 0)) {
                fillColor = UITheme.SUCCESS_COLOR;
                badge = "A";
            } else if (isRouteEndpoint(location, highlightedRoute.size() - 1)) {
                fillColor = UITheme.ERROR_COLOR;
                badge = "B";
            } else {
                fillColor = routeColor;
            }
        }

        // Soft drop shadow.
        graphics.setColor(new Color(15, 23, 42, 45));
        graphics.fill(new Ellipse2D.Float(x - NODE_RADIUS + 1.5f, y - NODE_RADIUS + 3f, NODE_RADIUS * 2, NODE_RADIUS * 2));

        // Route halo.
        if (onRoute) {
            graphics.setColor(new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 55));
            graphics.fillOval(x - NODE_RADIUS - 6, y - NODE_RADIUS - 6, (NODE_RADIUS + 6) * 2, (NODE_RADIUS + 6) * 2);
        }

        graphics.setColor(fillColor);
        graphics.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        graphics.setColor(darkMode ? new Color(226, 232, 240) : Color.WHITE);
        graphics.setStroke(new BasicStroke(2.2f));
        graphics.drawOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

        if (badge != null) {
            graphics.setFont(new Font("Segoe UI", Font.BOLD, 10));
            graphics.setColor(Color.WHITE);
            int badgeWidth = graphics.getFontMetrics().stringWidth(badge);
            graphics.drawString(badge, x - badgeWidth / 2f, y + 4);
        }

        graphics.setColor(textColor);
        graphics.setFont(new Font("Segoe UI", Font.BOLD, 12));
        graphics.drawString(location.getName(), x - NODE_RADIUS, y - NODE_RADIUS - 9);
    }

    private void paintCanvasLegend(Graphics2D graphics, boolean darkMode) {
        Color cardColor = darkMode ? new Color(30, 41, 59, 235) : new Color(255, 255, 255, 240);
        Color textColor = darkMode ? new Color(226, 232, 240) : UITheme.LABEL_COLOR;

        String zoomLabel = "Zoom " + getZoomPercentage() + "%";
        graphics.setFont(UITheme.LABEL_FONT);
        int zoomWidth = graphics.getFontMetrics().stringWidth(zoomLabel) + 22;
        int zx = getWidth() - zoomWidth - 16;
        int zy = 16;
        graphics.setColor(cardColor);
        graphics.fillRoundRect(zx, zy, zoomWidth, 28, 12, 12);
        graphics.setColor(textColor);
        graphics.drawString(zoomLabel, zx + 11, zy + 18);

        int locationCount = graph == null ? 0 : graph.getAllLocations().size();
        String countLabel = locationCount + (locationCount == 1 ? " location" : " locations");
        int countWidth = graphics.getFontMetrics().stringWidth(countLabel) + 22;
        graphics.setColor(cardColor);
        graphics.fillRoundRect(16, 16, countWidth, 28, 12, 12);
        graphics.setColor(textColor);
        graphics.drawString(countLabel, 27, 34);

        if (!highlightedRoute.isEmpty()) {
            int ly = 52;
            graphics.setColor(cardColor);
            graphics.fillRoundRect(16, ly, 176, 28, 12, 12);
            graphics.setColor(new Color(249, 115, 22));
            graphics.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.drawLine(28, ly + 14, 46, ly + 14);
            graphics.setColor(textColor);
            graphics.drawString("Shortest route", 56, ly + 18);
        }
    }

    private boolean isRouteEndpoint(Location location, int endpointIndex) {
        return endpointIndex >= 0
                && endpointIndex < highlightedRoute.size()
                && highlightedRoute.get(endpointIndex).getId().equals(location.getId());
    }

    private static String edgeKey(String firstId, String secondId) {
        return firstId.compareTo(secondId) <= 0
                ? firstId + '\u0000' + secondId
                : secondId + '\u0000' + firstId;
    }

    private static String formatWeight(double weight) {
        return weight == Math.rint(weight) ? String.valueOf((int) weight) : String.format("%.1f", weight);
    }

    private void paintEmptyState(Graphics2D graphics, boolean darkMode) {
        Color iconColor = darkMode ? new Color(71, 85, 105) : new Color(203, 213, 225);
        Color textColor = darkMode ? new Color(148, 163, 184) : UITheme.SECONDARY_TEXT;

        int cx = getWidth() / 2;
        int cy = getHeight() / 2 - 20;

        graphics.setColor(iconColor);
        graphics.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.drawOval(cx - 34, cy - 34, 68, 68);
        graphics.fillOval(cx - 6, cy - 6, 12, 12);
        graphics.drawLine(cx + 18, cy + 18, cx + 40, cy + 40);

        graphics.setFont(new Font("Segoe UI", Font.BOLD, 15));
        String title = "No city graph loaded";
        int titleWidth = graphics.getFontMetrics().stringWidth(title);
        graphics.setColor(darkMode ? new Color(226, 232, 240) : UITheme.HEADING_COLOR);
        graphics.drawString(title, cx - titleWidth / 2f, cy + 66);

        graphics.setFont(UITheme.BODY_FONT);
        String subtitle = "Load a graph or start a new city from the File menu.";
        int subtitleWidth = graphics.getFontMetrics().stringWidth(subtitle);
        graphics.setColor(textColor);
        graphics.drawString(subtitle, cx - subtitleWidth / 2f, cy + 88);
    }

    private static final class GraphBounds {
        private final double minimumX;
        private final double maximumX;
        private final double minimumY;
        private final double maximumY;

        private GraphBounds(double minimumX, double maximumX, double minimumY, double maximumY) {
            this.minimumX = minimumX;
            this.maximumX = maximumX;
            this.minimumY = minimumY;
            this.maximumY = maximumY;
        }

        private static GraphBounds from(Iterable<Location> locations) {
            double minimumX = Double.POSITIVE_INFINITY;
            double maximumX = Double.NEGATIVE_INFINITY;
            double minimumY = Double.POSITIVE_INFINITY;
            double maximumY = Double.NEGATIVE_INFINITY;
            for (Location location : locations) {
                minimumX = Math.min(minimumX, location.getX());
                maximumX = Math.max(maximumX, location.getX());
                minimumY = Math.min(minimumY, location.getY());
                maximumY = Math.max(maximumY, location.getY());
            }
            return new GraphBounds(minimumX, maximumX, minimumY, maximumY);
        }

        private double width() {
            return Math.max(1, maximumX - minimumX + NODE_RADIUS * 4.0);
        }

        private double height() {
            return Math.max(1, maximumY - minimumY + NODE_RADIUS * 4.0);
        }

        private double centerX() {
            return (minimumX + maximumX) / 2.0;
        }

        private double centerY() {
            return (minimumY + maximumY) / 2.0;
        }
    }
}
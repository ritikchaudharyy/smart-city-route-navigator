package com.smartcity.navigator.ui;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Draws small, self-contained vector icons for toolbar buttons and menu
 * items, so the "Icons should be supported" requirement is met without
 * depending on external image files or network access. Each icon is
 * rendered once into a {@link BufferedImage} using basic {@link Graphics2D}
 * primitives and wrapped as an {@link ImageIcon}.
 *
 * @author Smart City Route Navigator Team
 */
public final class IconFactory {

    private IconFactory() {
        // Utility class: not instantiable.
    }

    /** The set of icons used across the menu bar and toolbar. */
    public enum IconType {
        FIND, CLEAR, REFRESH, EXIT, NEW_CITY, LOAD, SAVE, ZOOM_IN, ZOOM_OUT, RESET_VIEW, INFO, SETTINGS
    }

    /**
     * Creates an icon of the given type, size, and color.
     *
     * @param type  which icon to draw
     * @param size  width and height in pixels (icons are square)
     * @param color stroke/fill color
     * @return a ready-to-use Swing {@link Icon}
     */
    public static Icon create(IconType type, int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.setStroke(new BasicStroke(Math.max(1.5f, size / 10f)));

        int pad = Math.max(2, size / 6);
        switch (type) {
            case FIND -> drawFind(g, size, pad);
            case CLEAR -> drawClear(g, size, pad);
            case REFRESH -> drawRefresh(g, size, pad);
            case EXIT -> drawExit(g, size, pad);
            case NEW_CITY -> drawNewCity(g, size, pad);
            case LOAD -> drawLoad(g, size, pad);
            case SAVE -> drawSave(g, size, pad);
            case ZOOM_IN -> drawZoom(g, size, pad, true);
            case ZOOM_OUT -> drawZoom(g, size, pad, false);
            case RESET_VIEW -> drawResetView(g, size, pad);
            case INFO -> drawInfo(g, size, pad);
            case SETTINGS -> drawSettings(g, size, pad);
        }

        g.dispose();
        return new ImageIcon(image);
    }

    private static void drawFind(Graphics2D g, int size, int pad) {
        int r = size / 2;
        g.drawOval(pad, pad, r, r);
        g.drawLine(pad + r - 2, pad + r - 2, size - pad, size - pad);
    }

    private static void drawClear(Graphics2D g, int size, int pad) {
        g.drawLine(pad, pad, size - pad, size - pad);
        g.drawLine(size - pad, pad, pad, size - pad);
    }

    private static void drawRefresh(Graphics2D g, int size, int pad) {
        g.drawArc(pad, pad, size - 2 * pad, size - 2 * pad, 30, 300);
        int ax = size - pad;
        int ay = size / 2;
        g.drawLine(ax, ay, ax - 6, ay - 4);
        g.drawLine(ax, ay, ax - 2, ay + 6);
    }

    private static void drawExit(Graphics2D g, int size, int pad) {
        g.drawRect(pad, pad, size / 2, size - 2 * pad);
        g.drawLine(size / 2, size / 2, size - pad, size / 2);
        g.drawLine(size - pad, size / 2, size - pad - 6, size / 2 - 6);
        g.drawLine(size - pad, size / 2, size - pad - 6, size / 2 + 6);
    }

    private static void drawNewCity(Graphics2D g, int size, int pad) {
        g.drawRect(pad, pad, size - 2 * pad, size - 2 * pad);
        g.drawLine(size / 2, pad + 4, size / 2, size - pad - 4);
        g.drawLine(pad + 4, size / 2, size - pad - 4, size / 2);
    }

    private static void drawLoad(Graphics2D g, int size, int pad) {
        g.drawRect(pad, size / 3, size - 2 * pad, size / 2);
        g.drawLine(pad, size / 3, pad + size / 4, pad);
        g.drawLine(pad + size / 4, pad, pad + size / 2, size / 3);
    }

    private static void drawSave(Graphics2D g, int size, int pad) {
        g.drawRect(pad, pad, size - 2 * pad, size - 2 * pad);
        g.fillRect(pad + (size - 2 * pad) / 4, pad, (size - 2 * pad) / 2, size / 4);
    }

    private static void drawZoom(Graphics2D g, int size, int pad, boolean zoomIn) {
        int r = size / 2;
        g.drawOval(pad, pad, r, r);
        g.drawLine(pad + r - 2, pad + r - 2, size - pad, size - pad);
        int cx = pad + r / 2;
        int cy = pad + r / 2;
        g.drawLine(cx - 4, cy, cx + 4, cy);
        if (zoomIn) {
            g.drawLine(cx, cy - 4, cx, cy + 4);
        }
    }

    private static void drawResetView(Graphics2D g, int size, int pad) {
        g.drawArc(pad, pad, size - 2 * pad, size - 2 * pad, 0, 360);
        g.drawLine(size / 2 - 3, size / 2, size / 2 + 3, size / 2);
        g.drawLine(size / 2, size / 2 - 3, size / 2, size / 2 + 3);
    }

    private static void drawInfo(Graphics2D g, int size, int pad) {
        int r = size - 2 * pad;
        g.drawOval(pad, pad, r, r);
        g.drawLine(size / 2, size / 2 - 2, size / 2, size - pad - 4);
        g.fillOval(size / 2 - 2, pad + 4, 4, 4);
    }

    private static void drawSettings(Graphics2D g, int size, int pad) {
        int r = size / 2 - pad;
        g.drawOval(size / 2 - r / 2, size / 2 - r / 2, r, r);
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            int x1 = (int) (size / 2 + Math.cos(angle) * r / 2);
            int y1 = (int) (size / 2 + Math.sin(angle) * r / 2);
            int x2 = (int) (size / 2 + Math.cos(angle) * (r / 2 + 4));
            int y2 = (int) (size / 2 + Math.sin(angle) * (r / 2 + 4));
            g.drawLine(x1, y1, x2, y2);
        }
    }
}

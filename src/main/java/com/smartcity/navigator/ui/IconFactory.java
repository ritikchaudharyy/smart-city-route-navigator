package com.smartcity.navigator.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Draws crisp, resolution-independent vector icons for toolbar buttons,
 * navigation panels, and menu items without depending on external raster
 * image files. Each icon is rendered on demand into a {@link BufferedImage}
 * using high-quality anti-aliased {@link Graphics2D} primitives and returned
 * as a Swing {@link Icon}.
 * <p>
 * This revision refines every glyph's proportions and stroke weight for
 * better legibility at small sizes, while keeping the {@link IconType} enum
 * and public methods unchanged so calling code needs no changes.
 *
 * @author Smart City Route Navigator Team
 */
public final class IconFactory {

    private IconFactory() {
        // Utility class: not instantiable.
    }

    /** The complete set of vector icons used across the application UI. */
    public enum IconType {
        AI_SPARKLE,
        SEARCH_ROUTE,
        FIND,
        SWAP,
        CLEAR,
        REFRESH,
        EXIT,
        NEW_CITY,
        LOAD,
        SAVE,
        ZOOM_IN,
        ZOOM_OUT,
        ZOOM_RESET,
        RESET_VIEW,
        INFO,
        SETTINGS,
        MAP,
        METRICS
    }

    /**
     * Alias for {@link #create(IconType, int, Color)} to maintain compatibility across panels.
     */
    public static Icon getIcon(IconType type, int size, Color color) {
        return create(type, size, color);
    }

    /**
     * Creates an icon of the specified type, dimensions, and color.
     *
     * @param type  which icon to draw
     * @param size  width and height in pixels
     * @param color stroke/fill color
     * @return a ready-to-use Swing {@link Icon}
     */
    public static Icon create(IconType type, int size, Color color) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        Color drawColor = color != null ? color : UITheme.HEADING_COLOR;
        g.setColor(drawColor);
        g.setStroke(new BasicStroke(Math.max(1.6f, size / 9.5f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int pad = Math.max(2, Math.round(size / 5.5f));

        switch (type) {
            case AI_SPARKLE -> drawSparkle(g, size);
            case SEARCH_ROUTE, FIND -> drawFind(g, size, pad);
            case SWAP -> drawSwap(g, size);
            case CLEAR -> drawClear(g, size, pad);
            case REFRESH -> drawRefresh(g, size, pad);
            case EXIT -> drawExit(g, size, pad);
            case NEW_CITY -> drawNewCity(g, size, pad);
            case LOAD -> drawLoad(g, size, pad);
            case SAVE -> drawSave(g, size, pad, drawColor);
            case ZOOM_IN -> drawZoom(g, size, pad, 1);
            case ZOOM_OUT -> drawZoom(g, size, pad, -1);
            case ZOOM_RESET, RESET_VIEW -> drawResetView(g, size, pad);
            case INFO -> drawInfo(g, size, pad, drawColor);
            case SETTINGS -> drawSettings(g, size);
            case MAP -> drawMap(g, size, pad, drawColor);
            case METRICS -> drawMetrics(g, size);
        }

        g.dispose();
        return new ImageIcon(image);
    }

    // ------------------------------------------------------------------
    // Individual glyph renderers
    // ------------------------------------------------------------------

    private static void drawSparkle(Graphics2D g, int size) {
        float cx = size * 0.5f;
        float cy = size * 0.48f;

        // Primary four-point star (rounded diamond silhouette).
        Path2D star = new Path2D.Float();
        float rOut = size * 0.40f;
        float rIn = size * 0.12f;
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0 - Math.PI / 2.0;
            float r = (i % 2 == 0) ? rOut : rIn;
            float px = cx + (float) (Math.cos(angle) * r);
            float py = cy + (float) (Math.sin(angle) * r);
            if (i == 0) {
                star.moveTo(px, py);
            } else {
                star.lineTo(px, py);
            }
        }
        star.closePath();
        g.fill(star);

        // Small companion sparkle, offset toward the corner.
        float scx = size * 0.80f;
        float scy = size * 0.78f;
        float sOut = size * 0.13f;
        float sIn = size * 0.04f;
        Path2D small = new Path2D.Float();
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0 - Math.PI / 2.0;
            float r = (i % 2 == 0) ? sOut : sIn;
            float px = scx + (float) (Math.cos(angle) * r);
            float py = scy + (float) (Math.sin(angle) * r);
            if (i == 0) {
                small.moveTo(px, py);
            } else {
                small.lineTo(px, py);
            }
        }
        small.closePath();
        g.fill(small);
    }

    private static void drawFind(Graphics2D g, int size, int pad) {
        float r = size * 0.40f;
        float cx = pad + r / 2f;
        float cy = pad + r / 2f;
        g.draw(new Ellipse2D.Float(cx - r / 2f, cy - r / 2f, r, r));
        float handleStartX = cx + (float) (r / 2f * 0.72f);
        float handleStartY = cy + (float) (r / 2f * 0.72f);
        g.drawLine(Math.round(handleStartX), Math.round(handleStartY), size - pad, size - pad);
    }

    private static void drawSwap(Graphics2D g, int size) {
        float s = size;
        float top = s * 0.36f;
        float bottom = s * 0.64f;

        // Top row: arrow pointing right.
        g.drawLine((int) (s * 0.16f), (int) top, (int) (s * 0.80f), (int) top);
        g.drawLine((int) (s * 0.62f), (int) (s * 0.20f), (int) (s * 0.80f), (int) top);
        g.drawLine((int) (s * 0.62f), (int) (s * 0.50f), (int) (s * 0.80f), (int) top);

        // Bottom row: arrow pointing left.
        g.drawLine((int) (s * 0.84f), (int) bottom, (int) (s * 0.20f), (int) bottom);
        g.drawLine((int) (s * 0.38f), (int) (s * 0.50f), (int) (s * 0.20f), (int) bottom);
        g.drawLine((int) (s * 0.38f), (int) (s * 0.80f), (int) (s * 0.20f), (int) bottom);
    }

    private static void drawClear(Graphics2D g, int size, int pad) {
        g.drawLine(pad, pad, size - pad, size - pad);
        g.drawLine(size - pad, pad, pad, size - pad);
    }

    private static void drawRefresh(Graphics2D g, int size, int pad) {
        int d = size - 2 * pad;
        g.drawArc(pad, pad, d, d, 35, 280);
        int ax = size - pad;
        int ay = size / 2;
        g.drawLine(ax, ay, ax - Math.max(4, size / 7), ay - Math.max(4, size / 7));
        g.drawLine(ax, ay, ax - Math.max(1, size / 20), ay + Math.max(5, size / 6));
    }

    private static void drawExit(Graphics2D g, int size, int pad) {
        RoundRectangle2D frame = new RoundRectangle2D.Float(pad, pad, size * 0.42f - pad, size - 2 * pad, 4, 4);
        g.draw(frame);
        int midY = size / 2;
        g.drawLine((int) (size * 0.42f), midY, size - pad, midY);
        g.drawLine(size - pad, midY, size - pad - Math.max(4, size / 6), midY - Math.max(4, size / 6));
        g.drawLine(size - pad, midY, size - pad - Math.max(4, size / 6), midY + Math.max(4, size / 6));
    }

    private static void drawNewCity(Graphics2D g, int size, int pad) {
        RoundRectangle2D frame = new RoundRectangle2D.Float(pad, pad, size - 2 * pad, size - 2 * pad, 5, 5);
        g.draw(frame);
        int gap = Math.max(4, size / 5);
        g.drawLine(size / 2, pad + gap, size / 2, size - pad - gap);
        g.drawLine(pad + gap, size / 2, size - pad - gap, size / 2);
    }

    private static void drawLoad(Graphics2D g, int size, int pad) {
        RoundRectangle2D tray = new RoundRectangle2D.Float(pad, size * 0.42f, size - 2 * pad, size * 0.42f, 4, 4);
        g.draw(tray);
        int arrowX = size / 2;
        g.drawLine(arrowX, pad, arrowX, (int) (size * 0.5f));
        g.drawLine(arrowX - Math.max(4, size / 6), (int) (size * 0.5f) - Math.max(4, size / 6), arrowX, (int) (size * 0.5f));
        g.drawLine(arrowX + Math.max(4, size / 6), (int) (size * 0.5f) - Math.max(4, size / 6), arrowX, (int) (size * 0.5f));
    }

    private static void drawSave(Graphics2D g, int size, int pad, Color color) {
        RoundRectangle2D body = new RoundRectangle2D.Float(pad, pad, size - 2 * pad, size - 2 * pad, 5, 5);
        g.draw(body);
        float labelWidth = (size - 2 * pad) * 0.55f;
        g.fill(new RoundRectangle2D.Float(pad + (size - 2 * pad - labelWidth) / 2f, pad, labelWidth, size * 0.28f, 2, 2));
        g.drawLine((int) (pad + size * 0.16f), (int) (size * 0.62f), (int) (size - pad - size * 0.16f), (int) (size * 0.62f));
    }

    private static void drawZoom(Graphics2D g, int size, int pad, int mode) {
        drawFind(g, size, pad);
        float r = size * 0.40f;
        int cx = Math.round(pad + r / 2f);
        int cy = Math.round(pad + r / 2f);
        int arm = Math.max(3, size / 7);
        g.drawLine(cx - arm, cy, cx + arm, cy);
        if (mode == 1) {
            g.drawLine(cx, cy - arm, cx, cy + arm);
        }
    }

    private static void drawResetView(Graphics2D g, int size, int pad) {
        int d = size - 2 * pad;
        g.draw(new Ellipse2D.Float(pad, pad, d, d));
        int arm = Math.max(3, size / 7);
        g.drawLine(size / 2 - arm, size / 2, size / 2 + arm, size / 2);
        g.drawLine(size / 2, size / 2 - arm, size / 2, size / 2 + arm);
    }

    private static void drawInfo(Graphics2D g, int size, int pad, Color color) {
        int d = size - 2 * pad;
        g.draw(new Ellipse2D.Float(pad, pad, d, d));
        g.drawLine(size / 2, size / 2 - 1, size / 2, size - pad - Math.max(3, size / 6));
        float dotSize = Math.max(3, size / 8f);
        g.fill(new Ellipse2D.Float(size / 2f - dotSize / 2f, pad + Math.max(3, size / 8f), dotSize, dotSize));
    }

    private static void drawSettings(Graphics2D g, int size) {
        float cx = size / 2f;
        float cy = size / 2f;
        float hubRadius = size * 0.16f;
        float toothInner = size * 0.24f;
        float toothOuter = size * 0.40f;
        float toothWidth = size * 0.12f;

        g.draw(new Ellipse2D.Float(cx - hubRadius, cy - hubRadius, hubRadius * 2, hubRadius * 2));

        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            double perp = angle + Math.PI / 2.0;
            float baseX = cx + (float) (Math.cos(angle) * toothInner);
            float baseY = cy + (float) (Math.sin(angle) * toothInner);
            float tipX = cx + (float) (Math.cos(angle) * toothOuter);
            float tipY = cy + (float) (Math.sin(angle) * toothOuter);
            float halfWidth = toothWidth / 2f;

            Path2D tooth = new Path2D.Float();
            tooth.moveTo(baseX + Math.cos(perp) * halfWidth, baseY + Math.sin(perp) * halfWidth);
            tooth.lineTo(tipX + Math.cos(perp) * (halfWidth * 0.6f), tipY + Math.sin(perp) * (halfWidth * 0.6f));
            tooth.lineTo(tipX - Math.cos(perp) * (halfWidth * 0.6f), tipY - Math.sin(perp) * (halfWidth * 0.6f));
            tooth.lineTo(baseX - Math.cos(perp) * halfWidth, baseY - Math.sin(perp) * halfWidth);
            tooth.closePath();
            g.fill(tooth);
        }
    }

    private static void drawMap(Graphics2D g, int size, int pad, Color color) {
        // Location pin silhouette: rounded teardrop with a hollow center.
        float cx = size * 0.5f;
        float topY = size * 0.14f;
        float bulgeRadius = size * 0.26f;
        float bulgeCenterY = topY + bulgeRadius;
        float tipY = size * 0.86f;

        Path2D pin = new Path2D.Float();
        pin.moveTo(cx, tipY);
        pin.curveTo(cx - bulgeRadius * 1.05f, bulgeCenterY + bulgeRadius * 0.35f,
                cx - bulgeRadius, bulgeCenterY, cx - bulgeRadius, bulgeCenterY - bulgeRadius * 0.15f);
        pin.curveTo(cx - bulgeRadius, topY, cx - bulgeRadius * 0.55f, topY - bulgeRadius * 0.25f,
                cx, topY - bulgeRadius * 0.25f);
        pin.curveTo(cx + bulgeRadius * 0.55f, topY - bulgeRadius * 0.25f, cx + bulgeRadius, topY,
                cx + bulgeRadius, bulgeCenterY - bulgeRadius * 0.15f);
        pin.curveTo(cx + bulgeRadius, bulgeCenterY, cx + bulgeRadius * 1.05f, bulgeCenterY + bulgeRadius * 0.35f,
                cx, tipY);
        pin.closePath();
        g.draw(pin);

        float innerRadius = bulgeRadius * 0.42f;
        g.fill(new Ellipse2D.Float(cx - innerRadius, bulgeCenterY - innerRadius, innerRadius * 2, innerRadius * 2));
    }

    private static void drawMetrics(Graphics2D g, int size) {
        float s = size;
        float baseline = s * 0.85f;
        drawBar(g, s * 0.18f, s * 0.55f, s * 0.16f, baseline);
        drawBar(g, s * 0.42f, s * 0.32f, s * 0.16f, baseline);
        drawBar(g, s * 0.66f, s * 0.12f, s * 0.16f, baseline);
    }

    private static void drawBar(Graphics2D g, float x, float top, float width, float baseline) {
        float height = baseline - top;
        g.fill(new RoundRectangle2D.Float(x, top, width, height, width * 0.4f, width * 0.4f));
    }
}
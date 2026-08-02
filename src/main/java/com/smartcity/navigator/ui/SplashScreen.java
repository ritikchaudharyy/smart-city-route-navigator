package com.smartcity.navigator.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.smartcity.navigator.utils.Constants;

/**
 * Startup splash screen shown briefly before {@link MainFrame} appears,
 * per the application workflow: Application Starts -&gt; Splash Screen -&gt;
 * Main Window.
 * <p>
 * Uses a Swing {@link Timer} rather than {@code Thread.sleep} to wait
 * out the display duration, so the Event Dispatch Thread is never
 * blocked and the splash window stays responsive to being painted.
 *
 * @author Smart City Route Navigator Team
 */
public class SplashScreen extends JWindow {

    public SplashScreen() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(UITheme.SIDEBAR_BACKGROUND);
        content.setBorder(BorderFactory.createLineBorder(UITheme.ACCENT_PRIMARY, 2));

        LogoBadge badge = new LogoBadge();
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(Constants.APP_TITLE, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UITheme.SIDEBAR_TEXT_ACTIVE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JLabel subtitle = new JLabel("Graph-Based Shortest Route Engine", SwingConstants.CENTER);
        subtitle.setFont(UITheme.BODY_FONT);
        subtitle.setForeground(UITheme.SIDEBAR_TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel centerBox = new JPanel();
        centerBox.setOpaque(false);
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setBorder(BorderFactory.createEmptyBorder(48, 20, 0, 20));
        centerBox.add(badge);
        centerBox.add(title);
        centerBox.add(subtitle);

        JLabel statusLabel = new JLabel("Initializing route engine...", SwingConstants.CENTER);
        statusLabel.setFont(UITheme.LABEL_FONT);
        statusLabel.setForeground(UITheme.SIDEBAR_TEXT_MUTED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel version = new JLabel("v" + Constants.APP_VERSION, SwingConstants.CENTER);
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(UITheme.SIDEBAR_SELECTION);
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        version.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setForeground(UITheme.ACCENT_PRIMARY);
        progressBar.setBackground(UITheme.SIDEBAR_SELECTION);
        progressBar.setBorderPainted(false);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(260, 6));
        progressBar.setPreferredSize(new Dimension(260, 6));

        JPanel southBox = new JPanel();
        southBox.setOpaque(false);
        southBox.setLayout(new BoxLayout(southBox, BoxLayout.Y_AXIS));
        southBox.setBorder(BorderFactory.createEmptyBorder(0, 50, 22, 50));
        southBox.add(statusLabel);
        southBox.add(version);
        southBox.add(progressBar);

        content.add(centerBox, BorderLayout.CENTER);
        content.add(southBox, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(480, 280);
        setLocationRelativeTo(null);
    }

    /**
     * Displays the splash screen, waits for {@link Constants#SPLASH_SCREEN_DURATION_MS},
     * then disposes it and invokes {@code onFinished} — intended to launch
     * {@link MainFrame}.
     *
     * @param onFinished callback run on the EDT once the splash screen closes
     */
    public void showSplash(Runnable onFinished) {
        setVisible(true);
        Timer timer = new Timer(Constants.SPLASH_SCREEN_DURATION_MS, e -> {
            setVisible(false);
            dispose();
            onFinished.run();
        });
        timer.setRepeats(false);
        timer.start();
    }

    /** Small self-contained circular brand mark drawn with vector shapes (no image assets). */
    private static final class LogoBadge extends JPanel {

        private LogoBadge() {
            setOpaque(false);
            setPreferredSize(new Dimension(64, 64));
            setMaximumSize(new Dimension(64, 64));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT_PRIMARY);
                g2.fillOval(0, 0, 64, 64);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(5, 5, 54, 54);

                var icon = IconFactory.getIcon(IconFactory.IconType.MAP, 32, Color.WHITE);
                icon.paintIcon(this, g2, (64 - icon.getIconWidth()) / 2, (64 - icon.getIconHeight()) / 2);
            } finally {
                g2.dispose();
            }
        }
    }
}
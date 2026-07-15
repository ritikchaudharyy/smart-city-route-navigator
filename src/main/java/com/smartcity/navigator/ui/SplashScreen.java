package com.smartcity.navigator.ui;

import com.smartcity.navigator.utils.Constants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

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
        content.setBackground(new Color(24, 38, 63));
        content.setBorder(BorderFactory.createLineBorder(new Color(90, 140, 255), 2));

        JLabel title = new JLabel(Constants.APP_TITLE, SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Graph-Based Shortest Route Engine", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(180, 200, 230));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel centerBox = new JPanel();
        centerBox.setOpaque(false);
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));
        centerBox.setBorder(BorderFactory.createEmptyBorder(60, 20, 0, 20));
        centerBox.add(title);
        centerBox.add(subtitle);

        JLabel version = new JLabel("v" + Constants.APP_VERSION, SwingConstants.CENTER);
        version.setFont(new Font("SansSerif", Font.PLAIN, 11));
        version.setForeground(new Color(140, 160, 190));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorder(BorderFactory.createEmptyBorder(10, 50, 20, 50));

        JPanel southBox = new JPanel();
        southBox.setOpaque(false);
        southBox.setLayout(new BoxLayout(southBox, BoxLayout.Y_AXIS));
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        southBox.add(version);
        southBox.add(progressBar);

        content.add(centerBox, BorderLayout.CENTER);
        content.add(southBox, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(460, 260);
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
}

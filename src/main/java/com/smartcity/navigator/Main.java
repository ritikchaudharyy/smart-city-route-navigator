package com.smartcity.navigator;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;
import com.smartcity.navigator.graph.GraphLoadException;
import com.smartcity.navigator.service.RouteService;
import com.smartcity.navigator.ui.MainFrame;
import com.smartcity.navigator.ui.SplashScreen;
import com.smartcity.navigator.ui.UITheme;
import com.smartcity.navigator.utils.AppConfig;
import com.smartcity.navigator.utils.AppLogger;

/**
 * Application entry point. Follows the workflow: Application Starts -&gt;
 * Splash Screen -&gt; Main Window -&gt; Graph Loads, then hands control to
 * {@link MainFrame} for everything after that.
 * <p>
 * All Swing work is dispatched onto the Event Dispatch Thread via
 * {@link SwingUtilities#invokeLater}, per standard Swing threading rules.
 *
 * @author Smart City Route Navigator Team
 */
public final class Main {

    private Main() {
        // Entry point class: not instantiable.
    }

    public static void main(String[] args) {
        configureLookAndFeel();
        AppLogger.info("Starting Smart City Route Navigator");
        SwingUtilities.invokeLater(() -> new SplashScreen().showSplash(Main::launchMainFrame));
    }

    private static void configureLookAndFeel() {
        FlatLightLaf.setup();
        UIManager.put("defaultFont", UITheme.BODY_FONT);
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("ToolTip.font", UITheme.BODY_FONT);
        UIManager.put("MenuBar.background", UITheme.WINDOW_BACKGROUND);
        UIManager.put("Menu.background", UITheme.WINDOW_BACKGROUND);
        UIManager.put("MenuItem.background", UITheme.WINDOW_BACKGROUND);
        UIManager.put("Panel.background", UITheme.WINDOW_BACKGROUND);
        UIManager.put("TextField.background", UITheme.PANEL_BACKGROUND);
        UIManager.put("TextArea.background", UITheme.PANEL_BACKGROUND);
        UIManager.put("Label.foreground", UITheme.HEADING_COLOR);
        UIManager.put("MenuItem.acceleratorForeground", UITheme.LABEL_COLOR);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
    }

    private static void launchMainFrame() {
        try {
            RouteService routeService = new RouteService();
            MainFrame frame = new MainFrame(routeService);
            frame.setTitle(AppConfig.getProperty("app.name", "Smart City Route Navigator") + " v" + AppConfig.getProperty("app.version", "1.0.0"));
            frame.setVisible(true);
        } catch (GraphLoadException e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to start Smart City Route Navigator:\n" + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}

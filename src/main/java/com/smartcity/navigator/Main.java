package com.smartcity.navigator;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;
import com.smartcity.navigator.graph.GraphLoadException;
import com.smartcity.navigator.service.AuthService;
import com.smartcity.navigator.service.RouteService;
import com.smartcity.navigator.service.ai.GeminiService;
import com.smartcity.navigator.ui.LoginFrame;
import com.smartcity.navigator.ui.MainFrame;
import com.smartcity.navigator.ui.SplashScreen;
import com.smartcity.navigator.ui.UITheme;
import com.smartcity.navigator.utils.AppConfig;
import com.smartcity.navigator.utils.AppLogger;

/**
 * Application entry point. Follows the workflow:
 * Application Starts -> Splash Screen -> Login Portal -> Main Dashboard -> Hands control to
 * {@link MainFrame} for map and path rendering.
 * <p>
 * All Swing work is dispatched onto the Event Dispatch Thread via
 * {@link SwingUtilities#invokeLater}, per standard Swing threading rules.
 *
 * @author Smart City Route Navigator Team
 */
public final class Main {

    private static final AtomicBoolean FATAL_ERROR_SHOWN = new AtomicBoolean(false);

    private Main() {
        // Entry point class: not instantiable.
    }

    public static void main(String[] args) {
        installGlobalErrorHandler();
        try {
            configureLookAndFeel();
            AppLogger.info("Starting Smart City Route Navigator");
        } catch (RuntimeException exception) {
            AppLogger.error("Application look-and-feel setup failed", exception);
            showFatalError("The application could not start its user interface. Please restart it.");
            return;
        }

        // Boot process: Splash Screen -> Transition to the Login Gateway
        SwingUtilities.invokeLater(() ->
            new SplashScreen().showSplash(Main::launchLoginFrame)
        );
    }

    private static void configureLookAndFeel() {
        // Core FlatLaf initialization
        FlatLightLaf.setup();

        // System-wide premium UI rendering configurations (anti-aliasing)
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // High-DPI Scaling options and window adjustments
        UIManager.put("flatlaf.useWindowDecorations", true);
        UIManager.put("flatlaf.menuBarEmbedded", true);

        // Rounded corners and architectural elements (Enterprise aesthetic)
        UIManager.put("defaultFont", UITheme.BODY_FONT);
        UIManager.put("Button.arc", 16);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ProgressBar.arc", 12);

        // Consistent colors aligned with our Design System tokens
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

    /**
     * Instantiates the Login Authentication frame first.
     * Keeps the core graph and map assets protected until authorized.
     */
    private static void launchLoginFrame() {
        AuthService authService = new AuthService();
        LoginFrame loginFrame = new LoginFrame(authService, Main::launchMainFrame);
        loginFrame.setVisible(true);
    }

    /**
     * Instantiated only after authentication checks succeed.
     * Loads graph files and renders the application dashboard framework.
     */
    private static void launchMainFrame() {
        try {
            RouteService routeService = new RouteService();
            GeminiService geminiService = new GeminiService();
            MainFrame frame = new MainFrame(routeService, geminiService);
            frame.setTitle(AppConfig.getProperty("app.name", "Smart City Route Navigator") + " v" + AppConfig.getProperty("app.version", "1.0.0"));
            frame.setVisible(true);
        } catch (GraphLoadException e) {
            AppLogger.error("The default city graph could not be loaded", e);
            showFatalError("The city graph could not be loaded. Verify the application files and restart.");
        } catch (RuntimeException exception) {
            AppLogger.error("Main dashboard startup failed", exception);
            showFatalError("The dashboard could not be started. Please restart the application.");
        }
    }

    private static void installGlobalErrorHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            AppLogger.error("Unexpected application failure on thread " + thread.getName(), throwable);
            showFatalError("The application encountered an unexpected problem. Please restart it.");
        });
    }

    private static void showFatalError(String message) {
        if (GraphicsEnvironment.isHeadless() || !FATAL_ERROR_SHOWN.compareAndSet(false, true)) {
            return;
        }

        Runnable showDialog = () -> {
            try {
                JOptionPane.showMessageDialog(null, message, "Smart City Route Navigator", JOptionPane.ERROR_MESSAGE);
            } catch (RuntimeException dialogException) {
                AppLogger.error("Unable to show the application error dialog", dialogException);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            showDialog.run();
        } else {
            SwingUtilities.invokeLater(showDialog);
        }
    }
}
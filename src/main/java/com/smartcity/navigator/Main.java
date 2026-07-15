package com.smartcity.navigator;

import com.smartcity.navigator.graph.GraphLoadException;
import com.smartcity.navigator.service.RouteService;
import com.smartcity.navigator.ui.MainFrame;
import com.smartcity.navigator.ui.SplashScreen;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

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
        SwingUtilities.invokeLater(() -> new SplashScreen().showSplash(Main::launchMainFrame));
    }

    private static void launchMainFrame() {
        try {
            RouteService routeService = new RouteService();
            MainFrame frame = new MainFrame(routeService);
            frame.setVisible(true);
        } catch (GraphLoadException e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to start Smart City Route Navigator:\n" + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}

package com.smartcity.navigator.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.smartcity.navigator.service.AuthService;
import com.smartcity.navigator.utils.AppLogger;

/**
 * A premium, responsive, split-pane authentication portal.
 *
 * @author Smart City Route Navigator Team
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private final Runnable onSuccessCallback;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private JProgressBar progressLoader;
    private JLabel lblErrorMessage;
    private JPanel pnlErrorBanner;
    private char passwordEchoChar;

    public LoginFrame(AuthService authService, Runnable onSuccessCallback) {
        this.authService = authService;
        this.onSuccessCallback = onSuccessCallback;

        initializeFrame();
        buildUI();
        setupListeners();
        setInteractiveState(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {
                SwingUtilities.invokeLater(() -> txtUsername.requestFocusInWindow());
            }
        });
    }

    private void initializeFrame() {
        setTitle("Smart City System Gateway - Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(880, 540);
        setMinimumSize(new Dimension(760, 500));
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void buildUI() {
        JPanel pnlMaster = new JPanel(new BorderLayout());
        pnlMaster.setBackground(UITheme.WINDOW_BACKGROUND);

        JPanel pnlBrand = createBrandPanel();
        pnlMaster.add(pnlBrand, BorderLayout.WEST);

        JPanel pnlForm = createFormPanel();
        pnlMaster.add(pnlForm, BorderLayout.CENTER);

        setContentPane(pnlMaster);
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.SIDEBAR_BACKGROUND);
        panel.setPreferredSize(new Dimension(380, getHeight()));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 40, 10, 40);

        LogoBadge badge = new LogoBadge();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 40, 22, 40);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(badge, gbc);

        JLabel lblTag = new JLabel("INTELLIGENT TRANSIT");
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTag.setForeground(UITheme.SUCCESS_COLOR);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 40, 6, 40);
        panel.add(lblTag, gbc);

        JLabel lblTitle = new JLabel("Smart City");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(UITheme.SIDEBAR_TEXT_ACTIVE);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 40, 0, 40);
        panel.add(lblTitle, gbc);

        JLabel lblSubTitle = new JLabel("Route Navigator");
        lblSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        lblSubTitle.setForeground(UITheme.SIDEBAR_TEXT_MUTED);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 40, 10, 40);
        panel.add(lblSubTitle, gbc);

        JSeparator sep = new JSeparator();
        sep.setBackground(UITheme.SIDEBAR_SELECTION);
        sep.setForeground(UITheme.SIDEBAR_SELECTION);
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 40, 20, 40);
        panel.add(sep, gbc);

        gbc.insets = new Insets(9, 40, 9, 40);

        gbc.gridy = 5;
        panel.add(buildFeatureRow(IconFactory.IconType.SEARCH_ROUTE, "Dijkstra Optimized Pathfinding"), gbc);

        gbc.gridy = 6;
        panel.add(buildFeatureRow(IconFactory.IconType.MAP, "Interactive Graph Render Engine"), gbc);

        gbc.gridy = 7;
        panel.add(buildFeatureRow(IconFactory.IconType.AI_SPARKLE, "Natural Language AI Assistant"), gbc);

        JLabel lblFooter = new JLabel("Enterprise Core Engine v1.0.0");
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFooter.setForeground(UITheme.SIDEBAR_SELECTION);
        gbc.gridy = 8;
        gbc.insets = new Insets(50, 40, 0, 40);
        panel.add(lblFooter, gbc);

        return panel;
    }

    private JPanel buildFeatureRow(IconFactory.IconType iconType, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        JLabel icon = new JLabel(IconFactory.getIcon(iconType, 15, UITheme.SUCCESS_COLOR));
        JLabel label = new JLabel(text);
        label.setFont(UITheme.BODY_FONT);
        label.setForeground(UITheme.SIDEBAR_TEXT_MUTED);

        row.add(icon);
        row.add(label);
        return row;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.PANEL_BACKGROUND);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblHeader = new JLabel("Sign In Gateway");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setForeground(UITheme.HEADING_COLOR);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(lblHeader, gbc);

        JLabel lblSub = new JLabel("Enter system credentials to access route maps.");
        lblSub.setFont(UITheme.BODY_FONT);
        lblSub.setForeground(UITheme.SECONDARY_TEXT);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 22, 0);
        panel.add(lblSub, gbc);

        pnlErrorBanner = buildErrorBanner();
        pnlErrorBanner.setVisible(false);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 18, 0);
        panel.add(pnlErrorBanner, gbc);

        JLabel lblUserLabel = new JLabel("System Username");
        lblUserLabel.setFont(UITheme.LABEL_FONT);
        lblUserLabel.setForeground(UITheme.LABEL_COLOR);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(lblUserLabel, gbc);

        txtUsername = new JTextField();
        txtUsername.setEnabled(true);
        txtUsername.setEditable(true);
        txtUsername.setFocusable(true);
        UITheme.styleTextField(txtUsername);
        txtUsername.putClientProperty("JTextField.placeholderText", "Enter authorized username (e.g., admin)");
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(txtUsername, gbc);

        JLabel lblPassLabel = new JLabel("Access Password");
        lblPassLabel.setFont(UITheme.LABEL_FONT);
        lblPassLabel.setForeground(UITheme.LABEL_COLOR);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(lblPassLabel, gbc);

        txtPassword = new JPasswordField();
        passwordEchoChar = txtPassword.getEchoChar();
        txtPassword.setEnabled(true);
        txtPassword.setEditable(true);
        txtPassword.setFocusable(true);
        UITheme.styleTextField(txtPassword);
        txtPassword.putClientProperty("JTextField.placeholderText", "••••••••");
        txtPassword.putClientProperty("JTextField.showClearButton", true);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(txtPassword, gbc);

        JPanel pnlAux = new JPanel(new BorderLayout());
        pnlAux.setBackground(UITheme.PANEL_BACKGROUND);

        JLabel sessionNotice = new JLabel("Your session ends when the application closes.");
        sessionNotice.setFont(UITheme.LABEL_FONT);
        sessionNotice.setForeground(UITheme.SECONDARY_TEXT);
        pnlAux.add(sessionNotice, BorderLayout.WEST);

        chkShowPassword = new JCheckBox("Show Password");
        UITheme.styleCheckBox(chkShowPassword);
        chkShowPassword.setBackground(UITheme.PANEL_BACKGROUND);
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });
        pnlAux.add(chkShowPassword, BorderLayout.EAST);
        chkShowPassword.addActionListener(e -> txtPassword.setEchoChar(
                chkShowPassword.isSelected() ? (char) 0 : passwordEchoChar));

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 30, 0);
        panel.add(pnlAux, gbc);

        progressLoader = new JProgressBar();
        progressLoader.setIndeterminate(true);
        progressLoader.setVisible(false);
        progressLoader.setForeground(UITheme.ACCENT_PRIMARY);
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(progressLoader, gbc);

        JPanel pnlButtons = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlButtons.setBackground(UITheme.PANEL_BACKGROUND);

        btnLogin = new JButton("Enter System");
        UITheme.stylePrimaryActionButton(btnLogin);

        btnExit = new JButton("Exit Gateway");
        UITheme.styleSecondaryToolBarButton(btnExit);

        pnlButtons.add(btnLogin);
        pnlButtons.add(btnExit);

        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(pnlButtons, gbc);

        getRootPane().setDefaultButton(btnLogin);

        return panel;
    }

    private JPanel buildErrorBanner() {
        JPanel banner = new JPanel(new BorderLayout(8, 0));
        banner.setOpaque(true);
        banner.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        lblErrorMessage = new JLabel(" ");
        lblErrorMessage.setFont(UITheme.BODY_FONT);
        banner.add(lblErrorMessage, BorderLayout.CENTER);
        return banner;
    }

    private void setupListeners() {
        btnLogin.addActionListener(e -> attemptLogin());
        btnExit.addActionListener(e -> System.exit(0));
    }

    private void attemptLogin() {
        String username = txtUsername.getText().trim();
        char[] password = txtPassword.getPassword();

        if (username.isEmpty() || password.length == 0) {
            Arrays.fill(password, '\0');
            showMessage("Enter both your username and password.", UITheme.ERROR_COLOR);
            return;
        }

        setInteractiveState(false);
        showMessage("Verifying secure access...", UITheme.SECONDARY_TEXT);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    return authService.authenticate(username, password);
                } finally {
                    Arrays.fill(password, '\0');
                }
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        AppLogger.info("Authentication validated. Transitioning to main frame...");
                        dispose();
                        onSuccessCallback.run();
                    } else {
                        setInteractiveState(true);
                        txtPassword.setText("");
                        txtPassword.requestFocusInWindow();
                        showMessage("Access denied. Check your credentials and try again.", UITheme.ERROR_COLOR);
                    }
                } catch (Exception ex) {
                    AppLogger.error("Login task execution failed", ex);
                    setInteractiveState(true);
                    showMessage("The sign-in service is unavailable. Please try again.", UITheme.ERROR_COLOR);
                }
            }
        };

        worker.execute();
    }

    private void setInteractiveState(boolean enabled) {
        txtUsername.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        chkShowPassword.setEnabled(enabled);
        btnLogin.setEnabled(enabled);
        btnExit.setEnabled(enabled);
        progressLoader.setVisible(!enabled);
    }

    private void showMessage(String message, Color color) {
        if (message == null || message.isBlank()) {
            pnlErrorBanner.setVisible(false);
            return;
        }

        boolean isNeutral = color == UITheme.SECONDARY_TEXT;
        Color background = isNeutral ? UITheme.SUBPANEL_BACKGROUND : new Color(254, 242, 242);
        Color borderColor = isNeutral ? UITheme.BORDER_COLOR : new Color(254, 202, 202);

        pnlErrorBanner.setBackground(background);
        pnlErrorBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        lblErrorMessage.setForeground(color);
        lblErrorMessage.setText(message);
        pnlErrorBanner.setVisible(true);
    }

    /** Small self-contained circular brand mark drawn with vector shapes (no image assets). */
    private static final class LogoBadge extends JPanel {

        private LogoBadge() {
            setOpaque(false);
            setPreferredSize(new Dimension(52, 52));
            setMaximumSize(new Dimension(52, 52));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT_PRIMARY);
                g2.fillOval(0, 0, 52, 52);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(4, 4, 44, 44);

                var icon = IconFactory.getIcon(IconFactory.IconType.MAP, 26, Color.WHITE);
                icon.paintIcon(this, g2, (52 - icon.getIconWidth()) / 2, (52 - icon.getIconHeight()) / 2);
            } finally {
                g2.dispose();
            }
        }
    }
}

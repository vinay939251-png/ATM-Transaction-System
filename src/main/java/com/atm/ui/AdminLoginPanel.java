package com.atm.ui;

import com.atm.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Separate administrator authentication workspace. */
public class AdminLoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private final AuthenticationService authService = new AuthenticationService();

    public AdminLoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setOpaque(true);
        setBackground(ATMTheme.BG_PRIMARY);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        add(createBrandPanel(), BorderLayout.WEST);
        add(createFormWorkspace(), BorderLayout.CENTER);
    }

    private JPanel createBrandPanel() {
        JPanel brand = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(0x3A2414), getWidth(), getHeight(), ATMTheme.BG_PRIMARY));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(ATMTheme.WARNING.getRed(), ATMTheme.WARNING.getGreen(), ATMTheme.WARNING.getBlue(), 50));
                g2.fillOval(-160, getHeight() - 250, 430, 430);
                g2.dispose();
            }
        };
        brand.setOpaque(false);
        brand.setPreferredSize(new Dimension(460, 0));
        brand.setBorder(new EmptyBorder(54, 56, 54, 52));
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        JLabel mark = ATMTheme.createLabel("ADMIN", new Font("Segoe UI", Font.BOLD, 17), ATMTheme.BG_PRIMARY);
        mark.setOpaque(true);
        mark.setBackground(ATMTheme.WARNING);
        mark.setBorder(new EmptyBorder(9, 12, 9, 12));
        mark.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(mark);
        brand.add(Box.createVerticalGlue());

        JLabel eyebrow = ATMTheme.createLabel("CONTROL CENTRE / 02", ATMTheme.FONT_SMALL, ATMTheme.WARNING);
        JLabel title = ATMTheme.createLabel("<html>Trust needs<br>clear control.</html>", ATMTheme.FONT_TITLE, ATMTheme.TEXT_PRIMARY);
        JLabel copy = ATMTheme.createLabel("<html><div style='width:320px;line-height:1.6'>Review customer accounts, inspect transaction outcomes, and manage simulated card access from one protected workspace.</div></html>", ATMTheme.FONT_BODY, ATMTheme.TEXT_SECONDARY);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        copy.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(eyebrow);
        brand.add(Box.createVerticalStrut(14));
        brand.add(title);
        brand.add(Box.createVerticalStrut(18));
        brand.add(copy);
        brand.add(Box.createVerticalGlue());

        for (String item : new String[]{"Separate administrator access", "Audit-focused workspace", "Local academic demonstration"}) {
            JLabel line = ATMTheme.createLabel("●  " + item, ATMTheme.FONT_SMALL, ATMTheme.TEXT_PRIMARY);
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            brand.add(line);
            brand.add(Box.createVerticalStrut(8));
        }
        return brand;
    }

    private JPanel createFormWorkspace() {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setOpaque(false);
        workspace.setBorder(new EmptyBorder(42, 70, 42, 70));

        JPanel form = ATMTheme.createCard();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(34, 40, 34, 40));
        form.setPreferredSize(new Dimension(470, 450));
        form.setMaximumSize(new Dimension(520, 500));

        JLabel section = ATMTheme.createLabel("ADMINISTRATOR ACCESS", ATMTheme.FONT_SMALL, ATMTheme.WARNING);
        JLabel title = ATMTheme.createLabel("Sign in to control centre", ATMTheme.FONT_HEADING, ATMTheme.TEXT_PRIMARY);
        JLabel subtitle = ATMTheme.createLabel("Authorized personnel only", ATMTheme.FONT_BODY, ATMTheme.TEXT_SECONDARY);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(section);
        form.add(Box.createVerticalStrut(10));
        form.add(title);
        form.add(Box.createVerticalStrut(6));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(30));

        form.add(fieldLabel("Username"));
        usernameField = ATMTheme.createTextField("Enter admin username");
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(17));

        form.add(fieldLabel("Password"));
        passwordField = ATMTheme.createPasswordField("Enter password");
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(22));

        JButton loginBtn = ATMTheme.createPrimaryButton("Enter control centre");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        loginBtn.addActionListener(e -> handleAdminLogin());
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(12));

        statusLabel = ATMTheme.createLabel(" ", ATMTheme.FONT_SMALL, ATMTheme.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(statusLabel);
        form.add(Box.createVerticalGlue());

        JButton backBtn = ATMTheme.createSecondaryButton("← Back to ATM login");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        backBtn.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_LOGIN));
        form.add(backBtn);

        workspace.add(form);
        passwordField.addActionListener(e -> handleAdminLogin());
        return workspace;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = ATMTheme.createLabel(text, ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void handleAdminLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) { statusLabel.setText("Please enter username and password."); return; }
        statusLabel.setForeground(ATMTheme.TEXT_SECONDARY);
        statusLabel.setText("Authenticating administrator session...");

        SwingWorker<AuthenticationService.AdminAuthResult, Void> worker = new SwingWorker<>() {
            @Override protected AuthenticationService.AdminAuthResult doInBackground() { return authService.authenticateAdmin(username, password); }
            @Override protected void done() {
                try {
                    var result = get();
                    if (result.success()) {
                        usernameField.setText(""); passwordField.setText(""); statusLabel.setText(""); mainFrame.startAdminSession(result.admin());
                    } else {
                        statusLabel.setForeground(ATMTheme.DANGER); statusLabel.setText(result.message()); passwordField.setText("");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(ATMTheme.DANGER); statusLabel.setText("System error. Please try again.");
                }
            }
        };
        worker.execute();
    }
}

package com.atm.ui;

import com.atm.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Customer ATM login workspace. */
public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private JTextField cardField;
    private JPasswordField pinField;
    private JLabel statusLabel;
    private final AuthenticationService authService = new AuthenticationService();

    public LoginPanel(MainFrame mainFrame) {
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
                g2.setPaint(new GradientPaint(0, 0, ATMTheme.GRADIENT_START, getWidth(), getHeight(), ATMTheme.BG_PRIMARY));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(ATMTheme.ACCENT_PRIMARY.getRed(), ATMTheme.ACCENT_PRIMARY.getGreen(), ATMTheme.ACCENT_PRIMARY.getBlue(), 55));
                g2.fillOval(-160, getHeight() - 250, 430, 430);
                g2.dispose();
            }
        };
        brand.setOpaque(false);
        brand.setPreferredSize(new Dimension(460, 0));
        brand.setBorder(new EmptyBorder(54, 56, 54, 52));
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        JLabel mark = ATMTheme.createLabel("ATM", new Font("Segoe UI", Font.BOLD, 22), ATMTheme.BG_PRIMARY);
        mark.setHorizontalAlignment(SwingConstants.CENTER);
        mark.setOpaque(true);
        mark.setBackground(ATMTheme.ACCENT_PRIMARY);
        mark.setBorder(new EmptyBorder(8, 12, 8, 12));
        mark.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(mark);
        brand.add(Box.createVerticalGlue());

        JLabel eyebrow = ATMTheme.createLabel("PERSONAL BANKING / 01", ATMTheme.FONT_SMALL, ATMTheme.ACCENT_SECONDARY);
        JLabel title = ATMTheme.createLabel("<html>Your money,<br>with clarity.</html>", ATMTheme.FONT_TITLE, ATMTheme.TEXT_PRIMARY);
        JLabel copy = ATMTheme.createLabel("<html><div style='width:320px;line-height:1.6'>A calm, secure workspace for checking balances, moving funds, and understanding every transaction.</div></html>", ATMTheme.FONT_BODY, ATMTheme.TEXT_SECONDARY);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        copy.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(eyebrow);
        brand.add(Box.createVerticalStrut(14));
        brand.add(title);
        brand.add(Box.createVerticalStrut(18));
        brand.add(copy);
        brand.add(Box.createVerticalGlue());

        JPanel points = new JPanel();
        points.setOpaque(false);
        points.setLayout(new BoxLayout(points, BoxLayout.Y_AXIS));
        for (String item : new String[]{"Live account visibility", "Secure session handling", "Simple, focused actions"}) {
            JLabel line = ATMTheme.createLabel("●  " + item, ATMTheme.FONT_SMALL, ATMTheme.TEXT_PRIMARY);
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            points.add(line);
            points.add(Box.createVerticalStrut(8));
        }
        brand.add(points);
        return brand;
    }

    private JPanel createFormWorkspace() {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setOpaque(false);
        workspace.setBorder(new EmptyBorder(42, 70, 42, 70));

        JPanel form = ATMTheme.createCard();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(34, 40, 34, 40));
        form.setPreferredSize(new Dimension(470, 520));
        form.setMaximumSize(new Dimension(520, 560));

        JLabel section = ATMTheme.createLabel("SIGN IN", ATMTheme.FONT_SMALL, ATMTheme.ACCENT_PRIMARY);
        JLabel title = ATMTheme.createLabel("Welcome back", ATMTheme.FONT_HEADING, ATMTheme.TEXT_PRIMARY);
        JLabel subtitle = ATMTheme.createLabel("Use your synthetic ATM card to continue", ATMTheme.FONT_BODY, ATMTheme.TEXT_SECONDARY);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(section);
        form.add(Box.createVerticalStrut(10));
        form.add(title);
        form.add(Box.createVerticalStrut(6));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(30));

        form.add(fieldLabel("Card number"));
        cardField = ATMTheme.createTextField("XXXX-XXXX-XXXX-XXXX");
        cardField.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        form.add(cardField);
        form.add(Box.createVerticalStrut(17));

        form.add(fieldLabel("PIN"));
        pinField = ATMTheme.createPasswordField("Enter 4-digit PIN");
        pinField.setAlignmentX(Component.LEFT_ALIGNMENT);
        pinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        form.add(pinField);
        form.add(Box.createVerticalStrut(22));

        JButton loginBtn = ATMTheme.createPrimaryButton("Continue to account");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        loginBtn.addActionListener(e -> handleLogin());
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(12));

        statusLabel = ATMTheme.createLabel(" ", ATMTheme.FONT_SMALL, ATMTheme.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(statusLabel);
        form.add(Box.createVerticalGlue());

        JPanel links = new JPanel(new GridLayout(1, 2, 10, 0));
        links.setOpaque(false);
        links.setAlignmentX(Component.LEFT_ALIGNMENT);
        links.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JButton signUp = ATMTheme.createSecondaryButton("Create account");
        signUp.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_SIGNUP));
        JButton admin = ATMTheme.createSecondaryButton("Admin portal");
        admin.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_ADMIN_LOGIN));
        links.add(signUp);
        links.add(admin);
        form.add(links);

        workspace.add(form);
        cardField.addActionListener(e -> pinField.requestFocusInWindow());
        pinField.addActionListener(e -> handleLogin());
        return workspace;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = ATMTheme.createLabel(text, ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    public void setCardNumber(String cardNumber) {
        if (cardField != null) cardField.setText(cardNumber);
        if (pinField != null) {
            pinField.setText("");
            SwingUtilities.invokeLater(() -> pinField.requestFocusInWindow());
        }
        if (statusLabel != null) {
            statusLabel.setForeground(ATMTheme.ACCENT_PRIMARY);
            statusLabel.setText("Card issued. Enter your PIN to sign in.");
        }
    }

    private void handleLogin() {
        String cardNumber = cardField.getText().trim();
        String pin = new String(pinField.getPassword());
        if (cardNumber.isEmpty()) { statusLabel.setText("Please enter your card number."); return; }
        if (pin.isEmpty()) { statusLabel.setText("Please enter your PIN."); return; }
        statusLabel.setForeground(ATMTheme.TEXT_SECONDARY);
        statusLabel.setText("Authenticating secure session...");

        SwingWorker<AuthenticationService.AuthResult, Void> worker = new SwingWorker<>() {
            @Override protected AuthenticationService.AuthResult doInBackground() { return authService.authenticateATMUser(cardNumber, pin); }
            @Override protected void done() {
                try {
                    var result = get();
                    if (result.success()) {
                        cardField.setText(""); pinField.setText(""); statusLabel.setText(""); mainFrame.startSession(result.card());
                    } else {
                        statusLabel.setForeground(ATMTheme.DANGER); statusLabel.setText(result.message()); pinField.setText(""); pinField.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(ATMTheme.DANGER); statusLabel.setText("System error. Please try again.");
                }
            }
        };
        worker.execute();
    }
}

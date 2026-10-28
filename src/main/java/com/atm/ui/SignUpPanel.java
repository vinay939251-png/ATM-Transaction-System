package com.atm.ui;

import com.atm.model.Account;
import com.atm.service.RegistrationService;
import com.atm.service.RegistrationService.RegistrationRequest;
import com.atm.service.RegistrationService.RegistrationResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;

/**
 * Sign-up and account creation screen.
 * Allows users to register a new customer account, specify or generate
 * their own custom 16-digit ATM card (e.g., 4532-1234-5678-9010),
 * set an opening deposit, and establish their 4-digit PIN.
 */
public class SignUpPanel extends JPanel implements MainFrame.Refreshable {

    private final MainFrame mainFrame;
    private final RegistrationService registrationService = new RegistrationService();

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<Account.AccountType> accountTypeCombo;
    private JTextField depositField;
    private JTextField cardField;
    private JPasswordField pinField;
    private JPasswordField confirmPinField;
    private JLabel statusLabel;
    private JButton createBtn;

    public SignUpPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setOpaque(true);
        setBackground(ATMTheme.BG_PRIMARY);
        setLayout(new BorderLayout());
        add(createBrandPanel(), BorderLayout.WEST);

        JPanel formHost = new JPanel(new GridBagLayout());
        formHost.setOpaque(false);
        formHost.setBorder(new EmptyBorder(24, 44, 24, 44));
        formHost.add(createSignUpCard());

        JScrollPane scrollPane = new JScrollPane(formHost);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createBrandPanel() {
        JPanel brand = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, ATMTheme.GRADIENT_START, getWidth(), getHeight(), ATMTheme.BG_PRIMARY));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(ATMTheme.ACCENT_SECONDARY.getRed(), ATMTheme.ACCENT_SECONDARY.getGreen(), ATMTheme.ACCENT_SECONDARY.getBlue(), 42));
                g2.fillOval(-150, getHeight() - 260, 420, 420);
                g2.dispose();
            }
        };
        brand.setOpaque(false);
        brand.setPreferredSize(new Dimension(390, 0));
        brand.setBorder(new EmptyBorder(48, 48, 48, 42));
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        JLabel mark = ATMTheme.createLabel("NEW ACCOUNT", new Font("Segoe UI", Font.BOLD, 15), ATMTheme.BG_PRIMARY);
        mark.setOpaque(true);
        mark.setBackground(ATMTheme.ACCENT_PRIMARY);
        mark.setBorder(new EmptyBorder(9, 12, 9, 12));
        mark.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(mark);
        brand.add(Box.createVerticalGlue());

        JLabel eyebrow = ATMTheme.createLabel("ONBOARDING / 03", ATMTheme.FONT_SMALL, ATMTheme.ACCENT_SECONDARY);
        JLabel title = ATMTheme.createLabel("<html>Start with a<br>clear account.</html>", ATMTheme.FONT_TITLE, ATMTheme.TEXT_PRIMARY);
        JLabel copy = ATMTheme.createLabel("<html><div style='width:260px;line-height:1.6'>Create a synthetic account, choose your card details, and set a secure PIN for this academic demonstration.</div></html>", ATMTheme.FONT_BODY, ATMTheme.TEXT_SECONDARY);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        copy.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(eyebrow);
        brand.add(Box.createVerticalStrut(14));
        brand.add(title);
        brand.add(Box.createVerticalStrut(18));
        brand.add(copy);
        brand.add(Box.createVerticalGlue());
        for (String item : new String[]{"Synthetic data only", "BCrypt-protected PIN", "Atomic account creation"}) {
            JLabel line = ATMTheme.createLabel("●  " + item, ATMTheme.FONT_SMALL, ATMTheme.TEXT_PRIMARY);
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            brand.add(line);
            brand.add(Box.createVerticalStrut(8));
        }
        return brand;
    }

    private JPanel createSignUpCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card background
                g2.setColor(ATMTheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));

                // Top gradient border
                GradientPaint gp = new GradientPaint(0, 0, ATMTheme.ACCENT_PRIMARY, getWidth(), 0, ATMTheme.ACCENT_SECONDARY);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), 4, 24, 24);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 36, 28, 36));
        card.setPreferredSize(new Dimension(650, 650));
        card.setMaximumSize(new Dimension(700, 680));

        // Header icon
        JLabel icon = ATMTheme.createLabel("💳", new Font("Segoe UI Emoji", Font.PLAIN, 36), ATMTheme.ACCENT_PRIMARY);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(icon);
        card.add(Box.createVerticalStrut(6));

        JLabel title = ATMTheme.createLabel("Create Account & ATM Card", ATMTheme.FONT_HEADING, ATMTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);

        JLabel subtitle = ATMTheme.createLabel("Choose your custom card number or generate one instantly",
                ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        // 1. Name Row (First Name + Last Name)
        JPanel namePanel = new JPanel(new GridLayout(1, 2, 12, 0));
        namePanel.setOpaque(false);
        namePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        namePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel fnCol = createFieldColumn("First Name", firstNameField = ATMTheme.createTextField("e.g. John"));
        JPanel lnCol = createFieldColumn("Last Name", lastNameField = ATMTheme.createTextField("e.g. Doe"));
        namePanel.add(fnCol);
        namePanel.add(lnCol);
        card.add(namePanel);
        card.add(Box.createVerticalStrut(10));

        // 2. Contact Row (Email + Phone)
        JPanel contactPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        contactPanel.setOpaque(false);
        contactPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contactPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel emailCol = createFieldColumn("Email Address", emailField = ATMTheme.createTextField("john.doe@example.com"));
        JPanel phoneCol = createFieldColumn("Phone Number", phoneField = ATMTheme.createTextField("9876543210"));
        contactPanel.add(emailCol);
        contactPanel.add(phoneCol);
        card.add(contactPanel);
        card.add(Box.createVerticalStrut(10));

        // 3. Account Type & Initial Deposit Row
        JPanel accPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        accPanel.setOpaque(false);
        accPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        accPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        accountTypeCombo = new JComboBox<>(Account.AccountType.values());
        accountTypeCombo.setFont(ATMTheme.FONT_BODY);
        accountTypeCombo.setBackground(ATMTheme.BG_INPUT);
        accountTypeCombo.setForeground(ATMTheme.TEXT_PRIMARY);
        JPanel typeCol = createFieldColumn("Account Type", accountTypeCombo);

        depositField = ATMTheme.createTextField("5000.00");
        JPanel depCol = createFieldColumn("Initial Deposit (₹)", depositField);
        accPanel.add(typeCol);
        accPanel.add(depCol);
        card.add(accPanel);
        card.add(Box.createVerticalStrut(10));

        // 4. Custom ATM Card Number Row (with Generate button)
        JLabel cardNumLbl = ATMTheme.createLabel("Custom ATM Card Number (16 Digits)", ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        cardNumLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(cardNumLbl);
        card.add(Box.createVerticalStrut(4));

        JPanel cardRow = new JPanel(new BorderLayout(8, 0));
        cardRow.setOpaque(false);
        cardRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        cardField = ATMTheme.createTextField("4532-1234-5678-9010");
        JButton genBtn = ATMTheme.createSecondaryButton("⚡ Generate");
        genBtn.setPreferredSize(new Dimension(110, 38));
        genBtn.addActionListener(e -> generateNewCardNumber());

        cardRow.add(cardField, BorderLayout.CENTER);
        cardRow.add(genBtn, BorderLayout.EAST);
        card.add(cardRow);
        card.add(Box.createVerticalStrut(10));

        // 5. PIN Row (PIN + Confirm PIN)
        JPanel pinRow = new JPanel(new GridLayout(1, 2, 12, 0));
        pinRow.setOpaque(false);
        pinRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        pinRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel pinCol = createFieldColumn("4-Digit PIN", pinField = ATMTheme.createPasswordField("PIN"));
        JPanel cPinCol = createFieldColumn("Confirm PIN", confirmPinField = ATMTheme.createPasswordField("Confirm"));
        pinRow.add(pinCol);
        pinRow.add(cPinCol);
        card.add(pinRow);
        card.add(Box.createVerticalStrut(14));

        // Status message
        statusLabel = ATMTheme.createLabel(" ", ATMTheme.FONT_SMALL, ATMTheme.DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(10));

        // Action Buttons Row
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        createBtn = ATMTheme.createPrimaryButton("Create Account");
        createBtn.addActionListener(e -> handleRegistration());

        JButton backBtn = ATMTheme.createSecondaryButton("← Back to Login");
        backBtn.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_LOGIN));

        btnRow.add(backBtn);
        btnRow.add(createBtn);
        card.add(btnRow);

        return card;
    }

    private JPanel createFieldColumn(String labelText, JComponent field) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        JLabel lbl = ATMTheme.createLabel(labelText, ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        col.add(lbl);
        col.add(Box.createVerticalStrut(4));
        col.add(field);
        return col;
    }

    private void generateNewCardNumber() {
        cardField.setText(registrationService.generateUniqueCardNumber());
        statusLabel.setText("New card number generated.");
        statusLabel.setForeground(ATMTheme.ACCENT_PRIMARY);
    }

    private void handleRegistration() {
        String fn = firstNameField.getText().trim();
        String ln = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        Account.AccountType accType = (Account.AccountType) accountTypeCombo.getSelectedItem();
        String depStr = depositField.getText().trim();
        String cardNum = cardField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();
        String confirmPin = new String(confirmPinField.getPassword()).trim();

        // Basic client validation
        if (fn.isEmpty() || ln.isEmpty()) {
            statusLabel.setForeground(ATMTheme.DANGER);
            statusLabel.setText("First and last names are required.");
            return;
        }
        if (email.isEmpty() || phone.isEmpty()) {
            statusLabel.setForeground(ATMTheme.DANGER);
            statusLabel.setText("Email and phone number are required.");
            return;
        }
        if (pin.isEmpty()) {
            statusLabel.setForeground(ATMTheme.DANGER);
            statusLabel.setText("Please set a 4-digit PIN.");
            return;
        }
        if (!pin.equals(confirmPin)) {
            statusLabel.setForeground(ATMTheme.DANGER);
            statusLabel.setText("PIN and confirmation PIN do not match.");
            return;
        }

        BigDecimal deposit;
        try {
            deposit = new BigDecimal(depStr);
        } catch (NumberFormatException ex) {
            statusLabel.setForeground(ATMTheme.DANGER);
            statusLabel.setText("Invalid opening deposit amount.");
            return;
        }

        createBtn.setEnabled(false);
        createBtn.setText("Creating Account...");
        statusLabel.setForeground(ATMTheme.TEXT_SECONDARY);
        statusLabel.setText("Setting up your account and card...");

        RegistrationRequest req = new RegistrationRequest(fn, ln, email, phone, accType, deposit, cardNum, pin);

        SwingWorker<RegistrationResult, Void> worker = new SwingWorker<>() {
            @Override
            protected RegistrationResult doInBackground() {
                return registrationService.register(req);
            }

            @Override
            protected void done() {
                createBtn.setEnabled(true);
                createBtn.setText("Create Account");
                try {
                    RegistrationResult result = get();
                    if (result.success()) {
                        statusLabel.setForeground(ATMTheme.ACCENT_PRIMARY);
                        statusLabel.setText("Account opened successfully!");

                        String successMsg = String.format(
                                "Account Created Successfully!\n\n" +
                                "Customer: %s %s\n" +
                                "Account Number: %s (%s)\n" +
                                "Card Number: %s\n" +
                                "Initial Balance: ₹%s\n\n" +
                                "You can now log in using this card number and your PIN.",
                                fn, ln, result.accountNumber(), accType, result.cardNumber(), deposit.toPlainString()
                        );

                        ATMTheme.showSuccess(SignUpPanel.this, successMsg);

                        // Pre-fill card number in LoginPanel and navigate to Login
                        mainFrame.prefillLogin(result.cardNumber());
                    } else {
                        statusLabel.setForeground(ATMTheme.DANGER);
                        statusLabel.setText(result.message());
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(ATMTheme.DANGER);
                    statusLabel.setText("System error during sign-up: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    @Override
    public void onShow() {
        // Reset or initialize fields on display
        statusLabel.setText(" ");
        if (cardField.getText().trim().isEmpty()) {
            cardField.setText("4532-1234-5678-9010");
        }
    }
}

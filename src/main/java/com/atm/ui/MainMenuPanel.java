package com.atm.ui;

import com.atm.model.ATMCard;
import com.atm.service.TransactionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Main ATM dashboard after successful login.
 * The composition borrows the references' spacious editorial hero, trust badges,
 * compact metrics, and payment-dashboard hierarchy while keeping the application
 * fully local and educational.
 */
public class MainMenuPanel extends JPanel implements MainFrame.Refreshable {

    private final MainFrame mainFrame;
    private final TransactionService txnService = new TransactionService();
    private JLabel greetingLabel;
    private JLabel cardInfoLabel;
    private JLabel balanceValue;
    private JLabel balanceStatus;
    private JLabel accountValue;
    private JLabel securityValue;

    public MainMenuPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setOpaque(true);
        setBackground(ATMTheme.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 42, 22, 42));
        buildUI();
    }

    private void buildUI() {
        add(createTopBar(), BorderLayout.NORTH);
        add(createDashboard(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 22, 0));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel eyebrow = ATMTheme.createLabel("ATM / PERSONAL BANKING", ATMTheme.FONT_SMALL, ATMTheme.ACCENT_PRIMARY);
        eyebrow.setFont(ATMTheme.FONT_BODY_BOLD);
        greetingLabel = ATMTheme.createLabel("Welcome back", ATMTheme.FONT_HEADING, ATMTheme.TEXT_PRIMARY);
        cardInfoLabel = ATMTheme.createLabel("Secure session", ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        brand.add(eyebrow);
        brand.add(Box.createVerticalStrut(7));
        brand.add(greetingLabel);
        brand.add(Box.createVerticalStrut(4));
        brand.add(cardInfoLabel);
        top.add(brand, BorderLayout.WEST);

        JPanel trust = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        trust.setOpaque(false);
        trust.add(ATMTheme.createBadge("● ONLINE", ATMTheme.SUCCESS));
        trust.add(ATMTheme.createBadge("EDUCATIONAL SIMULATION", ATMTheme.ACCENT_SECONDARY));
        top.add(trust, BorderLayout.EAST);
        return top;
    }

    private JPanel createDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout(18, 0));
        dashboard.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(createHeroCard());
        left.add(Box.createVerticalStrut(16));
        left.add(createMetricsRow());
        dashboard.add(left, BorderLayout.CENTER);

        dashboard.add(createQuickActions(), BorderLayout.EAST);
        return dashboard;
    }

    private JPanel createHeroCard() {
        JPanel hero = new JPanel(new BorderLayout(18, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, ATMTheme.GRADIENT_START,
                        getWidth(), getHeight(), ATMTheme.BG_CARD);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.setColor(new Color(ATMTheme.ACCENT_PRIMARY.getRed(), ATMTheme.ACCENT_PRIMARY.getGreen(), ATMTheme.ACCENT_PRIMARY.getBlue(), 70));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
            }
        };
        hero.setOpaque(false);
        hero.setBorder(new EmptyBorder(26, 28, 26, 28));
        hero.setPreferredSize(new Dimension(540, 242));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel eyebrow = ATMTheme.createLabel("YOUR FINANCIAL VISIBILITY", ATMTheme.FONT_SMALL, ATMTheme.ACCENT_SECONDARY);
        JLabel title = ATMTheme.createLabel("<html>Everything important,<br>at a glance.</html>", ATMTheme.FONT_TITLE, ATMTheme.TEXT_PRIMARY);
        JLabel description = ATMTheme.createLabel("Track your balance, move money, and keep your account\nunder control from one secure workspace.", ATMTheme.FONT_BODY, ATMTheme.TEXT_SECONDARY);
        copy.add(eyebrow);
        copy.add(Box.createVerticalStrut(12));
        copy.add(title);
        copy.add(Box.createVerticalStrut(12));
        copy.add(description);
        hero.add(copy, BorderLayout.CENTER);

        JPanel balance = new JPanel();
        balance.setOpaque(false);
        balance.setLayout(new BoxLayout(balance, BoxLayout.Y_AXIS));
        JLabel label = ATMTheme.createLabel("AVAILABLE BALANCE", ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        label.setAlignmentX(Component.RIGHT_ALIGNMENT);
        balanceValue = ATMTheme.createLabel("₹—", ATMTheme.FONT_BALANCE, ATMTheme.TEXT_PRIMARY);
        balanceValue.setAlignmentX(Component.RIGHT_ALIGNMENT);
        balanceStatus = ATMTheme.createLabel("Refreshing account", ATMTheme.FONT_SMALL, ATMTheme.ACCENT_PRIMARY);
        balanceStatus.setAlignmentX(Component.RIGHT_ALIGNMENT);
        balance.add(label);
        balance.add(Box.createVerticalStrut(8));
        balance.add(balanceValue);
        balance.add(Box.createVerticalStrut(8));
        balance.add(balanceStatus);
        hero.add(balance, BorderLayout.SOUTH);
        return hero;
    }

    private JPanel createMetricsRow() {
        JPanel metrics = new JPanel(new GridLayout(1, 3, 14, 0));
        metrics.setOpaque(false);
        accountValue = ATMTheme.createLabel("—", ATMTheme.FONT_SUBHEADING, ATMTheme.TEXT_PRIMARY);
        securityValue = ATMTheme.createLabel("Protected", ATMTheme.FONT_SUBHEADING, ATMTheme.SUCCESS);
        metrics.add(createMetricCard("ACCOUNT TYPE", accountValue, "Primary account"));
        metrics.add(createMetricCard("ACCOUNT STATUS", securityValue, "Authentication active"));
        metrics.add(createMetricCard("FAST ACCESS", ATMTheme.createLabel("5 actions", ATMTheme.FONT_SUBHEADING, ATMTheme.TEXT_PRIMARY), "Swipe or press Escape to go back"));
        return metrics;
    }

    private JPanel createMetricCard(String label, JLabel value, String detail) {
        JPanel card = ATMTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 17, 15, 17));
        JLabel heading = ATMTheme.createLabel(label, ATMTheme.FONT_SMALL, ATMTheme.TEXT_MUTED);
        JLabel note = ATMTheme.createLabel(detail, ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(heading);
        card.add(Box.createVerticalStrut(8));
        card.add(value);
        card.add(Box.createVerticalStrut(6));
        card.add(note);
        return card;
    }

    private JPanel createQuickActions() {
        JPanel rail = ATMTheme.createCard();
        rail.setLayout(new BoxLayout(rail, BoxLayout.Y_AXIS));
        rail.setBorder(new EmptyBorder(20, 20, 20, 20));
        rail.setPreferredSize(new Dimension(285, 0));

        JLabel heading = ATMTheme.createLabel("QUICK ACTIONS", ATMTheme.FONT_SMALL, ATMTheme.ACCENT_PRIMARY);
        JLabel subheading = ATMTheme.createLabel("Move with confidence", ATMTheme.FONT_SUBHEADING, ATMTheme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        subheading.setAlignmentX(Component.LEFT_ALIGNMENT);
        rail.add(heading);
        rail.add(Box.createVerticalStrut(5));
        rail.add(subheading);
        rail.add(Box.createVerticalStrut(16));

        addAction(rail, "Balance", "View available funds", "01", MainFrame.SCREEN_BALANCE);
        addAction(rail, "Withdraw", "Take out cash", "02", MainFrame.SCREEN_WITHDRAW);
        addAction(rail, "Deposit", "Add simulated cash", "03", MainFrame.SCREEN_DEPOSIT);
        addAction(rail, "Transfer", "Send between accounts", "04", MainFrame.SCREEN_TRANSFER);
        addAction(rail, "Statement", "Review recent activity", "05", MainFrame.SCREEN_MINI_STATEMENT);

        rail.add(Box.createVerticalGlue());
        JButton logout = ATMTheme.createDangerButton("Log out securely");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        logout.addActionListener(e -> {
            if (ATMTheme.showConfirm(this, "Your current session will be closed.")) mainFrame.logout();
        });
        rail.add(logout);
        return rail;
    }

    private void addAction(JPanel rail, String title, String detail, String number, String screen) {
        JButton action = new JButton() {
            private boolean hovered;
            {
                setLayout(new BorderLayout(12, 0));
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? ATMTheme.BG_CARD_HOVER : ATMTheme.BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(hovered ? ATMTheme.ACCENT_PRIMARY : ATMTheme.BORDER_DEFAULT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        action.setOpaque(false);
        action.setContentAreaFilled(false);
        action.setBorder(new EmptyBorder(10, 12, 10, 10));
        action.setFocusPainted(false);
        action.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel name = ATMTheme.createLabel(title, ATMTheme.FONT_BODY_BOLD, ATMTheme.TEXT_PRIMARY);
        JLabel caption = ATMTheme.createLabel(detail, ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        text.add(name);
        text.add(Box.createVerticalStrut(2));
        text.add(caption);
        JLabel index = ATMTheme.createLabel(number, ATMTheme.FONT_MONO, ATMTheme.ACCENT_PRIMARY);
        action.add(index, BorderLayout.WEST);
        action.add(text, BorderLayout.CENTER);
        action.add(ATMTheme.createLabel("→", ATMTheme.FONT_BODY, ATMTheme.TEXT_SECONDARY), BorderLayout.EAST);
        action.addActionListener(e -> mainFrame.showScreen(screen));
        action.setAlignmentX(Component.LEFT_ALIGNMENT);
        action.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        rail.add(action);
        rail.add(Box.createVerticalStrut(8));
    }

    private JLabel createFooter() {
        JLabel footer = ATMTheme.createLabel("ATM TRANSACTION SYSTEM  /  LOCAL DEMONSTRATION  /  SECURE SESSION", ATMTheme.FONT_SMALL, ATMTheme.TEXT_MUTED);
        footer.setBorder(new EmptyBorder(18, 0, 0, 0));
        return footer;
    }

    @Override
    public void onShow() {
        ATMCard card = mainFrame.getCurrentCard();
        if (card == null) return;
        String customerName = card.getAccount() != null && card.getAccount().getCustomer() != null
                ? card.getAccount().getCustomer().getFullName() : "Customer";
        greetingLabel.setText("Welcome, " + customerName);
        String accountType = card.getAccount() != null ? String.valueOf(card.getAccount().getAccountType()) : "Account";
        String accountNumber = card.getAccount() != null ? card.getAccount().getAccountNumber() : "—";
        cardInfoLabel.setText("Card " + card.getMaskedCardNumber() + "  /  " + accountNumber);
        accountValue.setText(accountType);
        balanceValue.setText("Loading...");
        balanceValue.setForeground(ATMTheme.TEXT_SECONDARY);
        balanceStatus.setText("Refreshing account");

        SwingWorker<BigDecimal, Void> worker = new SwingWorker<>() {
            @Override protected BigDecimal doInBackground() throws Exception {
                return txnService.getBalance(card.getAccountId());
            }
            @Override protected void done() {
                try {
                    BigDecimal balance = get();
                    NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                    balanceValue.setText(format.format(balance));
                    balanceValue.setForeground(ATMTheme.TEXT_PRIMARY);
                    balanceStatus.setText("Available to use now");
                    balanceStatus.setForeground(ATMTheme.SUCCESS);
                } catch (Exception ex) {
                    balanceValue.setText("Unavailable");
                    balanceValue.setForeground(ATMTheme.DANGER);
                    balanceStatus.setText("Could not refresh balance");
                    balanceStatus.setForeground(ATMTheme.DANGER);
                }
            }
        };
        worker.execute();
    }
}

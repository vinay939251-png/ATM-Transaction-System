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
 * Balance Inquiry screen — displays the current account balance.
 * Fetches balance directly from the database on each view.
 */
public class BalancePanel extends JPanel implements MainFrame.Refreshable {

    private final MainFrame mainFrame;
    private final TransactionService txnService = new TransactionService();
    private JLabel balanceLabel;
    private JLabel accountLabel;
    private JLabel statusLabel;

    public BalancePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setOpaque(true);
        setBackground(ATMTheme.BG_PRIMARY);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel card = ATMTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(460, 380));

        // Header
        JPanel header = ATMTheme.createGradientHeader("Balance Inquiry", "Your current account balance");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.add(header);
        card.add(Box.createVerticalStrut(30));

        // Account info
        accountLabel = ATMTheme.createLabel("", ATMTheme.FONT_BODY, ATMTheme.TEXT_SECONDARY);
        accountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(accountLabel);
        card.add(Box.createVerticalStrut(12));

        // Balance display
        JLabel currencyLabel = ATMTheme.createLabel("Available Balance", ATMTheme.FONT_SMALL, ATMTheme.TEXT_MUTED);
        currencyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(currencyLabel);
        card.add(Box.createVerticalStrut(8));

        balanceLabel = ATMTheme.createLabel("₹0.00", ATMTheme.FONT_BALANCE, ATMTheme.ACCENT_PRIMARY);
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(balanceLabel);
        card.add(Box.createVerticalStrut(8));

        statusLabel = ATMTheme.createLabel("", ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(24));

        // Back button
        JButton backBtn = ATMTheme.createSecondaryButton("← Back to Menu");
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_MAIN_MENU));
        card.add(backBtn);

        add(card);
    }

    @Override
    public void onShow() {
        ATMCard card = mainFrame.getCurrentCard();
        if (card == null) return;

        accountLabel.setText(card.getAccount().getAccountNumber() + " • " + card.getAccount().getAccountType());
        balanceLabel.setText("Loading...");
        balanceLabel.setForeground(ATMTheme.TEXT_SECONDARY);
        statusLabel.setText("");

        SwingWorker<BigDecimal, Void> worker = new SwingWorker<>() {
            @Override
            protected BigDecimal doInBackground() throws Exception {
                return txnService.getBalance(card.getAccountId());
            }

            @Override
            protected void done() {
                try {
                    BigDecimal balance = get();
                    if (balance != null) {
                        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                        balanceLabel.setText(fmt.format(balance));
                        balanceLabel.setForeground(ATMTheme.ACCENT_PRIMARY);
                        statusLabel.setText("Balance retrieved successfully");
                        statusLabel.setForeground(ATMTheme.SUCCESS);
                    } else {
                        balanceLabel.setText("Error");
                        balanceLabel.setForeground(ATMTheme.DANGER);
                        statusLabel.setText("Could not retrieve balance");
                    }
                } catch (Exception ex) {
                    balanceLabel.setText("Error");
                    balanceLabel.setForeground(ATMTheme.DANGER);
                    statusLabel.setText("Connection error. Please try again.");
                    statusLabel.setForeground(ATMTheme.DANGER);
                }
            }
        };
        worker.execute();
    }
}

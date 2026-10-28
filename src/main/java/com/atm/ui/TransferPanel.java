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
 * Fund Transfer screen — atomic two-sided transfer between accounts.
 */
public class TransferPanel extends JPanel implements MainFrame.Refreshable {

    private final MainFrame mainFrame;
    private final TransactionService txnService = new TransactionService();
    private JTextField destField;
    private JTextField amountField;
    private JLabel statusLabel;
    private JLabel balanceLabel;

    public TransferPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setOpaque(true);
        setBackground(ATMTheme.BG_PRIMARY);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel card = ATMTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(460, 480));

        JPanel header = ATMTheme.createGradientHeader("Fund Transfer", "Transfer money to another account");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.add(header);
        card.add(Box.createVerticalStrut(16));

        balanceLabel = ATMTheme.createLabel("Available: Loading...", ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        balanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(balanceLabel);
        card.add(Box.createVerticalStrut(16));

        // Destination account
        JLabel destLabel = ATMTheme.createLabel("Destination Account Number", ATMTheme.FONT_SMALL, ATMTheme.TEXT_MUTED);
        destLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(destLabel);
        card.add(Box.createVerticalStrut(6));

        destField = ATMTheme.createTextField("Enter account number");
        destField.setAlignmentX(Component.LEFT_ALIGNMENT);
        destField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        card.add(destField);
        card.add(Box.createVerticalStrut(12));

        // Amount
        JLabel amtLabel = ATMTheme.createLabel("Transfer Amount", ATMTheme.FONT_SMALL, ATMTheme.TEXT_MUTED);
        amtLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(amtLabel);
        card.add(Box.createVerticalStrut(6));

        amountField = ATMTheme.createTextField("Enter amount");
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        card.add(amountField);
        card.add(Box.createVerticalStrut(16));

        JButton transferBtn = ATMTheme.createPrimaryButton("Transfer Now");
        transferBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        transferBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        transferBtn.addActionListener(e -> processTransfer());
        card.add(transferBtn);
        card.add(Box.createVerticalStrut(10));

        statusLabel = ATMTheme.createLabel(" ", ATMTheme.FONT_BODY, ATMTheme.TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(12));

        JButton backBtn = ATMTheme.createSecondaryButton("← Back to Menu");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        backBtn.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_MAIN_MENU));
        card.add(backBtn);

        add(card);
    }

    private void processTransfer() {
        ATMCard card = mainFrame.getCurrentCard();
        if (card == null) return;

        String dest = destField.getText().trim();
        if (dest.isEmpty()) {
            statusLabel.setForeground(ATMTheme.DANGER);
            statusLabel.setText("Please enter a destination account number.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
        } catch (NumberFormatException e) {
            statusLabel.setForeground(ATMTheme.DANGER);
            statusLabel.setText("Please enter a valid amount.");
            return;
        }

        // Confirmation
        if (!ATMTheme.showConfirm(this,
                "Transfer ₹" + amount.toPlainString() + " to " + dest + "?")) {
            return;
        }

        statusLabel.setForeground(ATMTheme.TEXT_SECONDARY);
        statusLabel.setText("Processing transfer...");

        SwingWorker<TransactionService.TransactionResult, Void> worker = new SwingWorker<>() {
            @Override
            protected TransactionService.TransactionResult doInBackground() {
                return txnService.transfer(card, dest, amount);
            }

            @Override
            protected void done() {
                try {
                    var result = get();
                    if (result.success()) {
                        statusLabel.setForeground(ATMTheme.SUCCESS);
                        statusLabel.setText(result.message());
                        destField.setText("");
                        amountField.setText("");
                        updateBalance(result.newBalance());
                        ATMTheme.showSuccess(TransferPanel.this,
                                result.message() + "\nNew Balance: ₹" + result.newBalance().toPlainString() +
                                "\nRef: " + result.transactionRef());
                    } else {
                        statusLabel.setForeground(ATMTheme.DANGER);
                        statusLabel.setText(result.message());
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(ATMTheme.DANGER);
                    statusLabel.setText("System error. Please try again.");
                }
            }
        };
        worker.execute();
    }

    private void updateBalance(BigDecimal balance) {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        balanceLabel.setText("Available: " + fmt.format(balance));
    }

    @Override
    public void onShow() {
        ATMCard card = mainFrame.getCurrentCard();
        if (card == null) return;
        statusLabel.setText(" ");
        destField.setText("");
        amountField.setText("");

        SwingWorker<BigDecimal, Void> w = new SwingWorker<>() {
            @Override
            protected BigDecimal doInBackground() throws Exception {
                return txnService.getBalance(card.getAccountId());
            }

            @Override
            protected void done() {
                try { BigDecimal b = get(); if (b != null) updateBalance(b); } catch (Exception ignored) {}
            }
        };
        w.execute();
    }
}

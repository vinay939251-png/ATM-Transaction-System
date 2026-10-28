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
 * Cash Withdrawal screen.
 * Supports quick-select amounts and custom amount entry.
 */
public class WithdrawPanel extends JPanel implements MainFrame.Refreshable {

    private final MainFrame mainFrame;
    private final TransactionService txnService = new TransactionService();
    private JTextField amountField;
    private JLabel statusLabel;
    private JLabel balanceLabel;

    public WithdrawPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setOpaque(true);
        setBackground(ATMTheme.BG_PRIMARY);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel card = ATMTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(500, 550));

        // Header
        JPanel header = ATMTheme.createGradientHeader("Cash Withdrawal", "Select or enter the amount to withdraw");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.add(header);
        card.add(Box.createVerticalStrut(20));

        // Balance display
        balanceLabel = ATMTheme.createLabel("Available: Loading...", ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        balanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(balanceLabel);
        card.add(Box.createVerticalStrut(16));

        // Quick amount buttons
        JLabel quickLabel = ATMTheme.createLabel("Quick Select", ATMTheme.FONT_SMALL, ATMTheme.TEXT_MUTED);
        quickLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(quickLabel);
        card.add(Box.createVerticalStrut(8));

        JPanel quickGrid = new JPanel(new GridLayout(2, 3, 10, 10));
        quickGrid.setOpaque(false);
        quickGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        quickGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        int[] quickAmounts = {500, 1000, 2000, 5000, 10000, 25000};
        for (int amt : quickAmounts) {
            JButton qBtn = ATMTheme.createSecondaryButton("₹" + NumberFormat.getIntegerInstance().format(amt));
            qBtn.setPreferredSize(new Dimension(120, 40));
            qBtn.addActionListener(e -> {
                amountField.setText(String.valueOf(amt));
                processWithdrawal(new BigDecimal(amt));
            });
            quickGrid.add(qBtn);
        }
        card.add(quickGrid);
        card.add(Box.createVerticalStrut(20));

        // Custom amount
        JLabel customLabel = ATMTheme.createLabel("Or Enter Amount", ATMTheme.FONT_SMALL, ATMTheme.TEXT_MUTED);
        customLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(customLabel);
        card.add(Box.createVerticalStrut(6));

        amountField = ATMTheme.createTextField("Enter amount (multiple of ₹100)");
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        card.add(amountField);
        card.add(Box.createVerticalStrut(12));

        JButton withdrawBtn = ATMTheme.createPrimaryButton("Withdraw");
        withdrawBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        withdrawBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        withdrawBtn.addActionListener(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                processWithdrawal(amount);
            } catch (NumberFormatException ex) {
                statusLabel.setForeground(ATMTheme.DANGER);
                statusLabel.setText("Please enter a valid amount.");
            }
        });
        card.add(withdrawBtn);
        card.add(Box.createVerticalStrut(10));

        // Status
        statusLabel = ATMTheme.createLabel(" ", ATMTheme.FONT_BODY, ATMTheme.TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(12));

        // Back
        JButton backBtn = ATMTheme.createSecondaryButton("← Back to Menu");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        backBtn.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_MAIN_MENU));
        card.add(backBtn);

        add(card);
    }

    private void processWithdrawal(BigDecimal amount) {
        ATMCard card = mainFrame.getCurrentCard();
        if (card == null) return;

        statusLabel.setForeground(ATMTheme.TEXT_SECONDARY);
        statusLabel.setText("Processing withdrawal...");

        SwingWorker<TransactionService.TransactionResult, Void> worker = new SwingWorker<>() {
            @Override
            protected TransactionService.TransactionResult doInBackground() {
                return txnService.withdraw(card, amount);
            }

            @Override
            protected void done() {
                try {
                    var result = get();
                    if (result.success()) {
                        statusLabel.setForeground(ATMTheme.SUCCESS);
                        statusLabel.setText(result.message());
                        amountField.setText("");
                        updateBalance(result.newBalance());
                        ATMTheme.showSuccess(WithdrawPanel.this,
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
        amountField.setText("");

        SwingWorker<BigDecimal, Void> worker = new SwingWorker<>() {
            @Override
            protected BigDecimal doInBackground() throws Exception {
                return txnService.getBalance(card.getAccountId());
            }

            @Override
            protected void done() {
                try {
                    BigDecimal bal = get();
                    if (bal != null) updateBalance(bal);
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }
}

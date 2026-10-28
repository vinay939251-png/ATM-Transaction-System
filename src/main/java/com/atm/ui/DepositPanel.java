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
 * Cash Deposit screen — simulated deposit operation.
 */
public class DepositPanel extends JPanel implements MainFrame.Refreshable {

    private final MainFrame mainFrame;
    private final TransactionService txnService = new TransactionService();
    private JTextField amountField;
    private JLabel statusLabel;
    private JLabel balanceLabel;

    public DepositPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setOpaque(true);
        setBackground(ATMTheme.BG_PRIMARY);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel card = ATMTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(460, 420));

        JPanel header = ATMTheme.createGradientHeader("Cash Deposit", "This is a simulated deposit operation");
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.add(header);
        card.add(Box.createVerticalStrut(20));

        balanceLabel = ATMTheme.createLabel("Current Balance: Loading...", ATMTheme.FONT_SMALL, ATMTheme.TEXT_SECONDARY);
        balanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(balanceLabel);
        card.add(Box.createVerticalStrut(16));

        JLabel amtLabel = ATMTheme.createLabel("Deposit Amount", ATMTheme.FONT_SMALL, ATMTheme.TEXT_MUTED);
        amtLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(amtLabel);
        card.add(Box.createVerticalStrut(6));

        amountField = ATMTheme.createTextField("Enter amount (multiple of ₹100)");
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        card.add(amountField);
        card.add(Box.createVerticalStrut(16));

        JButton depositBtn = ATMTheme.createPrimaryButton("Deposit");
        depositBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        depositBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        depositBtn.addActionListener(e -> processDeposit());
        card.add(depositBtn);
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

    private void processDeposit() {
        ATMCard card = mainFrame.getCurrentCard();
        if (card == null) return;

        String amtStr = amountField.getText().trim();
        BigDecimal amount;
        try {
            amount = new BigDecimal(amtStr);
        } catch (NumberFormatException e) {
            statusLabel.setForeground(ATMTheme.DANGER);
            statusLabel.setText("Please enter a valid amount.");
            return;
        }

        statusLabel.setForeground(ATMTheme.TEXT_SECONDARY);
        statusLabel.setText("Processing deposit...");

        SwingWorker<TransactionService.TransactionResult, Void> worker = new SwingWorker<>() {
            @Override
            protected TransactionService.TransactionResult doInBackground() {
                return txnService.deposit(card, amount);
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
                        ATMTheme.showSuccess(DepositPanel.this,
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
        balanceLabel.setText("Current Balance: " + fmt.format(balance));
    }

    @Override
    public void onShow() {
        ATMCard card = mainFrame.getCurrentCard();
        if (card == null) return;
        statusLabel.setText(" ");
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

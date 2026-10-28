package com.atm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a single ATM transaction record.
 */
public class Transaction {

    public enum TransactionType { WITHDRAWAL, DEPOSIT, TRANSFER_DEBIT, TRANSFER_CREDIT }
    public enum TransactionStatus { SUCCESS, FAILED, PENDING }

    private int transactionId;
    private String transactionRef;
    private int accountId;
    private Integer cardId;           // Nullable for system/admin transactions
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private TransactionStatus status;
    private String description;
    private String transferRef;       // Shared reference for transfer pairs
    private LocalDateTime transactionDate;

    public Transaction() {
        this.status = TransactionStatus.SUCCESS;
    }

    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public Integer getCardId() { return cardId; }
    public void setCardId(Integer cardId) { this.cardId = cardId; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTransferRef() { return transferRef; }
    public void setTransferRef(String transferRef) { this.transferRef = transferRef; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    /** Returns a display-friendly type string */
    public String getTypeDisplay() {
        return switch (transactionType) {
            case WITHDRAWAL -> "Withdrawal";
            case DEPOSIT -> "Deposit";
            case TRANSFER_DEBIT -> "Transfer (Sent)";
            case TRANSFER_CREDIT -> "Transfer (Received)";
        };
    }

    /** Returns +/- prefix for amount display */
    public String getAmountDisplay() {
        String sign = switch (transactionType) {
            case DEPOSIT, TRANSFER_CREDIT -> "+";
            case WITHDRAWAL, TRANSFER_DEBIT -> "-";
        };
        return sign + "₹" + amount.toPlainString();
    }

    @Override
    public String toString() {
        return String.format("Transaction[%s: %s ₹%s, %s]",
                transactionRef, transactionType, amount, status);
    }
}

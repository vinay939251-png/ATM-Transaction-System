package com.atm.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents an ATM card linked to a bank account.
 * PIN is stored as a BCrypt hash — never in plaintext.
 */
public class ATMCard {

    public enum CardStatus { ACTIVE, BLOCKED, EXPIRED }

    private int cardId;
    private String cardNumber;
    private int accountId;
    private String pinHash;          // BCrypt hash — never expose
    private CardStatus cardStatus;
    private LocalDate expiryDate;
    private LocalDate issuedDate;
    private int failedAttempts;
    private LocalDateTime blockedUntil;
    private BigDecimal dailyWithdrawalTotal;
    private LocalDate lastWithdrawalDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient — loaded from JOIN
    private Account account;

    public ATMCard() {
        this.cardStatus = CardStatus.ACTIVE;
        this.failedAttempts = 0;
        this.dailyWithdrawalTotal = BigDecimal.ZERO;
    }

    // Getters and Setters
    public int getCardId() { return cardId; }
    public void setCardId(int cardId) { this.cardId = cardId; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    /** Returns masked card number like ****-****-****-9010 */
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public CardStatus getCardStatus() { return cardStatus; }
    public void setCardStatus(CardStatus cardStatus) { this.cardStatus = cardStatus; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public LocalDate getIssuedDate() { return issuedDate; }
    public void setIssuedDate(LocalDate issuedDate) { this.issuedDate = issuedDate; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public LocalDateTime getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(LocalDateTime blockedUntil) { this.blockedUntil = blockedUntil; }

    public BigDecimal getDailyWithdrawalTotal() { return dailyWithdrawalTotal; }
    public void setDailyWithdrawalTotal(BigDecimal dailyWithdrawalTotal) { this.dailyWithdrawalTotal = dailyWithdrawalTotal; }

    public LocalDate getLastWithdrawalDate() { return lastWithdrawalDate; }
    public void setLastWithdrawalDate(LocalDate lastWithdrawalDate) { this.lastWithdrawalDate = lastWithdrawalDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    /** Check if the card is usable for transactions */
    public boolean isUsable() {
        if (cardStatus != CardStatus.ACTIVE) return false;
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) return false;
        if (blockedUntil != null && blockedUntil.isAfter(LocalDateTime.now())) return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("ATMCard[%s, Status: %s]", getMaskedCardNumber(), cardStatus);
    }
}

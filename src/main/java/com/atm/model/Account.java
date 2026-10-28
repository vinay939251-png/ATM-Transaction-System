package com.atm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a bank account linked to a customer.
 */
public class Account {

    public enum AccountType { SAVINGS, CURRENT }
    public enum AccountStatus { ACTIVE, INACTIVE, CLOSED }

    private int accountId;
    private String accountNumber;
    private int customerId;
    private AccountType accountType;
    private BigDecimal balance;
    private BigDecimal minBalance;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient — loaded from JOIN, not persisted directly
    private Customer customer;

    public Account() {
        this.balance = BigDecimal.ZERO;
        this.minBalance = new BigDecimal("1000.00");
        this.status = AccountStatus.ACTIVE;
        this.accountType = AccountType.SAVINGS;
    }

    // Getters and Setters
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getMinBalance() { return minBalance; }
    public void setMinBalance(BigDecimal minBalance) { this.minBalance = minBalance; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    /**
     * Returns the withdrawable amount considering minimum balance requirement.
     */
    public BigDecimal getWithdrawableAmount() {
        BigDecimal available = balance.subtract(minBalance);
        return available.compareTo(BigDecimal.ZERO) > 0 ? available : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return String.format("Account[%s, %s, Balance: ₹%s]", accountNumber, accountType, balance);
    }
}

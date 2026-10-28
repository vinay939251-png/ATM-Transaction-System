package com.atm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a fund transfer between two accounts.
 */
public class Transfer {

    public enum TransferStatus { SUCCESS, FAILED, PENDING }

    private int transferId;
    private String transferRef;
    private int sourceAccountId;
    private int destAccountId;
    private BigDecimal amount;
    private TransferStatus status;
    private String description;
    private LocalDateTime transferDate;

    // Transient — for display
    private String sourceAccountNumber;
    private String destAccountNumber;

    public Transfer() {
        this.status = TransferStatus.SUCCESS;
    }

    // Getters and Setters
    public int getTransferId() { return transferId; }
    public void setTransferId(int transferId) { this.transferId = transferId; }

    public String getTransferRef() { return transferRef; }
    public void setTransferRef(String transferRef) { this.transferRef = transferRef; }

    public int getSourceAccountId() { return sourceAccountId; }
    public void setSourceAccountId(int sourceAccountId) { this.sourceAccountId = sourceAccountId; }

    public int getDestAccountId() { return destAccountId; }
    public void setDestAccountId(int destAccountId) { this.destAccountId = destAccountId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransferStatus getStatus() { return status; }
    public void setStatus(TransferStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDateTime transferDate) { this.transferDate = transferDate; }

    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }

    public String getDestAccountNumber() { return destAccountNumber; }
    public void setDestAccountNumber(String destAccountNumber) { this.destAccountNumber = destAccountNumber; }

    @Override
    public String toString() {
        return String.format("Transfer[%s: ₹%s from %d to %d, %s]",
                transferRef, amount, sourceAccountId, destAccountId, status);
    }
}

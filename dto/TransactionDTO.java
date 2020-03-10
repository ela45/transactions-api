package com.app.dto;



import com.app.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {

    private BigDecimal amount;
    private LocalDateTime timestamp;

    public TransactionDTO() {
    }

    public TransactionDTO(Transaction transaction) {
        this.amount = transaction.getAmount();
        this.timestamp=transaction.getDateTime();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

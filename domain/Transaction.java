package com.app.domain;

import com.app.dto.TransactionDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
	private BigDecimal amount;
	private LocalDateTime dateTime;

	public Transaction(TransactionDTO transactionDTO) {
		this.amount = transactionDTO.getAmount();
		this.dateTime=transactionDTO.getTimestamp();
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Transaction(BigDecimal amount, LocalDateTime dateTime) {
		this.amount = amount;
		this.dateTime = dateTime;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
}

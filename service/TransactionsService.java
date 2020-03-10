package com.app.service;

import com.app.dto.StatisticsDTO;
import com.app.dto.TransactionDTO;
import com.app.exceptions.NotFoundException;
import com.app.exceptions.UnprocessableException;

public interface TransactionsService {
    StatisticsDTO getStatistics();
    void saveTransaction(TransactionDTO transactionDTO) throws NotFoundException, UnprocessableException;
    void deleteAllTransactions();

}

package com.app.service;

import com.app.domain.Transaction;
import com.app.dto.StatisticsDTO;
import com.app.dto.TransactionDTO;
import com.app.exceptions.NotFoundException;
import com.app.exceptions.UnprocessableException;
import com.app.utils.Constants;
import com.app.utils.ValidationUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionsServiceImpl implements TransactionsService {

    private static final org.slf4j.Logger LOGGER= LoggerFactory.getLogger(TransactionsServiceImpl.class);


    private List<Transaction> transactions = new ArrayList<Transaction>();


    @Override
    public StatisticsDTO getStatistics() {

        BigDecimal sum = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        BigDecimal avg = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        BigDecimal max = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        BigDecimal min = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
        Long count = 0L;

        /* GET LAST 60 seg.*/
        LocalDateTime currentDateTime = ValidationUtils.getUTCLocalDateTime();
        LocalDateTime limitDateTime = currentDateTime.minusSeconds(Constants.LAST_SECONDS_NUMBER);
        LOGGER.debug("LIMIT CURRENT DATE TIME {}",limitDateTime);
        LOGGER.debug("CURRENT DATE TIME {}",currentDateTime);

        /* GET VALID TRANSACTIONS valid transaction= from 60 seg. to current time */
        List<Transaction> validTransactions = transactions.stream().
                filter(tran -> tran.getDateTime().isAfter(limitDateTime) && tran.getDateTime().isBefore(currentDateTime))
                .collect(Collectors.toList());

        /* DELETE NO VALID TRANSACTIONS */
        List<Transaction> transactionsToDelete=new ArrayList<>(transactions);
        transactionsToDelete.removeAll(validTransactions);
        transactions.removeAll(transactionsToDelete);

        if (Objects.nonNull(validTransactions) && !validTransactions.isEmpty()) {
            sum = new BigDecimal(validTransactions.stream().mapToDouble(trans -> trans.getAmount().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP);
            avg = new BigDecimal(validTransactions.stream().mapToDouble(trans -> trans.getAmount().doubleValue()).average().getAsDouble()).setScale(2, RoundingMode.HALF_UP);
            max = new BigDecimal(validTransactions.stream().mapToDouble(trans -> trans.getAmount().doubleValue()).max().getAsDouble()).setScale(2, RoundingMode.HALF_UP);
            min = new BigDecimal(validTransactions.stream().mapToDouble(trans -> trans.getAmount().doubleValue()).min().getAsDouble()).setScale(2, RoundingMode.HALF_UP);
            count = Long.valueOf(validTransactions.size());
        }

        return new StatisticsDTO(sum.toString(), avg.toString(), max.toString(), min.toString(), count);

    }

    @Override
    public void saveTransaction(TransactionDTO transactionDTO) throws NotFoundException, UnprocessableException {
        ValidationUtils.validateTransaction(transactionDTO);
        transactions.add(new Transaction(transactionDTO));
    }

    @Override
    public void deleteAllTransactions() {
        transactions.clear();
    }
}

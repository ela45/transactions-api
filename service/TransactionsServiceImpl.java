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

        BigDecimal sum = new BigDecimal(0);
        BigDecimal avg = new BigDecimal(0);
        BigDecimal max = new BigDecimal(0);
        BigDecimal min = new BigDecimal(0);
        Long count = 0L;

        /* GET LAST 60 seg.*/
        LocalDateTime currentDateTime = ValidationUtils.getUTCLocalDateTime();
        LocalDateTime limitDateTime = currentDateTime.minusSeconds(Constants.LAST_SECONDS_NUMBER);
        LOGGER.debug("LIMIT CURRENT DATE TIME {}",limitDateTime);
        LOGGER.debug("CURRENT DATE TIME {}",currentDateTime);

        /* GET VALID TRANSACTIONS valid transaction= from 60 seg. to current time */
        if(Objects.nonNull(transactions) && !transactions.isEmpty()) {
            int i = 0;
            while (i < transactions.size() && transactions.get(i).getDateTime().isAfter(limitDateTime) &&
                    transactions.get(i).getDateTime().isBefore(currentDateTime)) {

                sum = sum.add(transactions.get(i).getAmount());
                max = max.max(transactions.get(i).getAmount());
                if (i == 0) {
                    min = transactions.get(i).getAmount();
                } else {
                    min = min.min(transactions.get(i).getAmount());
                }
                i++;
            }

            count = Long.valueOf(i);
            BigDecimal divide = new BigDecimal(count);
            avg = sum.divide(divide,2,RoundingMode.HALF_UP);

            /* DELETE NO VALID TRANSACTIONS */
            transactions=transactions.subList(0,i-1);
        }

        return new StatisticsDTO(sum.setScale(2,RoundingMode.HALF_UP).toString(), avg.setScale(2,RoundingMode.HALF_UP).toString(),
                max.setScale(2,RoundingMode.HALF_UP).toString(), min.setScale(2,RoundingMode.HALF_UP).toString(), count);

    }

    @Override
    public void saveTransaction(TransactionDTO transactionDTO) throws NotFoundException, UnprocessableException {
        ValidationUtils.validateTransaction(transactionDTO);
        transactions.add(new Transaction(transactionDTO));
        //sort the list
        transactions=transactions.stream().sorted(Comparator.comparing(Transaction::getDateTime).reversed()).collect(Collectors.toList());
    }

    @Override
    public void deleteAllTransactions() {
        transactions.clear();
    }
}

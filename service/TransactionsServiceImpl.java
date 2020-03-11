package com.app.service;

import com.app.domain.Transaction;
import com.app.dto.StatisticsDTO;
import com.app.dto.TransactionDTO;
import com.app.exceptions.NotFoundException;
import com.app.exceptions.UnprocessableException;
import com.app.utils.Constants;
import com.app.utils.ValidationUtils;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionsServiceImpl implements TransactionsService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TransactionsServiceImpl.class);
    private static final List<Transaction> transactions = new ArrayList<Transaction>();
    private static final StatisticsDTO statisticsDTO = new StatisticsDTO();

    @Override
    public StatisticsDTO getStatistics() {
        //calculateStatistics();
        return statisticsDTO;
    }

    @Override
    public void saveTransaction(TransactionDTO transactionDTO) throws NotFoundException, UnprocessableException {
        ValidationUtils.validateTransaction(transactionDTO);
        //   synchronized (this) {
        transactions.add(new Transaction(transactionDTO));
        calculateStatistics();
        //    }

    }

    private void calculateStatistics() {
        BigDecimal sum = new BigDecimal(0);
        BigDecimal avg = new BigDecimal(0);
        BigDecimal max = new BigDecimal(0);
        BigDecimal min = new BigDecimal(0);
        Long count = 0L;

        /* GET LAST 60 seg.*/
        LocalDateTime currentDateTime = ValidationUtils.getUTCLocalDateTime();
        LocalDateTime limitDateTime = currentDateTime.minusSeconds(Constants.LAST_SECONDS_NUMBER);
        LOGGER.info("LIMIT CURRENT DATE TIME {}", limitDateTime);
        LOGGER.info("CURRENT DATE TIME {}", currentDateTime);

        /* GET VALID TRANSACTIONS valid transaction= from 60 seg. to current time */

        int i=0;
        synchronized (this) {
            if (Objects.nonNull(transactions) && !transactions.isEmpty()) {
                Iterator<Transaction> iterator = transactions.iterator();

                while (iterator.hasNext()) {
                    Transaction trans = iterator.next();

                    if (trans.getDateTime().isAfter(limitDateTime) &&
                            trans.getDateTime().isBefore(currentDateTime)) {
                        sum = sum.add(trans.getAmount());
                        max = max.max(trans.getAmount());
                        if (i == 0) {
                            min = trans.getAmount();
                        } else {
                            min = min.min(trans.getAmount());
                        }
                        i++;
                    //    LOGGER.info("calculating statistic transaction {} i {} ", trans.getDateTime(), i);
                    } else {
                        //REMOVE OLD TRANSACTIONS
                        LOGGER.info("Deleting old transaction " + trans.getDateTime());
                        iterator.remove();
                    }

                }
                if (i > 0) {
                    count = Long.valueOf(i);
                    BigDecimal divide = new BigDecimal(count);
                    avg = sum.divide(divide, 2, RoundingMode.HALF_UP);
                }


            }
            LOGGER.info("COUNT!!!!!!!!!!!!!!!!!!!!!!!!! "+i);
            statisticsDTO.setSum(sum.setScale(2, RoundingMode.HALF_UP).toString());
            statisticsDTO.setAvg(avg.setScale(2, RoundingMode.HALF_UP).toString());
            statisticsDTO.setMax(max.setScale(2, RoundingMode.HALF_UP).toString());
            statisticsDTO.setMin(min.setScale(2, RoundingMode.HALF_UP).toString());
            statisticsDTO.setCount(count);
        }
    }

    @Override
    public void deleteAllTransactions() {
        transactions.clear();
    }

    @Scheduled(fixedRate = 1000)
    public void executeCalculateTransactions() {
        System.out.println("Calcular estatistics..."+LocalDateTime.now());
       // synchronized (this) {
            calculateStatistics();
      //  }
    }
}

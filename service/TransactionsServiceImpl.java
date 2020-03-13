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
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class TransactionsServiceImpl implements TransactionsService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TransactionsServiceImpl.class);
    private static final Collection<Transaction> transactions = Collections.synchronizedCollection(new ArrayList<Transaction>());
    private static final StatisticsDTO statisticsDTO = new StatisticsDTO();

    @Override
    public synchronized StatisticsDTO getStatistics() {

        return statisticsDTO;
    }

    @Override
    public void saveTransaction(TransactionDTO transactionDTO) throws NotFoundException, UnprocessableException {
        try {
            ValidationUtils.validateTransaction(transactionDTO);
            synchronized (this) {
                transactions.add(new Transaction(transactionDTO));

            }
        } catch (UnprocessableException e) {
            throw new UnprocessableException();
        } catch (NotFoundException e) {
            throw new NotFoundException();
        } finally {
            calculateStatistics();
        }


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
        LOGGER.debug("LIMIT CURRENT DATE TIME {} size list {} ", limitDateTime, transactions.size());

        /* GET VALID TRANSACTIONS valid transaction= from 60 seg. to current time */


        synchronized (this) {
            AtomicInteger i = new AtomicInteger(0);
            if (Objects.nonNull(transactions) && !transactions.isEmpty()) {
                Iterator<Transaction> iterator = transactions.iterator();

                while (iterator.hasNext()) {
                    Transaction trans = iterator.next();

                    sum = sum.add(trans.getAmount());
                    max = max.max(trans.getAmount());
                    if (i.get() == 0) {
                        min = trans.getAmount();
                    } else {
                        min = min.min(trans.getAmount());
                    }
                    i.incrementAndGet();

                }
                if (i.get() > 0) {
                    count = Long.valueOf(i.get());
                    BigDecimal divide = new BigDecimal(count);
                    avg = sum.divide(divide, 2, RoundingMode.HALF_UP);
                }


            }

            statisticsDTO.setSum(sum.setScale(2, RoundingMode.HALF_UP).toString());
            statisticsDTO.setAvg(avg.setScale(2, RoundingMode.HALF_UP).toString());
            statisticsDTO.setMax(max.setScale(2, RoundingMode.HALF_UP).toString());
            statisticsDTO.setMin(min.setScale(2, RoundingMode.HALF_UP).toString());
            statisticsDTO.setCount(count);
        }
    }

    @Override
    public synchronized void deleteAllTransactions() {
        transactions.clear();
    }

    @Scheduled(cron = "* * * * * ?")
    public void executeCalculateTransactions() {
        LOGGER.info("Delete transactions...");
        removeOldTransactions();
        calculateStatistics();


    }

    public synchronized void removeOldTransactions() {
        LocalDateTime currentDateTime = ValidationUtils.getUTCLocalDateTime();
        LocalDateTime limitDateTime = currentDateTime.minusSeconds(Constants.LAST_SECONDS_NUMBER);

        LOGGER.debug("REMOVE OLD limit date {} size! list {} ", limitDateTime, transactions.size());

        transactions.removeIf(transaction -> (transaction.getDateTime().isBefore(limitDateTime) ||
                (transaction.getDateTime().getHour() == limitDateTime.getHour() &&
                        transaction.getDateTime().getMinute() == limitDateTime.getMinute() &&
                        transaction.getDateTime().getSecond() == limitDateTime.getSecond())));

    }
}

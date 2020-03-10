package com.app.utils;

import com.app.dto.TransactionDTO;
import com.app.exceptions.NotFoundException;
import com.app.exceptions.UnprocessableException;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.TimeZone;

public class ValidationUtils {
    private static final org.slf4j.Logger LOGGER= LoggerFactory.getLogger(ValidationUtils.class);

    private LocalDateTime currentDateTime;

    public static void validateTransaction(TransactionDTO transactionDTO) throws UnprocessableException, NotFoundException {
        LocalDateTime currentDateTime = getUTCLocalDateTime();
        LocalDateTime limitDateTime = currentDateTime.minusSeconds(Constants.LAST_SECONDS_NUMBER);
        LOGGER.debug("LIMIT DATE TIME {}",limitDateTime);
        LOGGER.debug("CURRENT DATE TIME {}",currentDateTime);
        if (transactionDTO.getTimestamp().isBefore(limitDateTime)) {
            throw new NotFoundException();
        } else if (transactionDTO.getTimestamp().isAfter(currentDateTime)) {
            throw new UnprocessableException();
        }

    }

    public static LocalDateTime getUTCLocalDateTime() {
        TimeZone timeZone = TimeZone.getTimeZone(Constants.UTC_TIME_ZONE);
        Calendar calendar = Calendar.getInstance();
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(calendar.toInstant(), timeZone.toZoneId());
        LOGGER.debug("Current time {}",calendar);
        LOGGER.debug("Current date time in UTC zone {} ",currentDateTime);
        return currentDateTime;
    }
}

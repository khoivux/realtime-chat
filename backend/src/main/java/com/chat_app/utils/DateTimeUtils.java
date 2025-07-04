package com.chat_app.utils;

import com.chat_app.constant.ErrorCode;
import com.chat_app.exception.custom.AppException;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import java.util.*;

@Component
public class DateTimeUtils {
    Map<Long, Function<Instant, String>> strategyMap = new LinkedHashMap<>();

    public DateTimeUtils() {
        strategyMap.put(60L, this::formatInSeconds);
        strategyMap.put(3600L, this::formatInMinutes);
        strategyMap.put(86400L, this::formatInHours);
        strategyMap.put(Long.MAX_VALUE, this::formatInDate);
    }

    public String format(Instant instant){
        if(instant == null) return null;
        long elapseSeconds = ChronoUnit.SECONDS.between(instant, Instant.now());
        var strategy = strategyMap.entrySet()
                .stream()
                .filter(longFunctionEntry -> elapseSeconds < longFunctionEntry.getKey())
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No suitable strategy found"));
        return strategy.getValue().apply(instant);
    }

    private String formatInSeconds(Instant instant){
        long elapseSeconds = ChronoUnit.SECONDS.between(instant, Instant.now());
        return String.format("%s giây trước", elapseSeconds);
    }

    private String formatInMinutes(Instant instant){
        long elapseMinutes = ChronoUnit.MINUTES.between(instant, Instant.now());
        return String.format("%s phút trước", elapseMinutes);
    }

    private String formatInHours(Instant instant){
        long elapseHours = ChronoUnit.HOURS.between(instant, Instant.now());
        return String.format("%s tiếng trước", elapseHours);
    }

    private String formatInDate(Instant instant){
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        java.time.format.DateTimeFormatter dateTimeFormatter = java.time.format.DateTimeFormatter
                .ofPattern(" HH:mm EEEE, dd-MM-yyyy", Locale.forLanguageTag("vi-VN"));

        return localDateTime.format(dateTimeFormatter);
    }

    public static String formatToVietnamTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")); // giờ Việt Nam
        return sdf.format(date);
    }

    public static void checkDateRange(Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.INVALID_DATE_RANGE);
        }

    }
}
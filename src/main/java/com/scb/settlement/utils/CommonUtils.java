package com.scb.settlement.utils;

import com.scb.settlement.constants.Constants;
import com.scb.settlement.constants.SearchConstants;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Log4j2
public class CommonUtils {

    private CommonUtils(){}

    private static final Integer ROUND_PLACES = 2;
    private static final String SETTLEMENT_FILENAME_FORMAT = "Settlement_Detail_Report_{0}.xlsx";
    private static final String POCKET_FILENAME_FORMAT = "{0} - pocket balance.xlsx";

    public static Order getSortedField(String fieldName, Direction direction) {
    	fieldName = SearchConstants.transferDetailsMap.getOrDefault(fieldName, fieldName);
    	return direction.equals(Sort.Direction.ASC) ? new Order(Sort.Direction.ASC, fieldName)
                : new Order(Sort.Direction.DESC, fieldName);
    }

    public static List<Order> getSortedOrderList(Pageable pageable, String sortingField) {
        List<Order> orders = new ArrayList<>();
        pageable.getSort().forEach(sortOrder -> {
            log.info("Sorting field {} order Before {}", sortOrder.getProperty(),
                    sortOrder.getDirection());
            if(!(sortOrder.getProperty().equalsIgnoreCase("DESC") || sortOrder.getProperty().equalsIgnoreCase("ASC"))){
                if (sortOrder.getProperty().equals("DESC") || sortOrder.getProperty().equals("ASC")) {
                    Order orderLastElement = orders.get(orders.size() - 1);
                    Direction direction =
                            sortOrder.getProperty().equals("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
                    orders.set(orders.size() - 1, getSortedField(orderLastElement.getProperty(), direction));
                } else {
                    orders.add(getSortedField(sortOrder.getProperty(), sortOrder.getDirection()));
                }
                log.info("Sorting field {} order {}", sortOrder.getProperty(),
                        sortOrder.getDirection());
            }
        });
        if (ObjectUtils.isEmpty(orders)) {
            log.info("Sorting field {} order {}", sortingField, "ASC");
            orders.add(new Order(Sort.Direction.ASC, sortingField));
        }
        return orders;
    }

    public static Double round(Double value) {
        if (value == null)
            return null;
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(ROUND_PLACES, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static LocalTime toThaiTime(LocalDate localDate, LocalTime localTime) {
        if(Objects.isNull(localDate) || Objects.isNull(localTime)) {
            return null;
        }
        ZonedDateTime dateTimeUtc = ZonedDateTime.of(localDate, localTime, ZoneOffset.UTC);
        log.info("UTC date time before converting  {} ", dateTimeUtc);
        LocalTime bangkokTime = dateTimeUtc.withZoneSameInstant(ZoneId.of(Constants.BANGKOK_ZONE_ID)).toLocalTime();
        log.info("Thai time after conversion {} ", bangkokTime);
        return bangkokTime;
    }

    public static String getFormattedCurrentDate(String format) {
        return getFormattedDate(LocalDate.now(), format);
    }

    public static String getFormattedDate(LocalDate localDate, String format) {
        if(Objects.isNull(localDate)) {
            return StringUtils.EMPTY;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(localDate);
    }


    public static String getFormattedDateTime(LocalDateTime localDateTime, String format) {
        if(Objects.isNull(localDateTime)) {
            return StringUtils.EMPTY;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(localDateTime);
    }

    public static String getFormattedTime(LocalTime localTime, String format) {
        if(Objects.isNull(localTime)) {
            return StringUtils.EMPTY;
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(localTime);
    }

    public static LocalTime cutOffTimeToLocalTime(String cutOffTime) {
        log.info("Configured cutOffTime in ops-portal is {}", cutOffTime);
        LocalTime localTime;
        try {
            if(StringUtils.isEmpty(cutOffTime)){
                localTime = LocalTime.of(Constants.DEFAULT_HOUR_TIME, Constants.DEFAULT_MINUTE_TIME);
            } else {
                localTime = LocalTime.parse(cutOffTime, DateTimeFormatter.ofPattern(Constants.CUTOFF_TIME_FORMAT));
            }
        } catch (Exception e){
            localTime = LocalTime.of(Constants.DEFAULT_HOUR_TIME, Constants.DEFAULT_MINUTE_TIME);
        }
        return localTime;
    }

    public static LocalDateTime convertToUtc(LocalDateTime dateTime) {
        log.debug("Date time before converting  {} ", dateTime);
        ZonedDateTime dateTimeInMyZone = ZonedDateTime.of(dateTime, ZoneId.of(Constants.BANGKOK_ZONE_ID));
        LocalDateTime utcDateTime = dateTimeInMyZone.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        log.debug("UTC Converted date Time  {} ", utcDateTime);
        return utcDateTime;
    }

    public static LocalDateTime convertToThaiTime(LocalDateTime dateTime) {
        if(Objects.isNull(dateTime)) {
            return null;
        }
        log.debug("UTC Date time before converting  {} ", dateTime);
        ZonedDateTime dateTimeUtc = ZonedDateTime.of(dateTime, ZoneOffset.UTC);
        LocalDateTime bangkokDateTime = dateTimeUtc.withZoneSameInstant(ZoneId.of(Constants.BANGKOK_ZONE_ID)).toLocalDateTime();
        log.debug("Thai Date time after conversion {} ", bangkokDateTime);
        return bangkokDateTime;
    }

    public static String getSettlementFileName() {
        String currentDateString = getFormattedCurrentDate(Constants.DATE_FORMAT_YYYYMMDD);
        return MessageFormat.format(SETTLEMENT_FILENAME_FORMAT, currentDateString);
    }

    public static String getPocketReportName(String settlementBatchId) {
        return MessageFormat.format(POCKET_FILENAME_FORMAT, settlementBatchId);
    }

    public static double sum(double total, String value) {
        if(isNumber(value)) {
            return Double.sum(total, Double.parseDouble(value));
        } else {
            return total;
        }
    }

    public static double subtract(String a, String b) {
        Double subtractValue = 0.0;
        if(isNumber(a) && isNumber(b)) {
            subtractValue = Double.parseDouble(a) - Double.parseDouble(b);
        } else if(isNumber(a)) {
            subtractValue = Double.parseDouble(a);
        } else if(isNumber(b)) {
            subtractValue = -Double.parseDouble(b);
        }
        return subtractValue;
    }

    public static boolean isNumber(String value) {
        boolean isValid = false;
        try {
            if(StringUtils.isNotBlank(value)) {
                Double.parseDouble(value);
                isValid = true;
            }
        } catch (NumberFormatException e) { }
        return isValid;
    }

    public static String sanitize(byte[] strBytes) {
        return new String(strBytes)
                .replace("\r", "")
                .replace("\n", "");
    }

}

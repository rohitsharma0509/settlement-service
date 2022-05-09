package com.scb.settlement.constants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Constants {
    private Constants(){}
    public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";
    public static final String DATE_TIME_FORMAT = "yyyyMMddHHmmss";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyyMMddHHmmssSSS";
    public static final String SUCCESS = "Success";
    public static final String FAILED = "Failed";
    public static final String RUNNING = "Running";
    public static final String RECTIFIED = "Rectified";
    public static final String PROCESSING_REMARKS = "S1 batch still running";
    public static final String DEFAULT_DATE_STR = "99990101000000900";
    public static final LocalDateTime DEFAULT_DATE =  LocalDateTime.parse(DEFAULT_DATE_STR, DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT));
    public static final String BANGKOK_ZONE_ID = "Asia/Bangkok";
    public static final Integer DEFAULT_HOUR_TIME = 18;
    public static final Integer DEFAULT_MINUTE_TIME = 0;
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String CUTOFF_TIME_FORMAT = "hh:mm a";
    public static final String TRANSFER_DATE_FORMAT = "yyyy-MM-dd";
    public static final String JOB_DATETIME_FORMAT = "dd MMMM yyyy, hh:mm a";
    public static final String X_USER_ID = "X-User-Id";
    public static final String SYSTEM = "SYSTEM";
    public static final String UNREAL_RIDER_ID_FOR_SECURITY = "SEC0001";
    public static final String SECURITY_DEPOSIT_ACCOUNT_NO = "1113944775";
}

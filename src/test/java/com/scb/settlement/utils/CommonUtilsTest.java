package com.scb.settlement.utils;

import com.scb.settlement.constants.Constants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class CommonUtilsTest {

    @InjectMocks
    private CommonUtils commonUtils;

    @BeforeAll
    static void setup() {

    }

    @Test
    public void getSortedField() {
        Assertions.assertNotNull(CommonUtils.getSortedField("abc", Sort.Direction.DESC));
        Assertions.assertNotNull(CommonUtils.getSortedField("abc", Sort.Direction.ASC));
    }

    @Test
    public void getSortedOrderList() {
        Pageable pageable = PageRequest.of(1, 5, Sort.by("desc"));
        CommonUtils.getSortedOrderList(pageable, "batchId");
    }

    @Test
    public void testSumWithInvalidNumber() {
        double result = CommonUtils.sum(10.0, "10,0");
        Assertions.assertEquals(result, 10.0);
    }

    @Test
    public void testSum() {
        double result = CommonUtils.sum(10.0, "10.0");
        Assertions.assertEquals(result, 20.0);
    }

    @Test
    public void testSubtractWhenFirstNumberIsInvalid() {
        double result = CommonUtils.subtract("20,0", "10.0");
        Assertions.assertEquals(result, -10.0);
    }

    @Test
    public void testSubtractWhenSecondNumberIsInvalid() {
        double result = CommonUtils.subtract("20.0", "10,0");
        Assertions.assertEquals(result, 20.0);
    }

    @Test
    public void testSubtractWhenBothNumberAreInvalid() {
        double result = CommonUtils.subtract("20,0", "10,0");
        Assertions.assertEquals(result, 0.0);
    }

    @Test
    public void testSubtract() {
        double result = CommonUtils.subtract("20.0", "10.0");
        Assertions.assertEquals(result, 10.0);
    }

    @Test
    void toThaiTimeForNullDate() {
        LocalTime result = CommonUtils.toThaiTime(null, LocalTime.now());
        assertNull(result);
    }

    @Test
    void toThaiTimeForNullTime() {
        LocalTime result = CommonUtils.toThaiTime(LocalDate.now(), null);
        assertNull(result);
    }

    @Test
    void toThaiTimeForValidTime() {
        LocalTime result = CommonUtils.toThaiTime(LocalDate.now(), LocalTime.now());
        assertNotNull(result);
    }

    @Test
    void getFormattedDateTestForNull() {
        String result = CommonUtils.getFormattedDate(null, Constants.DATE_FORMAT_YYYYMMDD);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void getFormattedDateTestForValidInput() {
        String result = CommonUtils.getFormattedDate(LocalDate.now(), Constants.DATE_FORMAT_YYYYMMDD);
        assertNotNull(result);
    }

    @Test
    void getFormattedTimeTestForNull() {
        String result = CommonUtils.getFormattedTime(null, Constants.TIME_FORMAT);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void getFormattedTimeTestForValidInput() {
        String result = CommonUtils.getFormattedTime(LocalTime.now(), Constants.TIME_FORMAT);
        assertNotNull(result);
    }

    @Test
    void getFormattedDateTimeTestForNull() {
        String result = CommonUtils.getFormattedDateTime(null, Constants.DATE_TIME_FORMAT);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void getFormattedDateTimeTestForValidInput() {
        String result = CommonUtils.getFormattedDateTime(LocalDateTime.now(), Constants.DATE_TIME_FORMAT);
        assertNotNull(result);
    }

    @Test
    void convertToThaiTimeTestForNull() {
        LocalDateTime result = CommonUtils.convertToThaiTime(null);
        assertNull(result);
    }

    @Test
    void convertToThaiTimeTestForValidInput() {
        LocalDateTime result = CommonUtils.convertToThaiTime(LocalDateTime.now());
        assertNotNull(result);
    }

    @Test
    void cutOffTimeToLocalTimeShouldReturnDefaultTimeForNullInput() {
        LocalTime result = CommonUtils.cutOffTimeToLocalTime(null);
        assertNotNull(result);
    }

    @Test
    void cutOffTimeToLocalTimeShouldReturnDefaultTimeForException() {
        LocalTime result = CommonUtils.cutOffTimeToLocalTime("0X:00 PM");
        assertNotNull(result);
    }

    @Test
    void cutOffTimeToLocalTimeShouldReturnDefaultTimeForValidInput() {
        LocalTime result = CommonUtils.cutOffTimeToLocalTime("06:00 PM");
        assertNotNull(result);
    }
}
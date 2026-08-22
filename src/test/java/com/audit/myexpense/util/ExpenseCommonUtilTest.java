package com.audit.myexpense.util;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link ExpenseCommonUtil}.
 */
class ExpenseCommonUtilTest {

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    @Test
    void formatsDatesUsingDayMonthYearPattern() {
        assertEquals("15/01/2024", ExpenseCommonUtil.formattedDate(date(2024, 1, 15)));
        assertEquals("01/12/1999", ExpenseCommonUtil.formattedDate(date(1999, 12, 1)));
    }

    @Test
    void nullDateIsFormattedAsNull() {
        assertNull(ExpenseCommonUtil.formattedDate(null));
    }
}

package org.wickedsource.budgeteer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

class DateUtilTest {

    @ParameterizedTest
    @CsvSource(
            value = {
                    "20.01.2015:20.02.2015",
                    "24.02.2015:20.03.2015",
                    "21.02.2015:22.02.2015",
                    "19.02.2015:26.02.2015",
            },
            delimiter = ':')
    void dateRangeIsOverlapping(String startDate, String endDate) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        DateRange d1 = new DateRange(formatter.parse("20.02.2015"), formatter.parse("25.02.2015"));

        DateRange d2 = new DateRange(formatter.parse(startDate), formatter.parse(endDate));
        assertTrue(DateUtil.isDateRangeOverlapping(d1, d2));
    }

    @Test
    void dateRangeIsntOverlapping() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        DateRange d1 = new DateRange(formatter.parse("20.02.2015"), formatter.parse("25.02.2015"));

        DateRange d2 = new DateRange(formatter.parse("26.02.2015"), formatter.parse("22.03.2015"));
        assertFalse(DateUtil.isDateRangeOverlapping(d1, d2));
    }

}

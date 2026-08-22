package com.audit.myexpense.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.audit.myexpense.model.IncomeDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link IncomeTrackerController}.
 */
@ExtendWith(MockitoExtension.class)
class IncomeTrackerControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private IncomeTrackerController controller;

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day, 12, 0, 0);
        return calendar.getTime();
    }

    private static IncomeDetails income(int id, Date date, double amount, String source) {
        IncomeDetails details = new IncomeDetails();
        details.setIncomeId(id);
        details.setIncomeDate(date);
        details.setAmount(amount);
        details.setSource(source);
        return details;
    }

    @Test
    void saveAssignsNextIdAndDerivedMonthYear() {
        when(mongoTemplate.findOne(any(Query.class), eq(IncomeDetails.class)))
                .thenReturn(income(41, date(2024, 1, 1), 1000.0, "Salary"));
        when(mongoTemplate.insert(any(IncomeDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IncomeDetails request = income(0, date(2024, 3, 25), 50000.0, "Salary");
        IncomeDetails result = controller.incomeDetails(request);

        assertThat(result.getIncomeId()).isEqualTo(42);
        assertThat(result.getMonth()).isEqualTo("March");
        assertThat(result.getYear()).isEqualTo(2024);
        verify(mongoTemplate).insert(request);
    }

    @Test
    void saveStartsIdAtZeroWhenNoExistingRecords() {
        when(mongoTemplate.findOne(any(Query.class), eq(IncomeDetails.class))).thenReturn(null);
        when(mongoTemplate.insert(any(IncomeDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IncomeDetails result = controller.incomeDetails(income(0, date(2024, 12, 5), 1200.0, "Bonus"));

        assertThat(result.getIncomeId()).isZero();
        assertThat(result.getMonth()).isEqualTo("December");
        assertThat(result.getYear()).isEqualTo(2024);
    }

    @Test
    void searchReturnsIncomesSortedByDateAscending() {
        List<IncomeDetails> fromDb = new ArrayList<>(Arrays.asList(
                income(3, date(2024, 1, 10), 100.0, "A"),
                income(1, date(2024, 1, 2), 300.0, "B"),
                income(2, date(2024, 1, 6), 200.0, "C")));
        when(mongoTemplate.find(any(Query.class), eq(IncomeDetails.class))).thenReturn(fromDb);

        List<IncomeDetails> result = controller.incomeDetails(2024, "January");

        assertThat(result).extracting(IncomeDetails::getIncomeId)
                .containsExactly(1, 2, 3);
    }

    @Test
    void searchWithoutFiltersReturnsAllResults() {
        when(mongoTemplate.find(any(Query.class), eq(IncomeDetails.class)))
                .thenReturn(new ArrayList<>());

        assertThat(controller.incomeDetails(null, null)).isEmpty();
    }

    @Test
    void deleteRemovesExistingIncome() {
        IncomeDetails stored = income(7, date(2024, 5, 1), 900.0, "Salary");
        when(mongoTemplate.findById(7, IncomeDetails.class)).thenReturn(stored);

        Map<String, Object> body = controller.deleteIncomeDetail(7);

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("7").contains("deleted");
    }

    @Test
    void deleteOfUnknownIncomeReportsNotFound() {
        when(mongoTemplate.findById(123, IncomeDetails.class)).thenReturn(null);

        Map<String, Object> body = controller.deleteIncomeDetail(123);

        verify(mongoTemplate, never()).remove(any(IncomeDetails.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }
}

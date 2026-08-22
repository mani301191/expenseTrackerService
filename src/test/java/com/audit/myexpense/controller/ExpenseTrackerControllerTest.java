package com.audit.myexpense.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.audit.myexpense.model.ExpenseDetails;
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
 * Unit tests for {@link ExpenseTrackerController}.
 */
@ExtendWith(MockitoExtension.class)
class ExpenseTrackerControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ExpenseTrackerController controller;

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day, 12, 0, 0);
        return calendar.getTime();
    }

    private static ExpenseDetails expense(int id, Date date, double amount, String expenseOf, String type) {
        ExpenseDetails details = new ExpenseDetails();
        details.expenseId = id;
        details.expenseDate = date;
        details.amount = amount;
        details.expenseOf = expenseOf;
        details.expenseType = type;
        return details;
    }

    @Test
    void saveAssignsNextIdAndDerivedMonthYear() {
        ExpenseDetails stored = expense(9, date(2024, 1, 10), 100.0, "Groceries", "Planned");
        when(mongoTemplate.findOne(any(Query.class), eq(ExpenseDetails.class))).thenReturn(stored);
        when(mongoTemplate.insert(any(ExpenseDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseDetails request = expense(0, date(2024, 1, 15), 250.0, "Fuel", "Planned");
        ExpenseDetails result = controller.expenseDetail(request);

        assertThat(result.expenseId).isEqualTo(10);
        assertThat(result.month).isEqualTo("January");
        assertThat(result.year).isEqualTo(2024);
        assertThat(result.amount).isEqualTo(250.0);
        verify(mongoTemplate).insert(request);
    }

    @Test
    void saveStartsIdAtZeroWhenNoExistingRecords() {
        when(mongoTemplate.findOne(any(Query.class), eq(ExpenseDetails.class))).thenReturn(null);
        when(mongoTemplate.insert(any(ExpenseDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseDetails request = expense(0, date(2024, 2, 20), 99.0, "Movies", "UnPlanned");
        ExpenseDetails result = controller.expenseDetail(request);

        assertThat(result.expenseId).isZero();
        assertThat(result.month).isEqualTo("February");
        assertThat(result.year).isEqualTo(2024);
    }

    @Test
    void searchReturnsExpensesSortedByDateThenIdDescending() {
        List<ExpenseDetails> fromDb = new ArrayList<>(Arrays.asList(
                expense(5, date(2024, 1, 2), 10.0, "A", "Planned"),
                expense(1, date(2024, 1, 3), 20.0, "B", "Planned"),
                expense(7, date(2024, 1, 2), 30.0, "C", "Planned")));
        when(mongoTemplate.find(any(Query.class), eq(ExpenseDetails.class))).thenReturn(fromDb);

        List<ExpenseDetails> result = controller.expenseDetails(2024, "January", null, null);

        assertThat(result).extracting(details -> details.expenseId)
                .containsExactly(1, 7, 5);
    }

    @Test
    void searchSupportsOptionalFiltersWithoutFailure() {
        when(mongoTemplate.find(any(Query.class), eq(ExpenseDetails.class)))
                .thenReturn(new ArrayList<>());

        List<ExpenseDetails> result = controller.expenseDetails(null, "", "", "");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteRemovesExistingExpense() {
        ExpenseDetails stored = expense(5, date(2024, 1, 15), 50.0, "Fuel", "Planned");
        when(mongoTemplate.findById(5, ExpenseDetails.class)).thenReturn(stored);

        Map<String, Object> body = controller.deleteExpenseDetail(5);

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("5").contains("deleted");
    }

    @Test
    void deleteOfUnknownExpenseReportsNotFound() {
        when(mongoTemplate.findById(42, ExpenseDetails.class)).thenReturn(null);

        Map<String, Object> body = controller.deleteExpenseDetail(42);

        verify(mongoTemplate, never()).remove(any(ExpenseDetails.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }

    @Test
    void updateCopiesIncomingFieldsOntoStoredExpense() {
        ExpenseDetails stored = expense(3, date(2024, 1, 15), 100.0, "Old", "Planned");
        when(mongoTemplate.findById(3, ExpenseDetails.class)).thenReturn(stored);
        when(mongoTemplate.save(any(ExpenseDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> body = controller.updateExpenseDetail(
                expense(3, date(2024, 1, 15), 175.0, "New", "Planned"));

        assertThat(stored.amount).isEqualTo(175.0);
        assertThat(stored.expenseOf).isEqualTo("New");
        assertThat(stored.updatedDate).isNotBlank();
        verify(mongoTemplate).save(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("updated successfully");
    }

    @Test
    void updateOfUnknownExpenseReportsDataNotfound() {
        when(mongoTemplate.findById(99, ExpenseDetails.class)).thenReturn(null);

        Map<String, Object> body = controller.updateExpenseDetail(
                expense(99, date(2024, 1, 15), 1.0, "Ghost", "Planned"));

        verify(mongoTemplate, never()).save(any(ExpenseDetails.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Data not found");
    }
}

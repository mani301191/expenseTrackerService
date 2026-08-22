package com.audit.myexpense.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.audit.myexpense.model.DayWiseExpense;
import com.audit.myexpense.model.ExpenseDetails;
import com.audit.myexpense.model.MonthlySummary;
import com.audit.myexpense.model.MonthlyTarget;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MonthlySummaryController}.
 */
@ExtendWith(MockitoExtension.class)
class MonthlySummaryControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private MonthlySummaryController controller;

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day, 12, 0, 0);
        return calendar.getTime();
    }

    private static MonthlySummary summary(int year, String month, double expense, double income) {
        MonthlySummary summary = new MonthlySummary();
        summary.setYear(year);
        summary.setMonth(month);
        summary.setExpense(expense);
        summary.setIncome(income);
        return summary;
    }

    private static MonthlyTarget target(int year, String month, double amount) {
        return new MonthlyTarget(year, month, "Groceries", amount);
    }

    @Test
    void monthlySummaryCombinesExpenseIncomeAndEstimates() {
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("myExpenseDetail"), eq(MonthlySummary.class)))
                .thenReturn(new AggregationResults<>(
                        Arrays.asList(summary(2024, "January", 250.0, 0.0)), new Document()));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("myIncomeDetail"), eq(MonthlySummary.class)))
                .thenReturn(new AggregationResults<>(
                        Arrays.asList(
                                summary(2024, "January", 0.0, 400.0),
                                summary(2024, "February", 0.0, 500.0)), new Document()));
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class)))
                .thenReturn(Arrays.asList(target(2024, "January", 300.0)));

        List<MonthlySummary> result = controller.monthlySummary(2024, null);

        assertThat(result).extracting(MonthlySummary::getMonth)
                .containsExactly("February", "January");

        MonthlySummary february = result.get(0);
        assertThat(february.getIncome()).isEqualTo(500.0);
        assertThat(february.getExpense()).isZero();
        assertThat(february.getSavings()).isEqualTo(500.0);
        assertThat(february.getEstimated()).isZero();

        MonthlySummary january = result.get(1);
        assertThat(january.getExpense()).isEqualTo(250.0);
        assertThat(january.getIncome()).isEqualTo(400.0);
        assertThat(january.getSavings()).isEqualTo(150.0);
        assertThat(january.getEstimated()).isEqualTo(300.0);
    }

    @Test
    void monthlySummaryWithoutAnyDataReturnsEmptyList() {
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("myExpenseDetail"), eq(MonthlySummary.class)))
                .thenReturn(new AggregationResults<>(Collections.emptyList(), new Document()));
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("myIncomeDetail"), eq(MonthlySummary.class)))
                .thenReturn(new AggregationResults<>(Collections.emptyList(), new Document()));
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class)))
                .thenReturn(Collections.emptyList());

        assertThat(controller.monthlySummary(2030, null)).isEmpty();
    }

    @Test
    void dailySummaryAggregatesExpensesPerDayOfTheMonth() {
        ExpenseDetails first = new ExpenseDetails();
        first.expenseDate = date(2024, 2, 15);
        first.amount = 50.0;
        ExpenseDetails second = new ExpenseDetails();
        second.expenseDate = date(2024, 2, 15);
        second.amount = 70.0;
        when(mongoTemplate.find(any(Query.class), eq(ExpenseDetails.class), eq("myExpenseDetail")))
                .thenReturn(Arrays.asList(first, second));

        List<DayWiseExpense> result = controller.dailySummary(2024, "February");

        assertThat(result).hasSize(29); // leap year
        assertThat(result.get(14).date).isEqualTo("2024-02-15");
        assertThat(result.get(14).expense).isEqualTo(120.0);
        assertThat(result.get(0).date).isEqualTo("2024-02-01");
        assertThat(result.get(0).expense).isZero();
    }

    @Test
    void dailySummaryRejectsUnknownMonthName() {
        assertThatThrownBy(() -> controller.dailySummary(2024, "Foo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid month name: Foo");
    }
}

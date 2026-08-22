package com.audit.myexpense.controller;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.audit.myexpense.model.ExpenseDetails;
import com.audit.myexpense.model.MonthlyStatus;
import com.audit.myexpense.model.MonthlyTarget;
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
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MonthlyStatusController}.
 */
@ExtendWith(MockitoExtension.class)
class MonthlyStatusControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private MonthlyStatusController controller;

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day, 12, 0, 0);
        return calendar.getTime();
    }

    private static MonthlyTarget target(String description, double amount) {
        MonthlyTarget target = new MonthlyTarget();
        target.description = description;
        target.amount = amount;
        return target;
    }

    private static ExpenseDetails expense(String expenseOf, double amount) {
        ExpenseDetails details = new ExpenseDetails();
        details.expenseOf = expenseOf;
        details.amount = amount;
        details.expenseDate = date(2024, 1, 15);
        return details;
    }

    @Test
    void statusJoinsTargetsAndExpensesSortedCaseInsensitively() {
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class)))
                .thenReturn(Arrays.asList(target("Groceries", 200.0), target("Fuel", 100.0)));
        when(mongoTemplate.find(any(Query.class), eq(ExpenseDetails.class)))
                .thenReturn(Arrays.asList(
                        expense("Groceries", 60.0),
                        expense("Fuel", 30.0),
                        expense("Groceries", 40.0),
                        expense("Miscellaneous", 999.0)));

        List<MonthlyStatus> result = controller.monthlyStatus(2024, "January");

        assertThat(result).extracting(status -> status.description)
                .containsExactly("Fuel", "Groceries");
        assertThat(result.get(0).estimatedAmount).isEqualTo(100.0);
        assertThat(result.get(0).expenseAmount).isEqualTo(30.0);
        assertThat(result.get(1).estimatedAmount).isEqualTo(200.0);
        assertThat(result.get(1).expenseAmount).isEqualTo(100.0);
    }

    @Test
    void targetsWithoutExpensesReportZeroSpent() {
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class)))
                .thenReturn(Arrays.asList(target("Vacation", 500.0)));
        when(mongoTemplate.find(any(Query.class), eq(ExpenseDetails.class)))
                .thenReturn(Arrays.asList());

        List<MonthlyStatus> result = controller.monthlyStatus(2024, "February");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).expenseAmount).isZero();
        assertThat(result.get(0).estimatedAmount).isEqualTo(500.0);
    }
}

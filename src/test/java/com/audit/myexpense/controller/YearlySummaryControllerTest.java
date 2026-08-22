package com.audit.myexpense.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.audit.myexpense.model.Category;
import com.audit.myexpense.model.ExpenseDetails;
import com.audit.myexpense.model.IncomeDetails;
import com.audit.myexpense.model.MonthlyExpByCatagory;
import com.audit.myexpense.model.MonthlyExpByCatagoryResult;
import com.audit.myexpense.model.MonthlyTarget;
import com.audit.myexpense.model.YearlySummary;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link YearlySummaryController}.
 */
@ExtendWith(MockitoExtension.class)
class YearlySummaryControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private YearlySummaryController controller;

    private static IncomeDetails income(int year, double amount) {
        IncomeDetails details = new IncomeDetails();
        details.setYear(year);
        details.setAmount(amount);
        return details;
    }

    private static ExpenseDetails expense(int year, String expenseType, double amount) {
        ExpenseDetails details = new ExpenseDetails();
        details.year = year;
        details.expenseType = expenseType;
        details.amount = amount;
        return details;
    }

    private static MonthlyTarget target(int year, double amount) {
        MonthlyTarget target = new MonthlyTarget();
        target.year = year;
        target.amount = amount;
        return target;
    }

    @Test
    void yearlySummaryAggregatesIncomeExpenseEstimateAndCategories() {
        when(mongoTemplate.find(any(Query.class), eq(IncomeDetails.class)))
                .thenReturn(Arrays.asList(income(2024, 100.0), income(2024, 200.0)));
        when(mongoTemplate.find(any(Query.class), eq(ExpenseDetails.class)))
                .thenReturn(Arrays.asList(
                        expense(2024, "Groceries", 50.0),
                        expense(2024, "Fuel", 30.0),
                        expense(2023, "Food", 40.0)));
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class)))
                .thenReturn(Arrays.asList(target(2024, 60.0), target(2024, 60.0)));

        List<YearlySummary> result = controller.yearlySummary(null);

        assertThat(result).extracting(summary -> summary.year.intValue())
                .containsExactly(2024, 2023);

        YearlySummary year2024 = result.get(0);
        assertThat(year2024.income).isEqualTo(300.0);
        assertThat(year2024.expense).isEqualTo(80.0);
        assertThat(year2024.savings).isEqualTo(220.0);
        assertThat(year2024.estimated).isEqualTo(120.0);
        assertThat(year2024.category).extracting(category -> category.expenseType + ":" + category.amount)
                .containsExactlyInAnyOrder("Groceries:50.0", "Fuel:30.0");

        YearlySummary year2023 = result.get(1);
        assertThat(year2023.income).isZero();
        assertThat(year2023.expense).isEqualTo(40.0);
        assertThat(year2023.savings).isEqualTo(-40.0);
        assertThat(year2023.estimated).isZero();
    }

    @Test
    void yearlySummaryReturnsEmptyListWhenNoExpensesExist() {
        when(mongoTemplate.find(any(Query.class), eq(IncomeDetails.class)))
                .thenReturn(Collections.singletonList(income(2024, 500.0)));
        when(mongoTemplate.find(any(Query.class), eq(ExpenseDetails.class)))
                .thenReturn(Collections.emptyList());
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class)))
                .thenReturn(Collections.emptyList());

        assertThat(controller.yearlySummary(2024)).isEmpty();
    }

    @Test
    void monthlyExpByCatagoryMapsResultsIntoMonthColumns() {
        MonthlyExpByCatagoryResult rentJanuary = new MonthlyExpByCatagoryResult();
        rentJanuary.year = 2024;
        rentJanuary.month = "January";
        rentJanuary.expenseOf = "Rent";
        rentJanuary.totalAmount = 1200.0;

        MonthlyExpByCatagoryResult rentFebruary = new MonthlyExpByCatagoryResult();
        rentFebruary.year = 2024;
        rentFebruary.month = "February";
        rentFebruary.expenseOf = "Rent";
        rentFebruary.totalAmount = 900.0;

        MonthlyExpByCatagoryResult foodMarch = new MonthlyExpByCatagoryResult();
        foodMarch.year = 2024;
        foodMarch.month = "March";
        foodMarch.expenseOf = "Food";
        foodMarch.totalAmount = 500.0;

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("myExpenseDetail"), eq(MonthlyExpByCatagoryResult.class)))
                .thenReturn(new AggregationResults<>(Arrays.asList(rentJanuary, rentFebruary, foodMarch), new Document()));

        List<MonthlyExpByCatagory> result = controller.monthlyExpByCatagory(2024);

        assertThat(result).hasSize(2);

        MonthlyExpByCatagory rent = result.stream()
                .filter(row -> row.category.equals("Rent"))
                .findFirst().orElseThrow(IllegalStateException::new);
        assertThat(rent.January).isEqualTo(1200.0);
        assertThat(rent.February).isEqualTo(900.0);
        assertThat(rent.March).isZero();
        assertThat(rent.December).isZero();

        MonthlyExpByCatagory food = result.stream()
                .filter(row -> row.category.equals("Food"))
                .findFirst().orElseThrow(IllegalStateException::new);
        assertThat(food.March).isEqualTo(500.0);
        assertThat(food.January).isZero();
    }
}

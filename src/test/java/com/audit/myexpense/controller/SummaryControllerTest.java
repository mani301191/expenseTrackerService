package com.audit.myexpense.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.audit.myexpense.model.DashboardData;
import com.audit.myexpense.model.ExpenseDetails;
import com.audit.myexpense.model.ExpenseTrackingSummary;
import com.audit.myexpense.model.FitnessChartData;
import com.audit.myexpense.model.FitnessSummary;
import com.audit.myexpense.model.IncomeDetails;
import com.audit.myexpense.model.InsuranceDetails;
import com.audit.myexpense.model.InsuranceSummary;
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
 * Unit tests for {@link SummaryController}.
 */
@ExtendWith(MockitoExtension.class)
class SummaryControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private SummaryController controller;

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day, 12, 0, 0);
        return calendar.getTime();
    }

    private static Date daysFromNow(long days) {
        return Date.from(LocalDate.now().plusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static IncomeDetails income(double amount) {
        IncomeDetails details = new IncomeDetails();
        details.setAmount(amount);
        return details;
    }

    private static ExpenseDetails expense(double amount) {
        ExpenseDetails details = new ExpenseDetails();
        details.amount = amount;
        return details;
    }

    private static MonthlyTarget target(double amount) {
        MonthlyTarget target = new MonthlyTarget();
        target.amount = amount;
        return target;
    }

    private static InsuranceDetails insurance(String provider, Date endDate) {
        InsuranceDetails details = new InsuranceDetails();
        details.insuranceProvider = provider;
        details.endDate = endDate;
        return details;
    }

    private static FitnessChartData fitnessData(String personName, Date date, double weight) {
        FitnessChartData data = new FitnessChartData();
        data.personName = personName;
        data.date = date;
        data.weight = weight;
        return data;
    }

    @Test
    void expenseTrackingSummarySumsCurrentMonthData() {
        when(mongoTemplate.find(any(Query.class), eq(IncomeDetails.class), eq("myIncomeDetail")))
                .thenReturn(Arrays.asList(income(1000.0), income(500.0)));
        when(mongoTemplate.find(any(Query.class), eq(ExpenseDetails.class), eq("myExpenseDetail")))
                .thenReturn(Collections.singletonList(expense(300.0)));
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class), eq("myMonthlyTarget")))
                .thenReturn(Arrays.asList(target(400.0), target(200.0)));

        ExpenseTrackingSummary summary = controller.getExpenseTrackingSummary();

        assertThat(summary.income).isEqualTo(1500.0);
        assertThat(summary.expense).isEqualTo(300.0);
        assertThat(summary.estimate).isEqualTo(600.0);
        assertThat(summary.currentMonth)
                .isEqualTo(LocalDate.now().getMonth().getDisplayName(java.time.format.TextStyle.FULL,
                        java.util.Locale.ENGLISH));
    }

    @Test
    void activeInsurancesAreSortedByEndDateAscending() {
        InsuranceDetails lateInsurance = insurance("ProviderA", daysFromNow(60));
        InsuranceDetails earlyInsurance = insurance("ProviderB", daysFromNow(10));
        when(mongoTemplate.find(any(Query.class), eq(InsuranceDetails.class), eq("myInsuranceDetails")))
                .thenReturn(new java.util.ArrayList<>(Arrays.asList(lateInsurance, earlyInsurance)));

        List<InsuranceSummary> result = controller.getActiveInsurances();

        assertThat(result).extracting(summary -> summary.type)
                .containsExactly("ProviderB", "ProviderA");
        assertThat(result.get(0).expiryDate).isNotBlank();
    }

    @Test
    void fitnessSummariesComputeMinMaxAndCurrentWeightsPerPerson() {
        when(mongoTemplate.find(any(Query.class), eq(FitnessChartData.class), eq("fitnessWeightData")))
                .thenReturn(Arrays.asList(
                        fitnessData("Alex", date(2024, 1, 10), 80.0),
                        fitnessData("Alex", date(2024, 2, 10), 75.0),
                        fitnessData("Alex", date(2024, 3, 10), 78.0),
                        fitnessData("Brad", date(2024, 5, 5), 90.0)));

        List<FitnessSummary> result = controller.getFitnessSummaries();

        assertThat(result).hasSize(2);

        FitnessSummary alex = result.stream()
                .filter(summary -> "Alex".equals(summary.name))
                .findFirst().orElseThrow(IllegalStateException::new);
        assertThat(alex.minWeight).isEqualTo(75.0);
        assertThat(alex.minWeightDate).isEqualTo("10/02/2024");
        assertThat(alex.maxWeight).isEqualTo(80.0);
        assertThat(alex.maxWeightDate).isEqualTo("10/01/2024");
        assertThat(alex.currentWeight).isEqualTo(78.0);
        assertThat(alex.currentWeightDate).isEqualTo("10/03/2024");

        FitnessSummary brad = result.stream()
                .filter(summary -> "Brad".equals(summary.name))
                .findFirst().orElseThrow(IllegalStateException::new);
        assertThat(brad.minWeight).isEqualTo(90.0);
        assertThat(brad.maxWeight).isEqualTo(90.0);
        assertThat(brad.currentWeight).isEqualTo(90.0);
    }

    @Test
    void dashboardSummaryCombinesTrackingFitnessAndInsuranceData() {
        when(mongoTemplate.find(any(Query.class), eq(IncomeDetails.class), eq("myIncomeDetail")))
                .thenReturn(Collections.singletonList(income(1000.0)));
        when(mongoTemplate.find(any(Query.class), eq(ExpenseDetails.class), eq("myExpenseDetail")))
                .thenReturn(Collections.singletonList(expense(250.0)));
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class), eq("myMonthlyTarget")))
                .thenReturn(Collections.singletonList(target(600.0)));
        when(mongoTemplate.find(any(Query.class), eq(InsuranceDetails.class), eq("myInsuranceDetails")))
                .thenReturn(Collections.singletonList(insurance("ProviderA", daysFromNow(30))));
        when(mongoTemplate.find(any(Query.class), eq(FitnessChartData.class), eq("fitnessWeightData")))
                .thenReturn(Collections.singletonList(fitnessData("Alex", date(2024, 1, 10), 80.0)));

        DashboardData dashboard = controller.dashboardSummary();

        assertThat(dashboard.expenseTrackingData.income).isEqualTo(1000.0);
        assertThat(dashboard.expenseTrackingData.expense).isEqualTo(250.0);
        assertThat(dashboard.expenseTrackingData.estimate).isEqualTo(600.0);
        assertThat(dashboard.insuranceData).hasSize(1);
        assertThat(dashboard.fitnessData).extracting(summary -> summary.name)
                .containsExactly("Alex");
    }
}

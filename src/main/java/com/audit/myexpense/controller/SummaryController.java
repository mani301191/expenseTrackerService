package com.audit.myexpense.controller;

import com.audit.myexpense.model.*;
import com.audit.myexpense.util.ExpenseCommonUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller to hold summary
 * @author Manikandan Narasimhan
 *
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class SummaryController {

    private final MongoTemplate mongoTemplate;
    private static final List<String> monthList = Arrays.asList(
            "January", "February", "March", "April",
            "May", "June", "July", "August", "September",
            "October", "November", "December"
    );

    public SummaryController( MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/summary")
    public DashboardData dashboardSummary() {
        DashboardData dashboardData= new DashboardData();
        dashboardData.expenseTrackingData = getExpenseTrackingSummary();
        dashboardData.fitnessData = getFitnessSummaries();
        dashboardData.insuranceData = getActiveInsurances();
       return  dashboardData;
    }


    public ExpenseTrackingSummary getExpenseTrackingSummary() {
        // Get the current month and year dynamically
        int currentMonthIndex = LocalDate.now().getMonthValue();
        String currentMonth = monthList.get(currentMonthIndex - 1); // Adjust for 0-based index
        int currentYear = LocalDate.now().getYear();

        // Query for income from myIncome collection
        Query incomeQuery = new Query(Criteria.where("month").is(currentMonth).and("year").is(currentYear));
        Double income = mongoTemplate.find(incomeQuery, IncomeDetails.class, "myIncomeDetail")
                .stream()
                .mapToDouble(IncomeDetails::getAmount)
                .sum();

        // Query for expense from myExpense collection
        Query expenseQuery = new Query(Criteria.where("month").is(currentMonth).and("year").is(currentYear));
        Double expense = mongoTemplate.find(expenseQuery, ExpenseDetails.class, "myExpenseDetail")
                .stream()
                .mapToDouble(expenseDetails -> expenseDetails.amount)
                .sum();

        // Query for estimate from myMonthlyTarget collection
        Query estimateQuery = new Query(Criteria.where("month").is(currentMonth).and("year").is(currentYear));
        Double estimate = mongoTemplate.find(estimateQuery, MonthlyTarget.class, "myMonthlyTarget")
                .stream()
                .mapToDouble(monthlyTarget ->monthlyTarget.amount )
                .sum();

        // Combine results into ExpenseTrackingSummary
        ExpenseTrackingSummary summary = new ExpenseTrackingSummary();
        summary.currentMonth=currentMonth;
        summary.income =income != null ? income : 0;
        summary.expense=expense != null ? expense : 0;
        summary.estimate=estimate != null ? estimate : 0;

        return summary;
    }

    public List<InsuranceSummary> getActiveInsurances() {
        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Create a query to fetch insurances where endDate > currentDate
        Query query = new Query(Criteria.where("endDate").gt(currentDate));

        // Execute the query and return the results
        List<InsuranceDetails> insuranceDetailsList = mongoTemplate.find(query, InsuranceDetails.class, "myInsuranceDetails");
        // Sort the list using List.sort() and a lambda expression
        insuranceDetailsList.sort((insurance1, insurance2) -> insurance1.endDate.compareTo(insurance2.endDate));

        // Map InsuranceDetails to InsuranceSummary
        return insuranceDetailsList.stream()
                .map(details -> new InsuranceSummary(details.insuranceProvider, ExpenseCommonUtil.formattedDate(details.endDate)))
                .collect(Collectors.toList());
    }

    public List<FitnessSummary> getFitnessSummaries() {
        // Query to fetch all fitness weight data
        Query query = new Query();
        List<FitnessChartData> fitnessWeightDataList = mongoTemplate.find(query, FitnessChartData.class, "fitnessWeightData");
        return fitnessWeightDataList.stream()
                .collect(Collectors.groupingBy(fitness->fitness.personName))
                .entrySet()
                .stream()
                .map(entry -> {
                    String name = entry.getKey();
                    List<FitnessChartData> dataList = entry.getValue();

                    // Calculate min weight
                    FitnessChartData minWeightData = dataList.stream()
                            .min((a, b) -> Double.compare(a.weight, b.weight))
                            .orElse(null);
                    // Calculate max weight
                    FitnessChartData maxWeightData = dataList.stream()
                            .max((a, b) -> Double.compare(a.weight, b.weight))
                            .orElse(null);

                    // Get current weight (latest entry)
                    FitnessChartData currentWeightData = dataList.stream()
                            .max((a, b) -> a.date.compareTo(b.date))
                            .orElse(null);

                    // Create FitnessSummary object
                    FitnessSummary summary = new FitnessSummary();
                    summary.name = name;
                    summary.minWeight = minWeightData != null ? minWeightData.weight : 0.0;
                    summary.minWeightDate = minWeightData != null ? ExpenseCommonUtil.formattedDate(minWeightData.date) : null;
                    summary.maxWeight = maxWeightData != null ? maxWeightData.weight : 0.0;
                    summary.maxWeightDate = maxWeightData != null ?ExpenseCommonUtil.formattedDate( maxWeightData.date ): null;
                    summary.currentWeight = currentWeightData != null ? currentWeightData.weight: 0.0;
                    summary.currentWeightDate = currentWeightData != null ? ExpenseCommonUtil.formattedDate(currentWeightData.date) : null;

                    return summary;
                })
                .collect(Collectors.toList());

                }
}

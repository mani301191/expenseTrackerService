/**
 * Controller for monthly summary
 *  @author Manikandan Narasimhan
 */
package com.audit.myexpense.controller;

import com.audit.myexpense.model.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

/**
 * Controller for yearly summary
 *  @author Manikandan Narasimhan
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class YearlySummaryController {

    private final MongoTemplate mongoTemplate;
    public YearlySummaryController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/yearlySummary")
    public List<YearlySummary> yearlySummary(@RequestParam(required = false) Integer year) {
        List<YearlySummary> response = new ArrayList<>();
        final Query query = year==null? new Query() : new Query(Criteria.where("year").is(year));

        //Fetch all documents from db
        List<IncomeDetails> incomeResult = mongoTemplate.find(query, IncomeDetails.class);
        List<ExpenseDetails> expenseResult = mongoTemplate.find(query, ExpenseDetails.class);
        List<MonthlyTarget> estimatedResult = mongoTemplate.find(query, MonthlyTarget.class);
        // map based on year
        Map<Integer,List<IncomeDetails>> incomeMap = incomeResult.stream().collect(groupingBy(IncomeDetails::getYear));
        Map<Integer,List<ExpenseDetails>> expenseMap = expenseResult.stream().collect(groupingBy(r->r.year));
        Map<Integer,List<MonthlyTarget>> estimateMap = estimatedResult.stream().collect(groupingBy(r->r.year));

        for(Integer key: expenseMap.keySet() ){
            YearlySummary yearlyData = new YearlySummary();
            //get the data based on year
            List<IncomeDetails> incomeList = incomeMap.get(key);
            List<ExpenseDetails> expensList = expenseMap.get(key);
            List<MonthlyTarget> estimateList = estimateMap.get(key);
            List<Category> categorylist = new ArrayList<>();
            // sum all the date based on year
            yearlyData.year = key;
            yearlyData.income = incomeList!=null?incomeList.stream().mapToDouble(r->r.getAmount()).sum():0.0;
            yearlyData.expense = expensList!=null?expensList.stream().mapToDouble(r->r.amount).sum():0.0;
            yearlyData.estimated = estimateList!=null?estimateList.stream().mapToDouble(r->r.amount).sum():0.0;
            yearlyData.savings = yearlyData.income - yearlyData.expense;
            //expense category
            Map<String,List<ExpenseDetails>> categoryMap = expenseMap.get(key)
                    .stream().collect(groupingBy(r->r.expenseType));
            //category Type data
            for(String category: categoryMap.keySet()) {
                Category categoryType = new Category();
                categoryType.expenseType = category;
                List<ExpenseDetails> catList= categoryMap.get(category);
                categoryType.amount = catList!=null ? catList.stream().mapToDouble(r->r.amount).sum():0.0;
                categorylist.add(categoryType);
            }
            yearlyData.category=categorylist;
            response.add(yearlyData);
        }
        // comprator logic
        Comparator<YearlySummary> dateComparator = (c1, c2) -> {			return c2.year.compareTo(c1.year);
        };
        Collections.sort(response, dateComparator);
        return response;
    }


    @GetMapping("/monthlyExpByCatagory")
    public List<MonthlyExpByCatagory> monthlyExpByCatagory(@RequestParam Integer year) {
        List<MonthlyExpByCatagory> response = new ArrayList<>();

        Aggregation agg = newAggregation(
                match( Criteria.where("year").is(year)),
                group("year","month","expenseOf").sum("amount").as("totalAmount"),
                project("totalAmount","expenseOf","month","year")
        );
        AggregationResults<MonthlyExpByCatagoryResult> result= mongoTemplate.aggregate(agg,"myExpenseDetail" ,MonthlyExpByCatagoryResult.class);
     Map<String,List<MonthlyExpByCatagoryResult>> mapByExpType= result.getMappedResults().stream().collect(groupingBy(d->d.expenseOf));
        for(String data:mapByExpType.keySet()){
         MonthlyExpByCatagory monthlyExpByCatagory = new MonthlyExpByCatagory();
         monthlyExpByCatagory.category= data;
         monthlyExpByCatagory.January= totalAmount(mapByExpType.get(data).stream().filter(d -> d.month.equals("January")).findFirst());
            monthlyExpByCatagory.February=totalAmount(mapByExpType.get(data).stream().filter(d -> d.month.equals("February")).findFirst());
            monthlyExpByCatagory.March=totalAmount(mapByExpType.get(data).stream().filter(d -> d.month.equals("March")).findFirst());
            monthlyExpByCatagory.April=totalAmount(mapByExpType.get(data).stream().filter(d -> d.month.equals("April")).findFirst());
            monthlyExpByCatagory.May=totalAmount(mapByExpType.get(data).stream().filter(d -> d.month.equals("May")).findFirst());
            monthlyExpByCatagory.June=totalAmount(mapByExpType.get(data).stream().filter(d -> d.month.equals("June")).findFirst());
            monthlyExpByCatagory.July=totalAmount(mapByExpType.get(data).stream().filter(d -> d.month.equals("July")).findFirst());
            monthlyExpByCatagory.August=totalAmount(mapByExpType.get(data).stream().filter(d -> d.month.equals("August")).findFirst());
            monthlyExpByCatagory.September=totalAmount(mapByExpType.get(data).stream().filter(d -> d.month.equals("September")).findFirst());
            monthlyExpByCatagory.October=totalAmount(mapByExpType.get(data).stream().filter(d -> d.month.equals("October")).findFirst());
            monthlyExpByCatagory.November=totalAmount(mapByExpType.get(data).stream().filter(d -> d.month.equals("November")).findFirst());
            monthlyExpByCatagory.December=totalAmount(mapByExpType.get(data).stream().filter(d -> d.month.equals("December")).findFirst());

         response.add(monthlyExpByCatagory);
     }
     return response;
    }

    private double totalAmount(Optional<MonthlyExpByCatagoryResult> inputData){
        if(inputData.isPresent()){
            return inputData.get().totalAmount;
        } else {
            return 0;
        }
    }
}

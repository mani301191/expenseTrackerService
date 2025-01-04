/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */
package com.audit.myexpense.controller;

import com.audit.myexpense.model.MonthlySummary;
import com.audit.myexpense.model.MonthlyTarget;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

/**
 * Controller for monthly summary
 *  @author Manikandan Narasimhan
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class MonthlySummaryController {

    private final MongoTemplate mongoTemplate;
    public MonthlySummaryController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    /**
     * @param year  year of summary
     * @param month month of summary
     * @return monthlySummary
     */
    @GetMapping("/monthlySummary")
    public List<MonthlySummary> monthlySummary(@RequestParam Integer year,
                                               @RequestParam(required = false) String month) {
        List<MonthlySummary> response = new ArrayList<>();
        final List<Criteria> criteria = new ArrayList<>();
        if (year != null) {
            criteria.add(Criteria.where("year").is(year));
        }
        if (month != null && !month.isEmpty()) {
            criteria.add(Criteria.where("month").is(month));
        }

        Map<String,MonthlySummary> expenseSummaryMap = aggregationResults(criteria,"expense","myExpenseDetail")
                .getMappedResults().stream().collect(Collectors.toMap(exp->exp.getYear()+exp.getMonth(), exp->exp));

        Map<String,MonthlySummary> incomeSummaryMap =  aggregationResults(criteria,"income","myIncomeDetail")
                .getMappedResults().stream().collect(Collectors.toMap(exp->exp.getYear()+exp.getMonth(),exp->exp));
        // estimate datas
        final Query query =  new Query(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));
        List<MonthlyTarget> result = mongoTemplate.find(query, MonthlyTarget.class);
        for(MonthlySummary summary :expenseSummaryMap.values() ){
            MonthlySummary incomeDetail = incomeSummaryMap.get(summary.getYear()+summary.getMonth());
            summary.setIncome(incomeDetail!=null?incomeDetail.getIncome():0.0);
            summary.setSavings(incomeDetail!=null?incomeDetail.getIncome()-summary.getExpense():0.0-summary.getExpense());
            summary.setEstimated(result.stream().filter(estimate -> estimate.year== summary.getYear() &&
                    estimate.month.equals(summary.getMonth())).mapToDouble(t->t.amount).sum());

            response.add(summary);
        }
        for(MonthlySummary incomeSummary :incomeSummaryMap.values()) {
            if(expenseSummaryMap.get(incomeSummary.getYear()+incomeSummary.getMonth()) == null) {
                incomeSummary.setSavings(incomeSummary.getIncome());
                incomeSummary.setEstimated(result.stream().filter(estimate -> estimate.year== incomeSummary.getYear() &&
                        estimate.month.equals(incomeSummary.getMonth())).mapToDouble(t->t.amount).sum());
                response.add(incomeSummary);
            }
        }

        return response;
    }

    private AggregationResults<MonthlySummary> aggregationResults(List<Criteria> criteria, String attribute, String collectionName) {
        Aggregation agg = newAggregation(
                match( new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()]))),
                group("year","month").sum("amount").as(attribute),
                project(attribute,"month","year")
        );
        return mongoTemplate.aggregate(agg,collectionName ,MonthlySummary.class);
    }
}

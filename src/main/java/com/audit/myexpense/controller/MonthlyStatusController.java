/*
 *  Copyright (C) 2024 Manikandan Narasimhan - All Rights Reserved
 *  * You may use, distribute and modify this code.
 *
 */

package com.audit.myexpense.controller;

import com.audit.myexpense.model.ExpenseDetails;
import com.audit.myexpense.model.MonthlyStatus;
import com.audit.myexpense.model.MonthlyTarget;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for monthly Target
 *  @author Manikandan Narasimhan
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class MonthlyStatusController {

    private final MongoTemplate mongoTemplate;
    public MonthlyStatusController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param year year of target status
     * @param month month of target status
     * @return monthlyTarget
     */
    @GetMapping("/monthlyStatus")
    public List<MonthlyStatus> monthlyStatus(@RequestParam Integer year,
                                             @RequestParam String month) {
        List<MonthlyStatus> result = new ArrayList<>();
        final Query query =  new Query(Criteria.where("year").is(year).and("month").is(month));
        List<MonthlyTarget> monthlyList = mongoTemplate.find(query, MonthlyTarget.class);
        List<ExpenseDetails> expenseDetails = mongoTemplate.find(query, ExpenseDetails.class);
        for(MonthlyTarget monthlyTarget: monthlyList) {
            MonthlyStatus monthlyStatus = new MonthlyStatus();
            monthlyStatus.description = monthlyTarget.description;
            monthlyStatus.estimatedAmount = monthlyTarget.amount;
            monthlyStatus.expenseAmount = expenseDetails.stream()
                    .filter(a ->a.expenseOf.equals(monthlyTarget.description))
                    .mapToDouble(d->d.amount).sum();
            result.add(monthlyStatus);
        }
        // sorting
        result.sort((d1,d2)-> d1.description.compareToIgnoreCase(d2.description));
        return result;
    }
}

/*
 *  Copyright (C) 2024 Manikandan Narasimhan - All Rights Reserved
 *  * You may use, distribute and modify this code.
 *
 */

package com.audit.myexpense.controller;

import com.audit.myexpense.model.Dropdown;
import com.audit.myexpense.model.MonthlyTarget;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for planned Expenses
 *  @author Manikandan Narasimhan
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class PlannedExpenseController {

    private final MongoTemplate mongoTemplate;
    public PlannedExpenseController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param year year of expense
     * @param month Month of expense
     * @return monthlyTarget
     */
    @GetMapping("/plannedExpense")
    public Set<Dropdown> plannedExpense(@RequestParam Integer year,
                                        @RequestParam String month) {
        final Query query =  new Query(Criteria.where("year").is(year).and("month").is(month));
        return mongoTemplate.find(query, MonthlyTarget.class).stream()
                .map(v-> new Dropdown(v.description,v.description)).collect(Collectors.toSet());
    }
}

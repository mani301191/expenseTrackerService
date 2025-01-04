/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */
package com.audit.myexpense.controller;

import com.audit.myexpense.model.IncomeDetails;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 *  Controller to hold income details
 *  @author Manikandan Narasimhan
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/incomeTracker")
public class IncomeTrackerController {

    private final MongoTemplate mongoTemplate;
    public IncomeTrackerController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param incomeDetails income Data information
     * @return IncomeDetails
     */
    @PostMapping("/incomeDetail")
    public IncomeDetails incomeDetails(@Valid @RequestBody IncomeDetails incomeDetails) {

        Query query = new Query();
        query.with( Sort.by(Sort.Direction.DESC, "incomeId"));
        query.limit(1);
        IncomeDetails maxObject = mongoTemplate.findOne(query, IncomeDetails.class);
        incomeDetails.setIncomeId(maxObject!= null ?maxObject.getIncomeId()+1:0);
        Calendar cal = Calendar.getInstance();
        cal.setTime(incomeDetails.getIncomeDate());
        incomeDetails.setMonth(cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.ENGLISH));
        incomeDetails.setYear(cal.get(Calendar.YEAR));
        return mongoTemplate.insert(incomeDetails);
    }

    /**
     * @param year year of income
     * @param month Month of the income
     * @return Lit of IncomeDetails
     */
    @GetMapping("/incomeDetails")
    public List<IncomeDetails> incomeDetails(@RequestParam(required = false) Integer year,
                                             @RequestParam(required = false) String month) {
        final Query query = new Query();
        final List<Criteria> criteria = new ArrayList<>();
        if (year != null) {
            criteria.add(Criteria.where("year").is(year));
        }
        if (month != null && !month.isEmpty()) {
            criteria.add(Criteria.where("month").is(month));
        }

        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));
        }
        List<IncomeDetails> incomeDetails = mongoTemplate.find(query, IncomeDetails.class);
        // comprator logic
        Comparator<IncomeDetails> dateComparator = (c1, c2) -> {
            return c1.getIncomeDate().compareTo(c2.getIncomeDate());
        };
        Collections.sort(incomeDetails, dateComparator);
        return incomeDetails;
    }

    /**
     * @param incomeId income id to be deleted
     * @return response message
     */
    @DeleteMapping("/incomeDetail/{incomeId}")
    public Map<String, Object> deleteIncomeDetail(@PathVariable("incomeId") Integer incomeId) {
        Map<String, Object> body = new LinkedHashMap<>();
        IncomeDetails incomeObj = mongoTemplate.findById(incomeId,IncomeDetails.class);
        if(incomeObj !=null) {
            mongoTemplate.remove(incomeObj);
            body.put("message", "ExpenseId -"+incomeId + " deleted sucessfully" );
        } else {
            body.put("message ",  "ExpenseId -" + incomeId + " not found");
        }
        return body;
    }

}

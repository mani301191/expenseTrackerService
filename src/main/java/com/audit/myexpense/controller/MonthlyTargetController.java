/*
 *  Copyright (C) 2024 Manikandan Narasimhan - All Rights Reserved
 *  * You may use, distribute and modify this code.
 *
 */

package com.audit.myexpense.controller;

import com.audit.myexpense.model.MonthlyTarget;
import com.audit.myexpense.util.ExpenseCommonUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 * Controller for monthly Target
 *  @author Manikandan Narasimhan
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class MonthlyTargetController {

    private final MongoTemplate mongoTemplate;
    public MonthlyTargetController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param monthlyTargetList of MonthlyTarget
     * @return monthlyTarget
     */
    @PostMapping("/monthlyTarget")
    public Collection<MonthlyTarget> monthlyTargetDetail(@Valid @RequestBody Collection<MonthlyTarget> monthlyTargetList) {
        Calendar cal = Calendar.getInstance();
        for(MonthlyTarget monthlyTarget :monthlyTargetList) {
            cal.setTime(monthlyTarget.date);
            monthlyTarget.month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.ENGLISH);
            monthlyTarget.year = cal.get(Calendar.YEAR);
        }
        for( MonthlyTarget monthlyTarget: monthlyTargetList) {
            mongoTemplate.save(monthlyTarget);
        }
        return monthlyTargetList;
    }

    /**
     * @param year year of the Target
     * @param month month of the Target
     * @return monthlyTarget
     */
    @GetMapping("/monthlyTarget")
    public List<MonthlyTarget> monthlyTarget(@RequestParam Integer year,
                                             @RequestParam String month) {
        final Query query =  new Query(Criteria.where("year").is(year).and("month").is(month));
        return mongoTemplate.find(query, MonthlyTarget.class);
    }

    /**
     * @param id target id to be deleted
     * @return response message
     */
    @DeleteMapping("/monthlyTarget/{id}")
    public Map<String, Object> deleteMonthlyTarget(@PathVariable("id") String id) {
        Map<String, Object> body = new LinkedHashMap<>();
        MonthlyTarget obj = mongoTemplate.findById(id,MonthlyTarget.class);
        if(obj !=null) {
            mongoTemplate.remove(obj);
            body.put("message", "id -"+id + " deleted sucessfully" );
        } else {
            body.put("message ",  "id -" + id + " not found");
        }
        return body;
    }

    /**
     * @param year year of target to be cloned
     * @param month month of target to be cloned
     * @return monthlyTarget
     */
    @GetMapping("/monthlyTarget/clone")
    public Collection<MonthlyTarget> monthlyTargetDetailClone(
            @RequestParam Integer year,
            @RequestParam String month) {
        List<MonthlyTarget> monthlyData=this.monthlyTarget(year,month);
        List<MonthlyTarget> monthlyDataCopy= new ArrayList<>();
        for(MonthlyTarget data:monthlyData){
            MonthlyTarget dataCopy = new MonthlyTarget();
            dataCopy=data;
            dataCopy.date = new Date();
            dataCopy.id=UUID.randomUUID().toString();
            dataCopy.updatedDate = ExpenseCommonUtil.formattedDate(new Date());
            monthlyDataCopy.add(dataCopy);
        }
        return this.monthlyTargetDetail(monthlyDataCopy);
    }
}

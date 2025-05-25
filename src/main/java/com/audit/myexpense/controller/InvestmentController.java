/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.controller;

import com.audit.myexpense.model.Investments;
import com.audit.myexpense.util.ExpenseCommonUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Manikandan Narasimhan
 *
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/investment")
public class InvestmentController {
    private final MongoTemplate mongoTemplate;

    public InvestmentController( MongoTemplate mongoTemplate) {
        super();
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param investments investments
     * @return Investments
     */
    @PostMapping("/investDetail")
    public Investments saveInvestDetail(@Validated @RequestBody Investments investments) {
        investments.investId = UUID.randomUUID().toString();
        return mongoTemplate.insert(investments);
    }

    /**
     * @return List<Investments>
     */
    @GetMapping("/investments")
    public List<Investments> fetchInvestDetails() {
        return mongoTemplate.findAll(Investments.class);
    }

    /**
     * @param investId investId
     * @return Map<String, Object>
     */
    @DeleteMapping("/{investId}")
    public Map<String, Object> deleteInvestment(@PathVariable("investId") String investId) {
        Map<String, Object> body = new LinkedHashMap<>();
        Investments investments= mongoTemplate.findById(investId,Investments.class);
        if (investments != null) {
            mongoTemplate.remove(investments);
            body.put("message", "investment " + investments.investmentDetail + " deleted successfully");
        } else {
            body.put("message", investId + " not found");
        }
        return body;
    }

    /**
     * @param  data investment data
     * @return Map<String, Object>
     */
    @PatchMapping("/investDetail")
    public Map<String, Object> updateInvestment(@RequestBody Investments data) {
        Map<String, Object> body = new LinkedHashMap<>();
        Investments investments= mongoTemplate.findById(data.investId,Investments.class);
        if (investments != null) {
            investments.status=data.status;
            investments.additionalDetails=data.additionalDetails;
            investments.updatedDate= ExpenseCommonUtil.formattedDate(new Date());
            mongoTemplate.save(investments);
            body.put("message", "investment Status " + investments.investmentDetail + " updated successfully");
        } else {
            body.put("message", "Data not found");
        }
        return body;
    }
}

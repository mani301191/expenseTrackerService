/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.controller;

import com.audit.myexpense.model.Investments;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Manikandan Narasimhan
 *
 */
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
            body.put("message", "investment Id " + investments.investId + " deleted sucessfully");
        } else {
            body.put("message", investId + " not found");
        }
        return body;
    }
}

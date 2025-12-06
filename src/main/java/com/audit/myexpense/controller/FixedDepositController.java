/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.controller;

import com.audit.myexpense.model.FixedDeposit;
import com.audit.myexpense.util.ExpenseCommonUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author 
 *
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/investment")
public class FixedDepositController {

    private final MongoTemplate mongoTemplate;

    public FixedDepositController(MongoTemplate mongoTemplate) {
        super();
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Create a new Fixed Deposit
     *
     * @param fd FixedDeposit
     * @return FixedDeposit
     */
    @PostMapping("/fixedDeposit")
    public FixedDeposit createFixedDeposit(@Validated @RequestBody FixedDeposit fd) {
        fd.id = UUID.randomUUID().toString();
        fd.updatedDate = ExpenseCommonUtil.formattedDate(new Date());
        return mongoTemplate.insert(fd);
    }

    /**
     * Get all Fixed Deposits
     *
     * @return List<FixedDeposit>
     */
    @GetMapping("/fixedDeposits")
    public List<FixedDeposit> getAllFixedDeposits() {
        return mongoTemplate.findAll(FixedDeposit.class);
    }

    /**
     * Delete a Fixed Deposit
     *
     * @param id FixedDeposit id
     * @return Map<String, Object>
     */
    @DeleteMapping("/fixedDeposit/{id}")
    public Map<String, Object> deleteFixedDeposit(@PathVariable("id") String id) {
        Map<String, Object> body = new LinkedHashMap<>();

        FixedDeposit fd = mongoTemplate.findById(id, FixedDeposit.class);

        if (fd != null) {
            mongoTemplate.remove(fd);
            body.put("message", "Fixed Deposit from bank " + fd.bankName + " deleted successfully");
        } else {
            body.put("message", "Fixed Deposit with id " + id + " not found");
        }

        return body;
    }

    /**
     * Update an existing Fixed Deposit
     *
     * @param data incoming FD update data
     * @return Map<String, Object>
     */
    @PatchMapping("/fixedDeposit")
    public Map<String, Object> updateFixedDeposit(@RequestBody FixedDeposit data) {
        Map<String, Object> body = new LinkedHashMap<>();

        FixedDeposit fd = mongoTemplate.findById(data.id, FixedDeposit.class);

        if (fd != null) {

            // Update fields
            fd.bankName = data.bankName;
            fd.accountNumber = data.accountNumber;
            fd.openedDate = data.openedDate;
            fd.maturityDate = data.maturityDate;
            fd.interestRate = data.interestRate;
            fd.nomineeName = data.nomineeName;
            fd.depositAmount = data.depositAmount;
            fd.expectedMaturityAmount = data.expectedMaturityAmount;

            fd.updatedDate = ExpenseCommonUtil.formattedDate(new Date());
            mongoTemplate.save(fd);

            body.put("message", "Fixed Deposit of " + fd.bankName + " updated successfully");

        } else {
            body.put("message", "Data not found");
        }

        return body;
    }
}

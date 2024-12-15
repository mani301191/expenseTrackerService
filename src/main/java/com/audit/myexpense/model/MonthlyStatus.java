
/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */
package com.audit.myexpense.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

/**
 * @author Manikandan Narasimhan
 *
 */
public class MonthlyStatus {

    @Id
    @JsonProperty("description")
    public  String description;

    @JsonProperty("expenseAmount")
    public  double expenseAmount;

    @JsonProperty("estimatedAmount")
    public  double estimatedAmount;

    public MonthlyStatus() {
    }

    public MonthlyStatus(String description, double expenseAmount, double estimatedAmount) {
        this.description = description;
        this.expenseAmount = expenseAmount;
        this.estimatedAmount = estimatedAmount;
    }
}

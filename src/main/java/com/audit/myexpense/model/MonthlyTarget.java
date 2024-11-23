/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */
package com.audit.myexpense.model;

import com.audit.myexpense.util.ExpenseCommonUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Manikandan Narasimhan
 *
 */
@Document(collection = "myMonthlyTarget")
public class MonthlyTarget {

//    @JsonProperty("targetId")
//    public int targetId;

    @JsonProperty("year")
    public  int year;

    @Id
    @JsonProperty("month")
    public  String month;

    @NotNull
    @JsonFormat(pattern="dd/MM/yyyy")
    @JsonProperty("date")
    public  Date date;

    @NotNull
    @JsonProperty("amount")
    public  double amount;

    @JsonProperty("updatedDate")
    private String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

    public MonthlyTarget() {
    }

    public MonthlyTarget( int year, String month, double amount) {

        this.year = year;
        this.month = month;
        this.amount = amount;
    }

}
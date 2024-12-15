/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */
package com.audit.myexpense.model;

import com.audit.myexpense.util.ExpenseCommonUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

/**
 * @author Manikandan Narasimhan
 *
 */
@Document(collection = "myMonthlyTarget")
public class MonthlyTarget {

    @Id
    @JsonProperty("id")
    public String  id = UUID.randomUUID().toString();

    @JsonProperty("year")
    public  int year;

    @JsonProperty("month")
    public  String month;

    @NotNull
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSSX")
    @JsonProperty("date")
    public  Date date;

    @NotNull
    @JsonProperty("amount")
    public  double amount;

    @NotNull
    @JsonProperty("description")
    public  String description;

    @JsonIgnore
    @JsonProperty("updatedDate")
    private String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

    public MonthlyTarget() {
    }

    public MonthlyTarget( int year, String month,String description, double amount) {

        this.year = year;
        this.month = month;
        this.description=description;
        this.amount = amount;
    }

}
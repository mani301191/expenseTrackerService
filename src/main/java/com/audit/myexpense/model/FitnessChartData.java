/*
 *  Copyright (C) 2024 Manikandan Narasimhan - All Rights Reserved
 *  * You may use, distribute and modify this code.
 *
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

/**
 * @author Manikandan Narasimhan
 *
 */
@Document(collection = "fitnessWeightData")
public class FitnessChartData {

    @Id
    @JsonProperty("id")
    public  String id;

    @NotNull
    @JsonProperty("personName")
    public  String personName;

    @NotNull
    @JsonFormat(pattern="dd/MM/yyyy")
    @JsonProperty("date")
    public Date date;

    @JsonProperty("height")
    public  Double height = 0.0;

    @JsonProperty("weight")
    public  Double weight = 0.0;

    @JsonIgnore
    @JsonProperty("updatedDate")
    public String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

    public FitnessChartData() {
    }
}

/*
 *  Copyright (C) 2024 Manikandan Narasimhan - All Rights Reserved
 *  * You may use, distribute and modify this code.
 *
 */

package com.audit.myexpense.model;

import com.audit.myexpense.util.ExpenseCommonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Manikandan Narasimhan
 *
 */
@Document(collection = "fitnessDetails")
public class FitnessDetails {

    @Id
    @NotNull
    @JsonProperty("personName")
    public  String personName;

    @JsonProperty("personPic")
    public  String personPic;

    @JsonProperty("currentHeight")
    public  Double currentHeight = 0.0;

    @JsonProperty("currentWeight")
    public  Double currentWeight = 0.0;

    @Transient
    @JsonProperty("trend")
    public List<FitnessChartData> trend = new ArrayList<>();

    @JsonIgnore
    @JsonProperty("updatedDate")
    public String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

    public FitnessDetails() {
    }
}

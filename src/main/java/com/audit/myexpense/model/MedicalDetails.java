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
@Document(collection = "medicalDetails")
public class MedicalDetails {

    @Id
    @JsonProperty("id")
    public  String id;

    @NotNull
    @JsonProperty("patientName")
    public  String patientName;

    @NotNull
    @JsonFormat(pattern="dd/MM/yyyy")
    @JsonProperty("date")
    public Date date;

    @JsonProperty("problem")
    public  String problem;

    @JsonProperty("hospital")
    public  String hospital;

    @JsonProperty("docterName")
    public  String docterName;

    @JsonProperty("diagnosis")
    public  String diagnosis;

    @JsonProperty("otherDetails")
    public  String otherDetails;

    @JsonIgnore
    @JsonProperty("updatedDate")
    public String updatedDate = ExpenseCommonUtil.formattedDate(new Date());
}

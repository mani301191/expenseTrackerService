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
@Document(collection = "myInsuranceDetails")
public class InsuranceDetails {

    @Id
    @JsonProperty("insuranceId")
    public String insuranceId;

    @NotNull
    @JsonProperty("insuranceType")
    public String insuranceType;

    @NotNull
    @JsonProperty("insuranceProvider")
    public String insuranceProvider;

    @NotNull
    @JsonProperty("policyNumber")
    public String policyNumber;

    @NotNull
    @JsonProperty("nominee")
    public String nominee;

    @NotNull
    @JsonFormat(pattern="dd/MM/yyyy")
    @JsonProperty("startDate")
    public Date startDate;

    @NotNull
    @JsonFormat(pattern="dd/MM/yyyy")
    @JsonProperty("endDate")
    public Date endDate;

    @JsonProperty("additionalDetails")
    public String additionalDetails;

    @JsonIgnore
    @JsonProperty("updatedDate")
    public String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

    public InsuranceDetails() {
        super();
    }
}

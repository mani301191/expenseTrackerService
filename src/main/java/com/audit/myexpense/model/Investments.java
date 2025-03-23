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
@Document(collection = "myInvestments")
public class Investments {

    @Id
    @JsonProperty("investId")
    public String investId;

    @NotNull
    @JsonProperty("investment")
    public String investment;

    @NotNull
    @JsonProperty("investmentDetail")
    public String investmentDetail;

    @NotNull
    @JsonProperty("vendorAccountNumber")
    public String vendorAccountNumber;

    @NotNull
    @JsonProperty("nominee")
    public String nominee;

    @JsonProperty("additionalDetails")
    public String additionalDetails;

    @NotNull
    @JsonProperty("status")
    public String status;

    @JsonIgnore
    @JsonProperty("updatedDate")
    public String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

    public Investments() {
        super();
    }
}

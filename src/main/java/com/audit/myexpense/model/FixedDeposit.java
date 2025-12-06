package com.audit.myexpense.model;

import com.audit.myexpense.util.ExpenseCommonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author 
 * Fixed Deposit Model
 */
@Document(collection = "fixedDeposits")
public class FixedDeposit {

    @Id
    @JsonProperty("id")
    public String id;

    @NotNull
    @JsonProperty("bankName")
    public String bankName;

    @NotNull
    @JsonProperty("accountNumber")
    public String accountNumber;

    @NotNull
    @JsonProperty("openedDate")
    public String openedDate;

    @NotNull
    @JsonProperty("maturityDate")
    public String maturityDate;

    @NotNull
    @JsonProperty("interestRate")
    public Double interestRate;

    @NotNull
    @JsonProperty("nomineeName")
    public String nomineeName;

    @NotNull
    @JsonProperty("depositAmount")
    public Double depositAmount;

    @NotNull
    @JsonProperty("expectedMaturityAmount")
    public Double expectedMaturityAmount;

    @JsonIgnore
    @JsonProperty("updatedDate")
    public String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

    public FixedDeposit() {
        super();
    }
}

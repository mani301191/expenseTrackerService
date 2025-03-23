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
@Document(collection = "myAppliances")
public class Appliances {
    @Id
    @JsonProperty("appliancesId")
    public String appliancesId;

    @NotNull
    @JsonProperty("applianceName")
    public String applianceName;

    @NotNull
    @JsonProperty("amc")
    public String amc;

    @NotNull
    @JsonFormat(pattern="dd/MM/yyyy")
    @JsonProperty("amcEndDate")
    public Date amcEndDate;

    @JsonFormat(pattern="dd/MM/yyyy")
    @JsonProperty("lastServicedDate")
    public Date lastServicedDate;

    @JsonProperty("additionalDetails")
    public String additionalDetails;

    @JsonIgnore
    @JsonProperty("updatedDate")
    public String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

    public Appliances() {
        super();
    }
}

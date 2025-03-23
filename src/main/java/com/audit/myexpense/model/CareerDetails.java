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
@Document(collection = "myCareerDetail")
public class CareerDetails {

    @Id
    @JsonProperty("id")
    public String id;

    @NotNull
    @JsonProperty("recordType")
    public String recordType;

    @JsonProperty("orgName")
    public String orgName;

    @JsonProperty("designation")
    public String designation;

    @JsonProperty("comments")
    public String comments;

    @JsonFormat(pattern="dd/MM/yyyy")
    @JsonProperty("startDate")
    public Date startDate;

    @JsonFormat(pattern="dd/MM/yyyy")
    @JsonProperty("endDate")
    public Date endDate;

    @JsonIgnore
    @JsonProperty("updatedDate")
    public String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

    public CareerDetails() {
        super();
    }
}

/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */
package com.audit.myexpense.model;

import com.audit.myexpense.util.ExpenseCommonUtil;
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
@Document(collection = "profileDetail")
public class ProfileDetail {

    @Id
    @JsonProperty("profileId")
    public  int profileId;

    @NotNull
    @JsonProperty("profileName")
    public  String profileName;

    @JsonProperty("profilePic")
    public  String profilePic;

    @JsonIgnore
    @JsonProperty("updatedDate")
    public String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

    public ProfileDetail() {
    }
}

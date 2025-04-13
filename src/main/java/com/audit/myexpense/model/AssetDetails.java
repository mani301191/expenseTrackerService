/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.model;

import com.audit.myexpense.util.ExpenseCommonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Manikandan Narasimhan
 *
 */
@Document(collection = "myAssetDetails")
public class AssetDetails {

	@Id
	@JsonProperty("assetId")
	public String assetId;

	@NotNull
	@JsonProperty("assetType")
	public String assetType;

	@NotNull
	@JsonProperty("asset")
	public String asset;

	@NotNull
	@JsonProperty("description")
	public String description;

	@NotNull
	@JsonProperty("assetWeight")
	public double assetWeight;

	@JsonProperty("additionalDetails")
	public String additionalDetails;

	@NotNull
	@JsonProperty("status")
	public String status;

	@JsonIgnore
	@JsonProperty("updatedDate")
	public String updatedDate = ExpenseCommonUtil.formattedDate(new Date());
	
	public AssetDetails() {
		super();
	}
}

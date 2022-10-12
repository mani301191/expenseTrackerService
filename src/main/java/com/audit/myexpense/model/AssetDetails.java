/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Manikandan Narasimhan
 *
 */
@Document(collection = "myAssetDetails")
public class AssetDetails {

	@Id
	@JsonProperty("assetId")
	private int assetId;

	@JsonProperty("assetName")
	private String assetName;

	@JsonProperty("assetDocumentNumber")
	private String assetDocumentNumber;

	@JsonProperty("assetBoughtOn")
	private String assetBoughtOn;

	@JsonProperty("assetType")
	private String assetType;

	@JsonProperty("assetQty")
	private int assetQty;

	@JsonProperty("assetCurrentValue")
	private double assetCurrentValue;
	
	@JsonProperty("remarks")
	private String remarks;
	
	@JsonProperty("personAsset")
	private String personAsset;
	
	public AssetDetails() {
		super();
	}

	public AssetDetails(int assetId, String assetName, String assetDocumentNumber, String assetBoughtOn,
			String assetType, int assetQty, double assetCurrentValue,String remarks) {
		super();
		this.assetId = assetId;
		this.assetName = assetName;
		this.assetDocumentNumber = assetDocumentNumber;
		this.assetBoughtOn = assetBoughtOn;
		this.assetType = assetType;
		this.assetQty = assetQty;
		this.assetCurrentValue = assetCurrentValue;
		this.remarks = remarks;
	}

	public int getAssetId() {
		return assetId;
	}

	public void setAssetId(int assetId) {
		this.assetId = assetId;
	}

	public String getAssetName() {
		return assetName;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public String getAssetDocumentNumber() {
		return assetDocumentNumber;
	}

	public void setAssetDocumentNumber(String assetDocumentNumber) {
		this.assetDocumentNumber = assetDocumentNumber;
	}

	public String getAssetBoughtOn() {
		return assetBoughtOn;
	}

	public void setAssetBoughtOn(String assetBoughtOn) {
		this.assetBoughtOn = assetBoughtOn;
	}

	public String getAssetType() {
		return assetType;
	}

	public void setAssetType(String assetType) {
		this.assetType = assetType;
	}

	public int getAssetQty() {
		return assetQty;
	}

	public void setAssetQty(int assetQty) {
		this.assetQty = assetQty;
	}

	public double getAssetCurrentValue() {
		return assetCurrentValue;
	}

	public void setAssetCurrentValue(double assetCurrentValue) {
		this.assetCurrentValue = assetCurrentValue;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getPersonAsset() {
		return personAsset;
	}

	public void setPersonAsset(String personAsset) {
		this.personAsset = personAsset;
	}
	
}

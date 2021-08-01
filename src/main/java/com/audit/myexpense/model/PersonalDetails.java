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
@Document(collection = "myPersonalDetails")
public class PersonalDetails {
	@Id
	@JsonProperty("detailId")
	private int detailId;

	@JsonProperty("recordName")
	private String recordName;

	@JsonProperty("recordNumber")
	private String recordNumber;

	@JsonProperty("recordValidity")
	private String recordValidity = "N/A";

	@JsonProperty("remarks")
	private String remarks;
	
	public PersonalDetails() {
		super();
	}

	public PersonalDetails(int detailId, String recordName, String recordNumber, String recordValidity,String remarks) {
		super();
		this.detailId = detailId;
		this.recordName = recordName;
		this.recordNumber = recordNumber;
		this.recordValidity = recordValidity;
		this.remarks = remarks;
	}

	public int getDetailId() {
		return detailId;
	}

	public void setDetailId(int detailId) {
		this.detailId = detailId;
	}

	public String getRecordName() {
		return recordName;
	}

	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}

	public String getRecordNumber() {
		return recordNumber;
	}

	public void setRecordNumber(String recordNumber) {
		this.recordNumber = recordNumber;
	}

	public String getRecordValidity() {
		return recordValidity;
	}

	public void setRecordValidity(String recordValidity) {
		this.recordValidity = recordValidity;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

}

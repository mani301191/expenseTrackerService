/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.audit.myexpense.util.ExpenseCommonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

/**
 * @author Manikandan Narasimhan
 *
 */
@Document(collection = "myIncomeDetail")
public class IncomeDetails {

	@Id
	@JsonProperty("incomeId")
	private  int incomeId;

	@JsonProperty("year")
	private  int year;
	
	@JsonProperty("month")
	private  String month;

	@NotNull
	@JsonFormat(pattern="dd/MM/yyyy")
	@JsonProperty("incomeDate")
	private  Date incomeDate;

	@NotNull
	@JsonProperty("amount")
	private  double amount;

	@NotNull
	@JsonProperty("source")
	private  String source;
	
	@JsonProperty("updatedDate")
	private String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

	public IncomeDetails() {
		super();
	}

	public int getIncomeId() {
		return incomeId;
	}

	public void setIncomeId(int incomeId) {
		this.incomeId = incomeId;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {

		this.year = year;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public Date getIncomeDate() {
		return incomeDate;
	}

	public void setIncomeDate(Date incomeDate) {
		this.incomeDate = incomeDate;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}



}

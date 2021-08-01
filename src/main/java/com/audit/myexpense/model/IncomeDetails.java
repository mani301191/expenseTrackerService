/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.audit.myexpense.util.ExpenseCommonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Manikandan Narasimhan
 *
 */
@Document(collection = "myIncome")
public class IncomeDetails {

	@Id
	@JsonProperty("incomeId")
	private  int incomeId;
	
	@JsonProperty("year")
	private  int year;
	
	@JsonProperty("month")
	private  String month;
	
	@JsonProperty("incomeDate")
	private  String incomeDate;
	
	@JsonProperty("amount")
	private  double amount;
	
	@JsonProperty("description")
	private  String description;
	
	@JsonProperty("updatedDate")
	private String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

	public IncomeDetails() {
		super();
	}

	public IncomeDetails(int incomeId, int year, String month, String incomeDate, double amount, String description,
			String updatedDate) {
		super();
		this.incomeId = incomeId;
		this.year = year;
		this.month = month;
		this.incomeDate = incomeDate;
		this.amount = amount;
		this.description = description;
		this.updatedDate = updatedDate;
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

	public String getIncomeDate() {
		return incomeDate;
	}

	public void setIncomeDate(String incomeDate) {
		this.incomeDate = incomeDate;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}
	
	

}

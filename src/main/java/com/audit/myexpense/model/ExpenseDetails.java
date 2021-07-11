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
@Document(collection = "myExpense")
public class ExpenseDetails {


	@Id
	@JsonProperty("expenseId")
	private  int expenseId;
	
	@JsonProperty("year")
	private  int year;
	
	@JsonProperty("month")
	private  String month;
	
	@JsonProperty("expenseDate")
	private  String expenseDate;
	
	@JsonProperty("amount")
	private  double amount;
	
	@JsonProperty("plannedExpense")
	private  String plannedExpense;
	
	@JsonProperty("description")
	private  String description;
	
	@JsonProperty("expenseType")
	private  String expenseType;
	
	@JsonProperty("updatedDate")
	private String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

	public ExpenseDetails() {
		// TODO Auto-generated constructor stub
	}

	public ExpenseDetails(int expenseId, int year, String month, String expenseDate, double amount,
			String plannedExpense, String description, String expenseType, String updatedDate) {
		super();
		this.expenseId = expenseId;
		this.year = year;
		this.month = month;
		this.expenseDate = expenseDate;
		this.amount = amount;
		this.plannedExpense = plannedExpense;
		this.description = description;
		this.expenseType = expenseType;
		this.updatedDate = updatedDate;
	}

	public int getExpenseId() {
		return expenseId;
	}

	public void setExpenseId(int expenseId) {
		this.expenseId = expenseId;
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

	public String getExpenseDate() {
		return expenseDate;
	}

	public void setExpenseDate(String expenseDate) {
		this.expenseDate = expenseDate;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getPlannedExpense() {
		return plannedExpense;
	}

	public void setPlannedExpense(String plannedExpense) {
		this.plannedExpense = plannedExpense;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExpenseType() {
		return expenseType;
	}

	public void setExpenseType(String expenseType) {
		this.expenseType = expenseType;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}


}
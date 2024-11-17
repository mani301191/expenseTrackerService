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
@Document(collection = "myExpenseDetail")
public class ExpenseDetails {


	@Id
	@JsonProperty("expenseId")
	private  int expenseId;
	
	@JsonProperty("year")
	private  int year;
	
	@JsonProperty("month")
	private  String month;

	@NotNull
	@JsonFormat(pattern="dd/MM/yyyy")
	@JsonProperty("expenseDate")
	private  Date expenseDate;

	@NotNull
	@JsonProperty("amount")
	private  double amount;

	@NotNull
	@JsonProperty("expenseOf")
	private  String expenseOf;

	@NotNull
	@JsonProperty("description")
	private  String description;

	@NotNull
	@JsonProperty("expenseType")
	private  String expenseType;
	
	@JsonProperty("updatedDate")
	private String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

	public ExpenseDetails() {
		// TODO Auto-generated constructor stub
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

	public Date getExpenseDate() {
		return expenseDate;
	}

	public void setExpenseDate(Date expenseDate) {
		this.expenseDate = expenseDate;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getExpenseOf() {
		return expenseOf;
	}

	public void setExpenseOf(String expenseOf) {
		this.expenseOf = expenseOf;
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
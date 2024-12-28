/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class ExpenseDetails implements Serializable {


	@Id
	@JsonProperty("expenseId")
	public  int expenseId;
	
	@JsonProperty("year")
	public  int year;
	
	@JsonProperty("month")
	public  String month;

	@NotNull
	@JsonFormat(pattern="dd/MM/yyyy")
	@JsonProperty("expenseDate")
	public  Date expenseDate;

	@NotNull
	@JsonProperty("amount")
	public  double amount;

	@NotNull
	@JsonProperty("expenseOf")
	public  String expenseOf;

	@JsonProperty("description")
	public  String description;

	@NotNull
	@JsonProperty("expenseType")
	public  String expenseType;

    @JsonIgnore
	@JsonProperty("updatedDate")
	public String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

	public ExpenseDetails() {
		// TODO Auto-generated constructor stub
	}
}
/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.audit.myexpense.model.MonthlySummary;
import com.audit.myexpense.model.YearlySummary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import com.audit.myexpense.model.ExpenseDetails;
import com.audit.myexpense.model.IncomeDetails;

import javax.validation.Valid;


import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author Manikandan Narasimhan
 *
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/expenseTracker")
public class ExpenseTrackerController {

	private final MongoTemplate mongoTemplate;
	private final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	public ExpenseTrackerController( MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	/**
	 * @param expenseDetails
	 * @return ExpenseDetails
	 */
	@PostMapping("/expenseDetail")
	public ExpenseDetails expenseDetail(@Valid @RequestBody ExpenseDetails expenseDetails) {
		Query query = new Query();
		query.with( Sort.by(Sort.Direction.DESC, "expenseId"));
		query.limit(1);
		ExpenseDetails maxObject = mongoTemplate.findOne(query, ExpenseDetails.class);
		expenseDetails.setExpenseId(maxObject!= null ?maxObject.getExpenseId()+1:0);
		Calendar cal = Calendar.getInstance();
		cal.setTime(expenseDetails.getExpenseDate());
		expenseDetails.setMonth( cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.ENGLISH));
		expenseDetails.setYear(cal.get(Calendar.YEAR));
		return mongoTemplate.insert(expenseDetails);
	}

	/**
	 * @param year
	 * @param month
	 * @param expenseOf
	 * @param expenseType
	 * @return list of ExpenseDetails
	 */
	@GetMapping("/expenseDetails")
	public List<ExpenseDetails> expenseDetails(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) String month, @RequestParam(required = false) String expenseOf,
			@RequestParam(required = false) String expenseType) {
		final Query query = new Query();
		final List<Criteria> criteria = new ArrayList<>();
		if (year != null) {
			criteria.add(Criteria.where("year").is(year));
		}
		if (month != null && !month.isEmpty()) {
			criteria.add(Criteria.where("month").is(month));
		}
		if (expenseOf != null && !expenseOf.isEmpty()) {
			criteria.add(Criteria.where("expenseOf").in(expenseOf));
		}
		if (expenseType != null && !expenseType.isEmpty()) {
			criteria.add(Criteria.where("expenseType").is(expenseType));
		}

		if (!criteria.isEmpty()) {
			query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));
		}
		List<ExpenseDetails> expenseDetails = mongoTemplate.find(query, ExpenseDetails.class);
		// comprator logic
		Comparator<ExpenseDetails> dateComparator = (c1, c2) -> {
			return c1.getExpenseDate().getTime() > c2.getExpenseDate().getTime()?-1:1;
		};
		Collections.sort(expenseDetails, dateComparator);
		return expenseDetails;
	}

	/**
	 * @param expenseId
	 * @return response message
	 */
	@DeleteMapping("/expenseDetail/{expenseId}")
	public Map<String, Object> deleteExpenseDetail(@PathVariable("expenseId") Integer expenseId) {
		Map<String, Object> body = new LinkedHashMap<>();
		ExpenseDetails expenseObj = mongoTemplate.findById(expenseId,ExpenseDetails.class);
		if(expenseObj !=null) {
			mongoTemplate.remove(expenseObj);
			body.put("message", "ExpenseId -"+expenseId.toString() + " deleted sucessfully" );
		} else {
			body.put("message ",  "ExpenseId -" + expenseId.toString() + " not found");
		}
		return body;
	}

	/**
	 * @param incomeDetails
	 * @return IncomeDetails
	 */
	@PostMapping("/incomeDetail")
	public IncomeDetails incomeDetails(@Valid @RequestBody IncomeDetails incomeDetails) {

		Query query = new Query();
		query.with( Sort.by(Sort.Direction.DESC, "incomeId"));
		query.limit(1);
		IncomeDetails maxObject = mongoTemplate.findOne(query, IncomeDetails.class);
		incomeDetails.setIncomeId(maxObject!= null ?maxObject.getIncomeId()+1:0);
		Calendar cal = Calendar.getInstance();
		cal.setTime(incomeDetails.getIncomeDate());
		incomeDetails.setMonth(cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.ENGLISH));
		incomeDetails.setYear(cal.get(Calendar.YEAR));
		return mongoTemplate.insert(incomeDetails);
	}

	/**
	 * @param year
	 * @param month
	 * @return Lit of IncomeDetails
	 */
	@GetMapping("/incomeDetails")
	public List<IncomeDetails> incomeDetails(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) String month) {
		final Query query = new Query();
		final List<Criteria> criteria = new ArrayList<>();
		if (year != null) {
			criteria.add(Criteria.where("year").is(year));
		}
		if (month != null && !month.isEmpty()) {
			criteria.add(Criteria.where("month").is(month));
		}

		if (!criteria.isEmpty()) {
			query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));
		}
		List<IncomeDetails> incomeDetails = mongoTemplate.find(query, IncomeDetails.class);
		// comprator logic
		Comparator<IncomeDetails> dateComparator = (c1, c2) -> {
			return c1.getIncomeDate().compareTo(c2.getIncomeDate());
		};
		Collections.sort(incomeDetails, dateComparator);
		return incomeDetails;
	}

	/**
	 * @param incomeId
	 * @return response message
	 */
	@DeleteMapping("/incomeDetail/{incomeId}")
	public Map<String, Object> deleteIncomeDetail(@PathVariable("incomeId") Integer incomeId) {
		Map<String, Object> body = new LinkedHashMap<>();
		IncomeDetails incomeObj = mongoTemplate.findById(incomeId,IncomeDetails.class);
		if(incomeObj !=null) {
			mongoTemplate.remove(incomeObj);
			body.put("message", "ExpenseId -"+incomeId.toString() + " deleted sucessfully" );
		} else {
			body.put("message ",  "ExpenseId -" + incomeId.toString() + " not found");
		}
		return body;
	}


	@GetMapping("/monthlySummary")
	public List<MonthlySummary> monthlySummary(@RequestParam(required = true) Integer year,
										 @RequestParam(required = false) String month) {
		List<MonthlySummary> response = new ArrayList<>();
		final List<Criteria> criteria = new ArrayList<>();
		if (year != null) {
			criteria.add(Criteria.where("year").is(year));
		}
		if (month != null && !month.isEmpty()) {
			criteria.add(Criteria.where("month").is(month));
		}

	   Map<String,MonthlySummary> expenseSummaryMap = aggregationResults(criteria,"expense","myExpenseDetail")
			.getMappedResults().stream().collect(Collectors.toMap(exp->exp.getYear()+exp.getMonth(),exp->exp));

		Map<String,MonthlySummary> incomeSummaryMap =  aggregationResults(criteria,"income","myIncomeDetail")
				.getMappedResults().stream().collect(Collectors.toMap(exp->exp.getYear()+exp.getMonth(),exp->exp));

		for(MonthlySummary summary :expenseSummaryMap.values() ){
			MonthlySummary incomeDetail = incomeSummaryMap.get(summary.getYear()+summary.getMonth());
			summary.setIncome(incomeDetail!=null?incomeDetail.getIncome():0.0);
			summary.setSavings(incomeDetail!=null?incomeDetail.getIncome()-summary.getExpense():0.0-summary.getExpense());
			response.add(summary);
		}
		for(MonthlySummary incomeSummary :incomeSummaryMap.values()) {
			if(expenseSummaryMap.get(incomeSummary.getYear()+incomeSummary.getMonth()) == null) {
				incomeSummary.setSavings(incomeSummary.getIncome());
				response.add(incomeSummary);
			}
		}
		return response;
	}

	@GetMapping("/yearlySummary")
	public List<YearlySummary> yearlySummary() {
		List<YearlySummary> response = new ArrayList<>();

		Map<Integer, YearlySummary> expenseSummaryMap = aggregationYearlyResults("expense","myExpenseDetail")
				.getMappedResults().stream().collect(Collectors.toMap(exp->exp.getYear(),exp->exp));

		Map<Integer,YearlySummary> incomeSummaryMap =  aggregationYearlyResults("income","myIncomeDetail")
				.getMappedResults().stream().collect(Collectors.toMap(exp->exp.getYear(),exp->exp));

		for(YearlySummary summary :expenseSummaryMap.values() ){
			YearlySummary incomeDetail = incomeSummaryMap.get(summary.getYear());
			summary.setIncome(incomeDetail!=null?incomeDetail.getIncome():0.0);
			summary.setSavings(incomeDetail!=null?incomeDetail.getIncome()-summary.getExpense():0.0-summary.getExpense());
			response.add(summary);
		}
		for(YearlySummary incomeSummary :incomeSummaryMap.values()) {
			if(expenseSummaryMap.get(incomeSummary.getYear()) == null) {
				incomeSummary.setSavings(incomeSummary.getIncome());
				response.add(incomeSummary);
			}
		}
		return response;
	}

	private AggregationResults<MonthlySummary> aggregationResults(List<Criteria> criteria,String attribute,String collectionName) {
		Aggregation agg = newAggregation(
				match( new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()]))),
				group("year","month").sum("amount").as(attribute),
				project(attribute,"month","year")
		);
		AggregationResults<MonthlySummary> expenseResults = mongoTemplate.aggregate(agg,collectionName ,MonthlySummary.class);
	return expenseResults;
	}

	private AggregationResults<YearlySummary> aggregationYearlyResults( String attribute,String collectionName) {
		Aggregation yearlyAgg = newAggregation(
				group("year").sum("amount").as(attribute),
				project(attribute,"year")
		);
		AggregationResults<YearlySummary> expenseResults = mongoTemplate.aggregate(yearlyAgg,collectionName ,YearlySummary.class);
		return expenseResults;
	}
}

/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.controller;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.audit.myexpense.model.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

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
		expenseDetails.expenseId= maxObject!= null ?maxObject.expenseId+1:0;
		Calendar cal = Calendar.getInstance();
		cal.setTime(expenseDetails.expenseDate);
		expenseDetails.month= cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.ENGLISH);
		expenseDetails.year=cal.get(Calendar.YEAR);
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
			return c1.expenseDate.getTime() > c2.expenseDate.getTime()?-1:1;
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
		// estimate datas
		final Query query =  new Query(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));
		List<MonthlyTarget> result = mongoTemplate.find(query, MonthlyTarget.class);
		Map<String,Double> estimateMap = result.stream().collect(Collectors.toMap(r->r.year+r.month,r->r.amount));

		for(MonthlySummary summary :expenseSummaryMap.values() ){
			MonthlySummary incomeDetail = incomeSummaryMap.get(summary.getYear()+summary.getMonth());
			summary.setIncome(incomeDetail!=null?incomeDetail.getIncome():0.0);
			summary.setSavings(incomeDetail!=null?incomeDetail.getIncome()-summary.getExpense():0.0-summary.getExpense());
			summary.setEstimated(estimateMap.get(summary.getYear()+summary.getMonth()));
			response.add(summary);
		}
		for(MonthlySummary incomeSummary :incomeSummaryMap.values()) {
			if(expenseSummaryMap.get(incomeSummary.getYear()+incomeSummary.getMonth()) == null) {
				incomeSummary.setSavings(incomeSummary.getIncome());
				incomeSummary.setEstimated(estimateMap.get(incomeSummary.getYear()+incomeSummary.getMonth()));
				response.add(incomeSummary);
			}
		}

		return response;
	}

	@GetMapping("/yearlySummary")
	public List<YearlySummary> yearlySummary(@RequestParam(required = false) Integer year) {
		List<YearlySummary> response = new ArrayList<>();
		final Query query = year==null? new Query() : new Query(Criteria.where("year").is(year));

		//Fetch all documents from db
		List<IncomeDetails> incomeResult = mongoTemplate.find(query, IncomeDetails.class);
		List<ExpenseDetails> expenseResult = mongoTemplate.find(query, ExpenseDetails.class);
		List<MonthlyTarget> estimatedResult = mongoTemplate.find(query, MonthlyTarget.class);
		// map based on year
		Map<Integer,List<IncomeDetails>> incomeMap = incomeResult.stream().collect(Collectors.groupingBy(r->r.getYear()));
		Map<Integer,List<ExpenseDetails>> expenseMap = expenseResult.stream().collect(Collectors.groupingBy(r->r.year));
		Map<Integer,List<MonthlyTarget>> estimateMap = estimatedResult.stream().collect(Collectors.groupingBy(r->r.year));

		for(Integer key: expenseMap.keySet() ){
			YearlySummary yearlyData = new YearlySummary();
			//get the data based on year
			List<IncomeDetails> incomeList = incomeMap.get(key);
			List<ExpenseDetails> expensList = expenseMap.get(key);
			List<MonthlyTarget> estimateList = estimateMap.get(key);
			List<Category> categorylist = new ArrayList<>();
			// sum all the date based on year
			yearlyData.year = key;
			yearlyData.income = incomeList!=null?incomeList.stream().mapToDouble(r->r.getAmount()).sum():0.0;
			yearlyData.expense = expensList!=null?expensList.stream().mapToDouble(r->r.amount).sum():0.0;
			yearlyData.estimated = estimateList!=null?estimateList.stream().mapToDouble(r->r.amount).sum():0.0;
			yearlyData.savings = yearlyData.income - yearlyData.expense;
            //expense category
			Map<String,List<ExpenseDetails>> categoryMap = expenseMap.get(key)
					.stream().collect(Collectors.groupingBy(r->r.expenseType));
			//category Type data
			for(String category: categoryMap.keySet()) {
				Category categoryType = new Category();
				categoryType.expenseType = category;
				List<ExpenseDetails> catList= categoryMap.get(category);
				categoryType.amount = catList!=null ? catList.stream().mapToDouble(r->r.amount).sum():0.0;
				categorylist.add(categoryType);
			}
			yearlyData.category=categorylist;
			response.add(yearlyData);
		}
		// comprator logic
		Comparator<YearlySummary> dateComparator = (c1, c2) -> {
			return c1.year-c2.year;
		};

		Collections.sort(response, dateComparator);
		return response;
	}
	/**
	 * @param monthlyTarget
	 * @return monthlyTarget
	 */
	@PostMapping("/monthlyTarget")
	public MonthlyTarget monthlyTargetDetail(@Valid @RequestBody MonthlyTarget monthlyTarget) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(monthlyTarget.date);
		monthlyTarget.month=cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.ENGLISH);
		monthlyTarget.year=cal.get(Calendar.YEAR);
		return mongoTemplate.save(monthlyTarget);
	}

	/**
	 * @param year
	 * @param month
	 * @return monthlyTarget
	 */
	@GetMapping("/monthlyTarget")
	public MonthlyTarget monthlyTarget(@RequestParam(required = true) Integer year,
									   @RequestParam(required = true) String month) {
		final Query query =  new Query(Criteria.where("year").is(year).and("month").is(month));
		MonthlyTarget result = mongoTemplate.findOne(query, MonthlyTarget.class);
		return result != null ? result : new MonthlyTarget();
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

}

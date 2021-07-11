/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.audit.myexpense.model.ExpenseDetails;
import com.audit.myexpense.repository.MyExpenseRepository;

/**
 * @author Manikandan Narasimhan
 *
 */
@RestController
@RequestMapping("/api/expenseTracker")
public class ExpenseTrackerController {

	private MyExpenseRepository myExpenseRepository;
	private MongoTemplate mongoTemplate;

	public ExpenseTrackerController(MyExpenseRepository myExpenseRepository, MongoTemplate mongoTemplate) {
		this.myExpenseRepository = myExpenseRepository;
		this.mongoTemplate = mongoTemplate;
	}

	/**
	 * @param expenseDetails
	 * @return ExpenseDetails
	 */
	@PostMapping("/saveExpenseDetails")
	public ExpenseDetails saveExpenseDetails(@RequestBody ExpenseDetails expenseDetails) {
		// fetching all data and sorting the data
		List<ExpenseDetails> existingData = myExpenseRepository.findAll().stream()
				.sorted(Comparator.comparingInt(ExpenseDetails::getExpenseId).reversed()).collect(Collectors.toList());
		// setting the expense id by taking the maximum id from existing record
		expenseDetails.setExpenseId(existingData.size() > 0 ? existingData.get(0).getExpenseId() + 1 : 1);
		return myExpenseRepository.insert(expenseDetails);
	}

	/**
	 * @return List of Expense Details
	 */
	@GetMapping("/expenseDetails")
	public List<ExpenseDetails> fetchExpenseDetails() {
		return myExpenseRepository.findAll();
	}

	@GetMapping("/getExpenseDetails")
	public List<ExpenseDetails> getExpenseDetails(@RequestParam(required = false) Integer year,
			@RequestParam(required = false) String month, @RequestParam(required = false) String plannedExpense,
			@RequestParam(required = false) String expenseType) {
		final Query query = new Query();
		final List<Criteria> criteria = new ArrayList<>();
		if (year != null) {
			criteria.add(Criteria.where("year").is(year));
		}
		if (month != null && !month.isEmpty()) {
			criteria.add(Criteria.where("month").is(month));
		}
		if (plannedExpense != null && !plannedExpense.isEmpty()) {
			criteria.add(Criteria.where("plannedExpense").in(plannedExpense));
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
			return c1.getExpenseDate().compareTo(c2.getExpenseDate());
		};
		Collections.sort(expenseDetails, dateComparator);
		return expenseDetails;
	}

	/**
	 * @param expenseDetails
	 * @return ExpenseDetails
	 */
	@PostMapping("/expenseDetail")
	public ExpenseDetails updateExpenseDetails(@RequestBody ExpenseDetails expenseDetails) {
		Optional<ExpenseDetails> expenseData = myExpenseRepository.findById(expenseDetails.getExpenseId());
		ExpenseDetails updateExpense = expenseData.get();
		updateExpense.setAmount(expenseDetails.getAmount());
		updateExpense.setExpenseDate(expenseDetails.getExpenseDate());
		updateExpense.setDescription(expenseDetails.getDescription());
		updateExpense.setExpenseType(expenseDetails.getExpenseType());
		updateExpense.setMonth(expenseDetails.getMonth());
		updateExpense.setPlannedExpense(expenseDetails.getPlannedExpense());
		updateExpense.setYear(expenseDetails.getYear());
		updateExpense.setUpdatedDate(expenseDetails.getUpdatedDate());
		return myExpenseRepository.save(updateExpense);

	}

	/**
	 * @param expenseId
	 * @return response message
	 */
	@DeleteMapping("/expenseDetail/{expenseId}")
	public Map<String, Object> deleteExpenseDetail(@PathVariable("expenseId") Integer expenseId) {
		Map<String, Object> body = new LinkedHashMap<>();
		if (myExpenseRepository.findById(expenseId) != null) {
			myExpenseRepository.deleteById(expenseId);
			body.put("message", "Expense Id " + expenseId.toString() + " deleted sucessfully");
		} else {
			body.put("message", expenseId.toString() + " not found");
		}
		return body;
	}
}

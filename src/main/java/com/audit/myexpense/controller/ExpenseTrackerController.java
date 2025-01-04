/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.controller;

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
 * Controller to hold expense details
 * @author Manikandan Narasimhan
 *
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/expenseTracker")
public class ExpenseTrackerController {

	private final MongoTemplate mongoTemplate;
	public ExpenseTrackerController( MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	/**
	 * @param expenseDetails expenseDetails with date and send info
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
	 * @param year year of expense
	 * @param month month of the expense
	 * @param expenseOf expense belongs to
	 * @param expenseType type of expense
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
	 * @param expenseId expense Id to be deleted
	 * @return response message
	 */
	@DeleteMapping("/expenseDetail/{expenseId}")
	public Map<String, Object> deleteExpenseDetail(@PathVariable("expenseId") Integer expenseId) {
		Map<String, Object> body = new LinkedHashMap<>();
		ExpenseDetails expenseObj = mongoTemplate.findById(expenseId,ExpenseDetails.class);
		if(expenseObj !=null) {
			mongoTemplate.remove(expenseObj);
			body.put("message", "ExpenseId -"+expenseId + " deleted sucessfully" );
		} else {
			body.put("message ",  "ExpenseId -" + expenseId + " not found");
		}
		return body;
	}
}

/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.controller;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audit.myexpense.model.PersonalDetails;
import com.audit.myexpense.repository.PersonalDetailRepository;

/**
 * @author Manikandan Narasimhan
 *
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/personalDetail")
public class PersonalDetailController {

	private PersonalDetailRepository personalDetailRepository;

	public PersonalDetailController(PersonalDetailRepository personalDetailRepository) {
		super();
		this.personalDetailRepository = personalDetailRepository;
	}

	/**
	 * @param personalDetails
	 * @return PersonalDetails
	 */
	@PostMapping("/savePersonalDetail")
	public PersonalDetails savePersonalDetail(@RequestBody PersonalDetails personalDetails) {
		// fetching all data and sorting the data
		List<PersonalDetails> personalData = personalDetailRepository.findAll().stream()
				.sorted(Comparator.comparingInt(PersonalDetails::getDetailId).reversed()).collect(Collectors.toList());
		// setting the personalData id by taking the maximum id from existing record
		personalDetails.setDetailId(personalData.size() > 0 ? personalData.get(0).getDetailId() + 1 : 1);
		return personalDetailRepository.insert(personalDetails);
	}

	/**
	 * @return List<PersonalDetails>
	 */
	@GetMapping("/personalDetails")
	public List<PersonalDetails> fetchPersonalDetails() {
		return personalDetailRepository.findAll();
	}

	/**
	 * @param detailId
	 * @return Map<String, Object>
	 */
	@DeleteMapping("/{detailId}")
	public Map<String, Object> deletePersonalDetail(@PathVariable("detailId") Integer detailId) {
		Map<String, Object> body = new LinkedHashMap<>();
		if (personalDetailRepository.findById(detailId) != null) {
			personalDetailRepository.deleteById(detailId);
			body.put("message", "detail Id " + detailId.toString() + " deleted sucessfully");
		} else {
			body.put("message", detailId.toString() + " not found");
		}
		return body;
	}
}

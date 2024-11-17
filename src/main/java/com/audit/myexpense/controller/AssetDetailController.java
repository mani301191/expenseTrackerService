/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.controller;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audit.myexpense.model.AssetDetails;
import com.audit.myexpense.repository.AssetDetailRepository;

/**
 * @author Manikandan Narasimhan
 *
 */
@RestController
@RequestMapping("/api/assetDetail")
public class AssetDetailController {

	private AssetDetailRepository assetDetailRepository;

	public AssetDetailController(AssetDetailRepository assetDetailRepository) {
		super();
		this.assetDetailRepository = assetDetailRepository;
	}

	/**
	 * @param assetDetails
	 * @return AssetDetails
	 */
	@PostMapping("/saveAssetDetail")
	public AssetDetails saveAssetDetail(@RequestBody AssetDetails assetDetails) {
		// fetching all data and sorting the data
		List<AssetDetails> assetData = assetDetailRepository.findAll().stream()
				.sorted(Comparator.comparingInt(AssetDetails::getAssetId).reversed()).collect(Collectors.toList());
		// setting the asset id by taking the maximum id from existing record
		assetDetails.setAssetId(assetData.size() > 0 ? assetData.get(0).getAssetId() + 1 : 1);
		return assetDetailRepository.insert(assetDetails);
	}
	
	/**
	 * @return List<AssetDetails>
	 */
	@GetMapping("/assetDetails")
	public List<AssetDetails> fetchAssetDetails() {
		return assetDetailRepository.findAll();
	}

	/**
	 * @param assetId
	 * @return Map<String, Object>
	 */
	@DeleteMapping("/{assetId}")
	public Map<String, Object> deletePersonalDetail(@PathVariable("assetId") Integer assetId) {
		Map<String, Object> body = new LinkedHashMap<>();
		if (assetDetailRepository.findById(assetId) != null) {
			assetDetailRepository.deleteById(assetId);
			body.put("message", "asset Id " + assetId.toString() + " deleted sucessfully");
		} else {
			body.put("message", assetId.toString() + " not found");
		}
		return body;
	}
}

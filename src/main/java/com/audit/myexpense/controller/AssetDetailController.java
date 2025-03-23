/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.controller;

import java.util.*;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.audit.myexpense.model.AssetDetails;

/**
 * @author Manikandan Narasimhan
 *
 */
@RestController
@RequestMapping("/api/asset")
public class AssetDetailController {

	private final MongoTemplate mongoTemplate;

	public AssetDetailController( MongoTemplate mongoTemplate) {
		super();
		this.mongoTemplate = mongoTemplate;
	}

	/**
	 * @param assetDetails assetDetails
	 * @return AssetDetails
	 */
	@PostMapping("/assetDetail")
	public AssetDetails saveAssetDetail(@Validated @RequestBody AssetDetails assetDetails) {
		assetDetails.assetId = UUID.randomUUID().toString();
		return mongoTemplate.insert(assetDetails);
	}
	
	/**
	 * @return List<AssetDetails>
	 */
	@GetMapping("/assetDetails")
	public List<AssetDetails> fetchAssetDetails() {
		return mongoTemplate.findAll(AssetDetails.class);
	}

	/**
	 * @param assetId assetId
	 * @return Map<String, Object>
	 */
	@DeleteMapping("/{assetId}")
	public Map<String, Object> deleteAsset(@PathVariable("assetId") String assetId) {
		Map<String, Object> body = new LinkedHashMap<>();
		AssetDetails assetDetail =mongoTemplate.findById(assetId,AssetDetails.class);
		if ( assetDetail!= null) {
			mongoTemplate.remove(assetDetail);
			body.put("message", "asset Id " + assetDetail.assetId + " deleted sucessfully");
		} else {
			body.put("message", assetId + " not found");
		}
		return body;
	}
}

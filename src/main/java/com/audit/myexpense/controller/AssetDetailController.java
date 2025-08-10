/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.controller;

import java.util.*;

import com.audit.myexpense.util.ExpenseCommonUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.audit.myexpense.model.AssetDetails;

/**
 * @author Manikandan Narasimhan
 *
 */
@CrossOrigin(origins = "http://localhost:4200")
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
		assetDetails.id = UUID.randomUUID().toString();
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
	 * @param id assetId
	 * @return Map<String, Object>
	 */
	@DeleteMapping("/{id}")
	public Map<String, Object> deleteAsset(@PathVariable("id") String id) {
		Map<String, Object> body = new LinkedHashMap<>();
		AssetDetails assetDetail =mongoTemplate.findById(id,AssetDetails.class);
		if ( assetDetail!= null) {
			mongoTemplate.remove(assetDetail);
			body.put("message", "asset " + assetDetail.name  + " deleted successfully");
		} else {
			body.put("message", id + " not found");
		}
		return body;
	}

	/**
	 * @param  data asset data
	 * @return Map<String, Object>
	 */
	@PatchMapping("/assetDetail")
	public Map<String, Object> updateAssetStatus(@RequestBody AssetDetails data) {
		Map<String, Object> body = new LinkedHashMap<>();
		AssetDetails assetDetails= mongoTemplate.findById(data.id,AssetDetails.class);
		if (assetDetails != null) {
			assetDetails.comments= data.comments;
			assetDetails.name=data.name;
			assetDetails.image=data.image;
			assetDetails.type=data.type;
			assetDetails.status=data.status;
			assetDetails.updatedDate= ExpenseCommonUtil.formattedDate(new Date());
			mongoTemplate.save(assetDetails);
			body.put("message", "asset " + assetDetails.name + " updated successfully");
		} else {
			body.put("message", "Data not found");
		}
		return body;
	}
}

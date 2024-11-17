/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.audit.myexpense.model.AssetDetails;

/**
 * @author Manikandan Narasimhan
 *
 */
@Repository
public interface AssetDetailRepository extends MongoRepository<AssetDetails, Integer>{

}

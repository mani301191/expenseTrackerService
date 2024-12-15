/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */
package com.audit.myexpense.controller;

import com.audit.myexpense.model.ProfileDetail;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;


/**
 * @author Manikandan Narasimhan
 *
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/profile")
public class ProfileDetailController {

    private final MongoTemplate mongoTemplate;

    public ProfileDetailController( MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param profileDetail
     * @return profileDetail
     */
    @PostMapping("/profileDetail")
    public ProfileDetail profileDetail(@Valid @RequestBody ProfileDetail profileDetail) {
        profileDetail.profileId=1;
        return mongoTemplate.save(profileDetail);
    }

    /**
     * @return ProfileDetail
     */
    @GetMapping("/profileDetail")
    public ProfileDetail profileDetail() {
        return mongoTemplate.findOne(new Query(Criteria.where("profileId").is(1)), ProfileDetail.class);
    }
}

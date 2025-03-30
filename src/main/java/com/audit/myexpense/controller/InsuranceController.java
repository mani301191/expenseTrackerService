package com.audit.myexpense.controller;

import com.audit.myexpense.model.InsuranceDetails;
import com.audit.myexpense.model.Investments;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Manikandan Narasimhan
 *
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/insurance")
public class InsuranceController {

    private final MongoTemplate mongoTemplate;

    public InsuranceController( MongoTemplate mongoTemplate) {
        super();
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param insuranceDetails insuranceDetails
     * @return insuranceDetails
     */
    @PostMapping("/insuranceDetail")
    public InsuranceDetails saveInsuranceDetails(@Validated @RequestBody InsuranceDetails insuranceDetails) {
        insuranceDetails.insuranceId = UUID.randomUUID().toString();
        return mongoTemplate.insert(insuranceDetails);
    }

    /**
     * @return List<insuranceDetails>
     */
    @GetMapping("/insuranceDetails")
    public List<InsuranceDetails> fetchInsuranceDetails() {
        return mongoTemplate.findAll(InsuranceDetails.class);
    }

    /**
     * @param insuranceId insuranceId
     * @return Map<String, Object>
     */
    @DeleteMapping("/{insuranceId}")
    public Map<String, Object> deleteInsurance(@PathVariable("insuranceId") String insuranceId) {
        Map<String, Object> body = new LinkedHashMap<>();
        InsuranceDetails insuranceDetails= mongoTemplate.findById(insuranceId,InsuranceDetails.class);
        if (insuranceDetails != null) {
            mongoTemplate.remove(insuranceDetails);
            body.put("message", "insurance " + insuranceDetails.insuranceType +" - "+ insuranceDetails.policyNumber + " deleted sucessfully");
        } else {
            body.put("message", insuranceId + " not found");
        }
        return body;
    }
}

package com.audit.myexpense.controller;

import com.audit.myexpense.model.InsuranceDetails;
import com.audit.myexpense.model.Investments;
import com.audit.myexpense.util.ExpenseCommonUtil;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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
        List<InsuranceDetails> result=  mongoTemplate.findAll(InsuranceDetails.class);
        return result.stream().sorted((c1,c2)->c1.endDate.getTime() < c2.endDate.getTime() ? -1 : 1).collect(Collectors.toList());
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

    /**
     * @param  data insurance data
     * @return Map<String, Object>
     */
    @PatchMapping("/insuranceDetail")
    public Map<String, Object> updateInsurance(@RequestBody InsuranceDetails data) {
        Map<String, Object> body = new LinkedHashMap<>();
        InsuranceDetails insurance= mongoTemplate.findById(data.insuranceId,InsuranceDetails.class);
        if (insurance != null) {
            insurance.additionalDetails=data.additionalDetails;
            insurance.updatedDate= ExpenseCommonUtil.formattedDate(new Date());
            mongoTemplate.save(insurance);
            body.put("message", "insurance  " + insurance.insuranceProvider + " updated successfully");
        } else {
            body.put("message", "Data not found");
        }
        return body;
    }
}

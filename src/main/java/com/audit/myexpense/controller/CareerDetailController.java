package com.audit.myexpense.controller;

import com.audit.myexpense.model.CareerDetails;
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
@RestController
@RequestMapping("/api/career")
public class CareerDetailController {

    private final MongoTemplate mongoTemplate;

    public CareerDetailController( MongoTemplate mongoTemplate) {
        super();
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param careerDetails careerDetails
     * @return careerDetails
     */
    @PostMapping("/careerDetail")
    public CareerDetails saveCareerDetail(@Validated @RequestBody CareerDetails careerDetails) {
        careerDetails.id = UUID.randomUUID().toString();
        return mongoTemplate.insert(careerDetails);
    }

    /**
     * @return List<CareerDetails>
     */
    @GetMapping("/careerDetail")
    public List<CareerDetails> fetchCareerDetail() {
        return mongoTemplate.findAll(CareerDetails.class);
    }

    /**
     * @param id id
     * @return Map<String, Object>
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteAsset(@PathVariable("id") String id) {
        Map<String, Object> body = new LinkedHashMap<>();
        CareerDetails careerDetails =mongoTemplate.findById(id,CareerDetails.class);
        if ( careerDetails!= null) {
            mongoTemplate.remove(careerDetails);
            body.put("message", " Id " + careerDetails.id + " deleted sucessfully");
        } else {
            body.put("message", id + " not found");
        }
        return body;
    }
}

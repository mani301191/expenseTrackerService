package com.audit.myexpense.controller;

import com.audit.myexpense.model.CareerDetails;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Manikandan Narasimhan
 *
 */
@CrossOrigin(origins = "http://localhost:4200")
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
        List<CareerDetails> result = mongoTemplate.findAll(CareerDetails.class);
         result.sort(Comparator.comparing(
                careerDetail ->careerDetail.endDate,
                Comparator.nullsFirst(Comparator.reverseOrder())));
        return  result;
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
            body.put("message",  careerDetails.recordType+" - "+ careerDetails.orgName+ " deleted sucessfully");
        } else {
            body.put("message", id + " not found");
        }
        return body;
    }
}

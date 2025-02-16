/*
 *  Copyright (C) 2024 Manikandan Narasimhan - All Rights Reserved
 *  * You may use, distribute and modify this code.
 *
 */

package com.audit.myexpense.controller;

import com.audit.myexpense.model.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 *  @author Manikandan Narasimhan
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/fitness")
public class FitnessDetailController {

    private final MongoTemplate mongoTemplate;
    private final String DESC ="DESC";
    private final String ASC ="ASC";

    public FitnessDetailController( MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param fitnessDetails fitness detail information
     * @return fitnessDetails
     */
    @PostMapping("/personDetail")
    public FitnessDetails personDetail(@Valid @RequestBody FitnessDetails fitnessDetails) {
        return mongoTemplate.insert(fitnessDetails);
    }

    /**
     * @return list of FitnessDetails
     */
    @GetMapping("/personDetails")
    public List<FitnessDetails> personDetails() {
        List<FitnessDetails> fitnessdetails = mongoTemplate.findAll( FitnessDetails.class);
         fitnessdetails.stream().forEach(data -> {
             final Query query =  new Query(Criteria.where("personName").is(data.personName));
             List<FitnessChartData> weightData= mongoTemplate.find(query,FitnessChartData.class);
             data.trend=dateSorting(weightData,ASC);
             if(!data.trend.isEmpty()) {
                 FitnessChartData latestData = data.trend.get(data.trend.size() - 1);
                 data.currentHeight = latestData.height;
                 data.currentWeight = latestData.weight;
             }
         });
        return fitnessdetails;
    }

    /**
     * @param personName personName Id to be deleted
     * @return response message
     */
    @DeleteMapping("/person/{personName}")
    public Map<String, Object> deleteFitnessDetail(@PathVariable("personName") String personName) {
        Map<String, Object> body = new LinkedHashMap<>();
        FitnessDetails fitnessDetails = mongoTemplate.findById(personName,FitnessDetails.class);
        if(fitnessDetails !=null) {
            mongoTemplate.remove(fitnessDetails);
            body.put("message", "personName - "+personName + " deleted sucessfully" );
        } else {
            body.put("message",  "personName - " + personName + " not found");
        }
        return body;
    }
    private List<FitnessChartData> dateSorting(List<FitnessChartData> trend,String operator) {
        // comprator logic
        Comparator<FitnessChartData> dateComparator = (c1, c2) -> {
            if(operator.equals(DESC)){
                return c1.date.getTime() > c2.date.getTime() ? -1 : 1;
            }else {
                return c1.date.getTime() < c2.date.getTime() ? -1 : 1;
            }
        };
         Collections.sort(trend, dateComparator);
        return trend;
    }

    /**
     * @param medicalDetail  information
     * @return medicalDetail
     */
    @PostMapping("/medicalDetail")
    public MedicalDetails medicalDetail(@Valid @RequestBody MedicalDetails medicalDetail) {
        medicalDetail.id= UUID.randomUUID().toString();
        return mongoTemplate.insert(medicalDetail);
    }


    /**
     * @param patientName name
     * @return list of MedicalDetails
     */
    @GetMapping("/medicalDetails")
    public List<MedicalDetails> medicalDetails(@RequestParam(required = true) String patientName) {
        final Query query =  new Query(Criteria.where("patientName").is(patientName));
        List<MedicalDetails> result =mongoTemplate.find( query, MedicalDetails.class);
        result.sort((d1,d2)-> d2.date.compareTo(d1.date));
        return result;
    }

    /**
     * @param id  Id to be deleted
     * @return response message
     */
    @DeleteMapping("/medicalDetail/{id}")
    public Map<String, Object> deleteMedicalDetail(@PathVariable("id") String id) {
        Map<String, Object> body = new LinkedHashMap<>();
        MedicalDetails medicalDetails = mongoTemplate.findById(id,MedicalDetails.class);
        if(medicalDetails !=null) {
            mongoTemplate.remove(medicalDetails);
            body.put("message", "medicalDetail - "+id + " deleted sucessfully" );
        } else {
            body.put("message",  "medicalDetail - " + id + " not found");
        }
        return body;
    }

    /**
     * @return list of FitnessDetails
     */
    @GetMapping("/personNames")
    public List<Dropdown> personNames() {
        List<Dropdown> result = new ArrayList<>();
        List<FitnessDetails> fitnessdetails = mongoTemplate.findAll( FitnessDetails.class);
        fitnessdetails.stream().forEach(data -> {
            result.add(new Dropdown(data.personName, data.personName));
        });
        return result;
    }

    /**
     * @param fitnessChartData fitness personName information
     * @return fitnessDetails
     */
    @PostMapping("/personDetail/weight")
    public FitnessChartData weightDetail(@Valid @RequestBody  FitnessChartData fitnessChartData) {
        fitnessChartData.id=UUID.randomUUID().toString();
        return mongoTemplate.save(fitnessChartData);
    }
    /**
     * @param personName name
     * @return list of FitnessChartData
     */
    @GetMapping("/personDetail/weight")
    public List<FitnessChartData> personName(@RequestParam(required = true) String personName) {
        final Query query =  new Query(Criteria.where("personName").is(personName));
        return dateSorting(mongoTemplate.find( query, FitnessChartData.class),DESC);
    }
    /**
     * @param id  Id to be deleted
     * @return response message
     */
    @DeleteMapping("/personDetail/weight/{id}")
    public Map<String, Object> deletePersonDetail(@PathVariable("id") String id) {
        Map<String, Object> body = new LinkedHashMap<>();
        FitnessChartData fitnessChartData = mongoTemplate.findById(id,FitnessChartData.class);
        if(fitnessChartData !=null) {
            mongoTemplate.remove(fitnessChartData);
            body.put("message", "person weight - "+id + " deleted sucessfully" );
        } else {
            body.put("message",  "person weight - " + id + " not found");
        }
        return body;
    }
}

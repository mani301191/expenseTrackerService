package com.audit.myexpense.controller;

import com.audit.myexpense.model.Appliances;
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
@RequestMapping("/api/appliances")
public class AppliancesController {

    private final MongoTemplate mongoTemplate;

    public AppliancesController( MongoTemplate mongoTemplate) {
        super();
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param appliances appliances
     * @return appliances
     */
    @PostMapping("/appliancesDetail")
    public Appliances saveAppliancesDetail(@Validated @RequestBody Appliances appliances) {
        appliances.appliancesId = UUID.randomUUID().toString();
        return mongoTemplate.insert(appliances);
    }

    /**
     * @return List<AssetDetails>
     */
    @GetMapping("/appliancesDetail")
    public List<Appliances> fetchAppliancesDetails() {
        return mongoTemplate.findAll(Appliances.class);
    }

    /**
     * @param appliancesId appliancesId
     * @return Map<String, Object>
     */
    @DeleteMapping("/{appliancesId}")
    public Map<String, Object> deleteAppliances(@PathVariable("appliancesId") String appliancesId) {
        Map<String, Object> body = new LinkedHashMap<>();
        Appliances appliances =mongoTemplate.findById(appliancesId,Appliances.class);
        if ( appliances!= null) {
            mongoTemplate.remove(appliances);
            body.put("message", "asset Id " + appliances.appliancesId + " deleted sucessfully");
        } else {
            body.put("message", appliancesId + " not found");
        }
        return body;
    }
}

package com.audit.myexpense.controller;

import com.audit.myexpense.model.Appliances;
import com.audit.myexpense.model.AssetDetails;
import com.audit.myexpense.util.ExpenseCommonUtil;
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
            body.put("message", "Appliances " + appliances.applianceName + " deleted sucessfully");
        } else {
            body.put("message", appliancesId + " not found");
        }
        return body;
    }

    /**
     * @param  data appliances data
     * @return Map<String, Object>
     */
    @PatchMapping("/appliancesDetail")
    public Map<String, Object> updateAppliancesDetail(@RequestBody Appliances data) {
        Map<String, Object> body = new LinkedHashMap<>();
        Appliances appliances= mongoTemplate.findById(data.appliancesId,Appliances.class);
        if (appliances != null) {
            appliances.applianceName=data.applianceName;
            appliances.additionalDetails=data.additionalDetails;
            appliances.amc=data.amc;
            appliances.amcEndDate=data.amcEndDate;
            appliances.lastServicedDate=data.lastServicedDate;
            appliances.updatedDate= ExpenseCommonUtil.formattedDate(new Date());
            mongoTemplate.save(appliances);
            body.put("message", "appliances " + appliances.applianceName + " updated successfully");
        } else {
            body.put("message", "Data not found");
        }
        return body;
    }
}

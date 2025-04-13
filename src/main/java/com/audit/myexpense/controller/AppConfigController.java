package com.audit.myexpense.controller;

import com.audit.myexpense.model.AppConfig;
import com.audit.myexpense.model.Dropdown;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 * @author Manikandan Narasimhan
 *
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/config")
public class AppConfigController {

    private final MongoTemplate mongoTemplate;

    public AppConfigController( MongoTemplate mongoTemplate) {
        super();
        this.mongoTemplate = mongoTemplate;
    }
    /**
     * @param appConfigs data
     * @return appConfigs
     */
    @PostMapping("/appConfig")
    public Collection<AppConfig> appConfig(@Valid @RequestBody Collection<AppConfig> appConfigs) {

        for( AppConfig appConfig :appConfigs) {
            mongoTemplate.save(appConfig);
        }
        return appConfigs;
    }

    /**
     * @return List<appConfig>
     */
    @GetMapping("/appConfig")
    public List<AppConfig> fetchAppConfigDetails() {
        return mongoTemplate.findAll(AppConfig.class);
    }

    /**
     * @return List<Dropdo>
     */
    @GetMapping("/dropDown")
    public List<Dropdown> fetchAppDropDowns( @RequestParam String key) {
        List<Dropdown> result = new ArrayList<>();
        final Query query =  new Query(Criteria.where("key").is(key));
        AppConfig data = mongoTemplate.findOne(query, AppConfig.class);
        if (data !=null) {
            String[] values = data.value.split(",");
            for (String val : values) {
                result.add(new Dropdown(val, val));
            }
        }
        return result;
    }


    /**
     * @param key key
     * @return Map<String, Object>
     */
    @DeleteMapping("/{key}")
    public Map<String, Object> deleteAppConfig(@PathVariable("key") String key) {
        Map<String, Object> body = new LinkedHashMap<>();
        AppConfig appConfig =mongoTemplate.findById(key,AppConfig.class);
        if ( appConfig!= null) {
            mongoTemplate.remove(appConfig);
            body.put("message", "key " + appConfig.key + " deleted sucessfully");
        } else {
            body.put("message", appConfig + " not found");
        }
        return body;
    }

    /**
     * @return appConfigs
     */
    @PostMapping("/default")
    public Collection<AppConfig> defaultConfig() {
        List<AppConfig> appConfigs = new ArrayList<>();
        appConfigs.add(new AppConfig("AssetTypes","Movable,Non-Movable"));
        appConfigs.add(new AppConfig("Assets","Gold,Silver,Property"));
        appConfigs.add(new AppConfig("AssetStatus","In-Loan/EMI,In-Locker,In-use,Loan/EMI Closed"));
        appConfigs.add(new AppConfig("Investment","PF,NPS,SSA,MutaulFund,Equity,BankAccount,FixedDeposits"));
        appConfigs.add(new AppConfig("InvestmentStatus","Active,InActive,Closed"));
        appConfigs.add(new AppConfig("InsurenceType","Health,Term,Property,Veichle"));
        appConfigs.add(new AppConfig("EventType","Birthday,Anniversary,Festival,PaymentDue,Meetings,Travel,Adhoc"));

         mongoTemplate.insertAll(appConfigs);

        return appConfigs;
    }
}

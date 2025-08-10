package com.audit.myexpense.controller;

import com.audit.myexpense.model.EventDetails;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Manikandan Narasimhan
 *
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/event")
public class EventDetailController {
    private final MongoTemplate mongoTemplate;

    public EventDetailController( MongoTemplate mongoTemplate) {
        super();
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param eventDetails eventDetails
     * @return eventDetails
     */
    @PostMapping("/eventDetail")
    public EventDetails saveEventDetail(@Validated @RequestBody EventDetails eventDetails) {
        eventDetails.eventId = UUID.randomUUID().toString();
        return mongoTemplate.insert(eventDetails);
    }

    /**
     * @return List<CareerDetails>
     */
    @GetMapping("/eventDetail")
    public List<EventDetails> fetchEventDetails() {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.ASC, "eventDate"));
        return mongoTemplate.find(query, EventDetails.class);
    }

    /**
     * @param eventId eventId
     * @return Map<String, Object>
     */
    @DeleteMapping("/{eventId}")
    public Map<String, Object> deleteEvent(@PathVariable("eventId") String eventId) {
        Map<String, Object> body = new LinkedHashMap<>();
        EventDetails eventDetails =mongoTemplate.findById(eventId,EventDetails.class);
        if ( eventDetails!= null) {
            mongoTemplate.remove(eventDetails);
            body.put("message", eventDetails.eventDetail+" Record deleted successfully");
        } else {
            body.put("message", eventId + " not found");
        }
        return body;
    }

}

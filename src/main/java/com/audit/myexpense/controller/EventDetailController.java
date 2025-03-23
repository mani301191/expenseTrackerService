package com.audit.myexpense.controller;

import com.audit.myexpense.model.EventDetails;
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
        return mongoTemplate.findAll(EventDetails.class);
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
            body.put("message", "event Id " + eventDetails.eventId + " deleted sucessfully");
        } else {
            body.put("message", eventId + " not found");
        }
        return body;
    }

}

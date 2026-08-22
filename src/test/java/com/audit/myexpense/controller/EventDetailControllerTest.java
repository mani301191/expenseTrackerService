package com.audit.myexpense.controller;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.audit.myexpense.model.EventDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EventDetailController}.
 */
@ExtendWith(MockitoExtension.class)
class EventDetailControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private EventDetailController controller;

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    private static EventDetails event(String eventId, String detail) {
        EventDetails eventDetails = new EventDetails();
        eventDetails.eventId = eventId;
        eventDetails.eventDetail = detail;
        eventDetails.eventType = "Birthday";
        eventDetails.eventDate = date(2024, 5, 10);
        return eventDetails;
    }

    @Test
    void saveAssignsGeneratedIdAndInserts() {
        when(mongoTemplate.insert(any(EventDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventDetails request = event(null, "John's birthday");
        EventDetails result = controller.saveEventDetail(request);

        assertThat(result.eventId).isNotBlank();
        verify(mongoTemplate).insert(request);
    }

    @Test
    void fetchReturnsAllEvents() {
        EventDetails stored = event("evt-1", "Anniversary");
        when(mongoTemplate.find(any(Query.class), eq(EventDetails.class)))
                .thenReturn(Arrays.asList(stored));

        assertThat(controller.fetchEventDetails()).containsExactly(stored);
    }

    @Test
    void deleteRemovesExistingEvent() {
        EventDetails stored = event("evt-1", "Wedding anniversary");
        when(mongoTemplate.findById("evt-1", EventDetails.class)).thenReturn(stored);

        Map<String, Object> body = controller.deleteEvent("evt-1");

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Wedding anniversary").contains("deleted successfully");
    }

    @Test
    void deleteOfUnknownEventReportsNotFound() {
        when(mongoTemplate.findById("missing", EventDetails.class)).thenReturn(null);

        Map<String, Object> body = controller.deleteEvent("missing");

        verify(mongoTemplate, never()).remove(any(EventDetails.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }
}

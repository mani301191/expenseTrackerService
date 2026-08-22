package com.audit.myexpense.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.audit.myexpense.model.CareerDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CareerDetailController}.
 */
@ExtendWith(MockitoExtension.class)
class CareerDetailControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private CareerDetailController controller;

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    private static CareerDetails career(String id, String recordType, String orgName, String designation,
                                        Date endDate) {
        CareerDetails details = new CareerDetails();
        details.id = id;
        details.recordType = recordType;
        details.orgName = orgName;
        details.designation = designation;
        details.startDate = date(2015, 1, 1);
        details.endDate = endDate;
        return details;
    }

    @Test
    void saveAssignsGeneratedIdAndInserts() {
        when(mongoTemplate.insert(any(CareerDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareerDetails request = career(null, "Experience", "ABC Corp", "Developer", date(2020, 6, 30));
        CareerDetails result = controller.saveCareerDetail(request);

        assertThat(result.id).isNotBlank();
        verify(mongoTemplate).insert(request);
    }

    @Test
    void fetchSortsCurrentRecordsFirstThenByMostRecentEndDate() {
        CareerDetails currentJob = career("c-1", "Experience", "XYZ Corp", "Architect", null);
        CareerDetails olderJob = career("c-2", "Experience", "ABC Corp", "Developer", date(2020, 6, 30));
        CareerDetails recentJob = career("c-3", "Experience", "LMN Corp", "Lead", date(2022, 12, 31));
        when(mongoTemplate.findAll(CareerDetails.class))
                .thenReturn(new ArrayList<>(Arrays.asList(olderJob, currentJob, recentJob)));

        java.util.List<CareerDetails> result = controller.fetchCareerDetail();

        assertThat(result).extracting(details -> details.id)
                .containsExactly("c-1", "c-3", "c-2");
    }

    @Test
    void deleteRemovesExistingCareerRecord() {
        CareerDetails stored = career("c-1", "Education", "Anna University", "B.E.", date(2009, 5, 20));
        when(mongoTemplate.findById("c-1", CareerDetails.class)).thenReturn(stored);

        Map<String, Object> body = controller.deleteCareerDetail("c-1");

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Education").contains("Anna University").contains("deleted");
    }

    @Test
    void deleteOfUnknownCareerRecordReportsNotFound() {
        when(mongoTemplate.findById("missing", CareerDetails.class)).thenReturn(null);

        Map<String, Object> body = controller.deleteCareerDetail("missing");

        verify(mongoTemplate, never()).remove(any(CareerDetails.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }

    @Test
    void updateCopiesEndDateAndComments() {
        CareerDetails stored = career("c-1", "Experience", "ABC Corp", "Developer", date(2020, 6, 30));
        stored.comments = "old";
        when(mongoTemplate.findById("c-1", CareerDetails.class)).thenReturn(stored);
        when(mongoTemplate.save(any(CareerDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareerDetails change = new CareerDetails();
        change.id = "c-1";
        change.endDate = date(2021, 7, 15);
        change.comments = "relieved after notice period";

        Map<String, Object> body = controller.updateCareerDetail(change);

        assertThat(stored.endDate).isEqualTo(date(2021, 7, 15));
        assertThat(stored.comments).isEqualTo("relieved after notice period");
        assertThat(stored.updatedDate).isNotBlank();
        verify(mongoTemplate).save(stored);

        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("updated successfully");
    }

    @Test
    void updateOfUnknownCareerRecordReportsDataNotFound() {
        when(mongoTemplate.findById("ghost", CareerDetails.class)).thenReturn(null);

        CareerDetails change = new CareerDetails();
        change.id = "ghost";
        Map<String, Object> body = controller.updateCareerDetail(change);

        verify(mongoTemplate, never()).save(any(CareerDetails.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Data not found");
    }
}

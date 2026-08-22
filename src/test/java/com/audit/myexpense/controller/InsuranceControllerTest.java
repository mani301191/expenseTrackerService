package com.audit.myexpense.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.audit.myexpense.model.InsuranceDetails;
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
 * Unit tests for {@link InsuranceController}.
 */
@ExtendWith(MockitoExtension.class)
class InsuranceControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private InsuranceController controller;

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    private static InsuranceDetails insurance(String id, String type, String provider, String policyNumber,
                                              Date endDate) {
        InsuranceDetails details = new InsuranceDetails();
        details.insuranceId = id;
        details.insuranceType = type;
        details.insuranceProvider = provider;
        details.policyNumber = policyNumber;
        details.nominee = "Spouse";
        details.startDate = date(2020, 1, 1);
        details.endDate = endDate;
        return details;
    }

    @Test
    void saveAssignsGeneratedIdAndInserts() {
        when(mongoTemplate.insert(any(InsuranceDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InsuranceDetails request = insurance(null, "Health", "HDFC", "POL-123", date(2030, 5, 10));
        InsuranceDetails result = controller.saveInsuranceDetails(request);

        assertThat(result.insuranceId).isNotBlank();
        verify(mongoTemplate).insert(request);
    }

    @Test
    void fetchReturnsInsurancesSortedByEndDate() {
        InsuranceDetails late = insurance("ins-2", "Term", "LIC", "POL-2", date(2031, 1, 1));
        InsuranceDetails early = insurance("ins-1", "Health", "HDFC", "POL-1", date(2030, 1, 1));
        when(mongoTemplate.findAll(InsuranceDetails.class)).thenReturn(new ArrayList<>(Arrays.asList(late, early)));

        List<InsuranceDetails> result = controller.fetchInsuranceDetails();

        assertThat(result).extracting(details -> details.insuranceId)
                .containsExactly("ins-1", "ins-2");
    }

    @Test
    void deleteRemovesExistingInsurance() {
        InsuranceDetails stored = insurance("ins-1", "Health", "HDFC", "POL-123", date(2030, 1, 1));
        when(mongoTemplate.findById("ins-1", InsuranceDetails.class)).thenReturn(stored);

        Map<String, Object> body = controller.deleteInsurance("ins-1");

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Health").contains("POL-123").contains("deleted");
    }

    @Test
    void deleteOfUnknownInsuranceReportsNotFound() {
        when(mongoTemplate.findById("missing", InsuranceDetails.class)).thenReturn(null);

        Map<String, Object> body = controller.deleteInsurance("missing");

        verify(mongoTemplate, never()).remove(any(InsuranceDetails.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }

    @Test
    void updateCopiesAdditionalDetails() {
        InsuranceDetails stored = insurance("ins-1", "Health", "HDFC", "POL-123", date(2030, 1, 1));
        when(mongoTemplate.findById("ins-1", InsuranceDetails.class)).thenReturn(stored);
        when(mongoTemplate.save(any(InsuranceDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InsuranceDetails change = insurance("ins-1", "ignored", "ignored", "ignored", null);
        change.additionalDetails = "room rent capped at 1%";
        Map<String, Object> body = controller.updateInsurance(change);

        assertThat(stored.additionalDetails).isEqualTo("room rent capped at 1%");
        assertThat(stored.updatedDate).isNotBlank();
        verify(mongoTemplate).save(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("HDFC").contains("updated successfully");
    }

    @Test
    void updateOfUnknownInsuranceReportsDataNotFound() {
        when(mongoTemplate.findById("ghost", InsuranceDetails.class)).thenReturn(null);

        InsuranceDetails change = new InsuranceDetails();
        change.insuranceId = "ghost";
        Map<String, Object> body = controller.updateInsurance(change);

        verify(mongoTemplate, never()).save(any(InsuranceDetails.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Data not found");
    }
}

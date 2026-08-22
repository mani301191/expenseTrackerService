package com.audit.myexpense.controller;

import java.util.Arrays;
import java.util.Map;

import com.audit.myexpense.model.Investments;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
 * Unit tests for {@link InvestmentController}.
 */
@ExtendWith(MockitoExtension.class)
class InvestmentControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private InvestmentController controller;

    private static Investments investment(String investId, String detail, String status) {
        Investments investments = new Investments();
        investments.investId = investId;
        investments.investmentDetail = detail;
        investments.status = status;
        investments.investment = "PF";
        investments.nominee = "Spouse";
        return investments;
    }

    @Test
    void saveAssignsGeneratedIdAndInserts() {
        when(mongoTemplate.insert(any(Investments.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Investments request = investment(null, "NPS Tier-1", "Active");
        Investments result = controller.saveInvestDetail(request);

        assertThat(result.investId).isNotBlank();
        verify(mongoTemplate).insert(request);
    }

    @Test
    void fetchReturnsAllInvestments() {
        Investments stored = investment("inv-1", "NPS", "Active");
        when(mongoTemplate.findAll(Investments.class)).thenReturn(Arrays.asList(stored));

        assertThat(controller.fetchInvestDetails()).containsExactly(stored);
    }

    @Test
    void deleteRemovesExistingInvestment() {
        Investments stored = investment("inv-1", "NPS Tier-1", "Active");
        when(mongoTemplate.findById("inv-1", Investments.class)).thenReturn(stored);

        Map<String, Object> body = controller.deleteInvestment("inv-1");

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("NPS Tier-1").contains("deleted successfully");
    }

    @Test
    void deleteOfUnknownInvestmentReportsNotFound() {
        when(mongoTemplate.findById("missing", Investments.class)).thenReturn(null);

        Map<String, Object> body = controller.deleteInvestment("missing");

        verify(mongoTemplate, never()).remove(any(Investments.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }

    @Test
    void updateCopiesStatusAndAdditionalDetails() {
        Investments stored = investment("inv-1", "NPS Tier-1", "Active");
        stored.additionalDetails = "old";
        when(mongoTemplate.findById("inv-1", Investments.class)).thenReturn(stored);
        when(mongoTemplate.save(any(Investments.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Investments change = investment("inv-1", "ignored", "Closed");
        change.additionalDetails = "closed after maturity";
        Map<String, Object> body = controller.updateInvestment(change);

        ArgumentCaptor<Investments> captor = ArgumentCaptor.forClass(Investments.class);
        verify(mongoTemplate).save(captor.capture());
        Investments saved = captor.getValue();
        assertThat(saved.status).isEqualTo("Closed");
        assertThat(saved.additionalDetails).isEqualTo("closed after maturity");
        assertThat(saved.updatedDate).isNotBlank();

        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("updated successfully");
    }

    @Test
    void updateOfUnknownInvestmentReportsDataNotFound() {
        when(mongoTemplate.findById("ghost", Investments.class)).thenReturn(null);

        Map<String, Object> body = controller.updateInvestment(investment("ghost", "x", "Active"));

        verify(mongoTemplate, never()).save(any(Investments.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Data not found");
    }
}

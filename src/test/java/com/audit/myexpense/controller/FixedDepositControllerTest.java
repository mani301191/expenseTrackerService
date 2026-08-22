package com.audit.myexpense.controller;

import java.util.Arrays;
import java.util.Map;

import com.audit.myexpense.model.FixedDeposit;
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
 * Unit tests for {@link FixedDepositController}.
 */
@ExtendWith(MockitoExtension.class)
class FixedDepositControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private FixedDepositController controller;

    private static FixedDeposit fixedDeposit(String id, String bankName) {
        FixedDeposit fd = new FixedDeposit();
        fd.id = id;
        fd.bankName = bankName;
        fd.accountNumber = "ACC-001";
        fd.openedDate = "01/01/2024";
        fd.maturityDate = "01/01/2027";
        fd.interestRate = 7.1;
        fd.nomineeName = "Spouse";
        fd.depositAmount = 100000.0;
        fd.expectedMaturityAmount = 123000.0;
        return fd;
    }

    @Test
    void createAssignsIdAndUpdatedDateThenInserts() {
        when(mongoTemplate.insert(any(FixedDeposit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FixedDeposit request = fixedDeposit(null, "HDFC");
        FixedDeposit result = controller.createFixedDeposit(request);

        assertThat(result.id).isNotBlank();
        assertThat(result.updatedDate).isNotBlank();
        verify(mongoTemplate).insert(request);
    }

    @Test
    void fetchReturnsAllFixedDeposits() {
        FixedDeposit stored = fixedDeposit("fd-1", "HDFC");
        when(mongoTemplate.findAll(FixedDeposit.class)).thenReturn(Arrays.asList(stored));

        assertThat(controller.getAllFixedDeposits()).containsExactly(stored);
    }

    @Test
    void deleteRemovesExistingFixedDeposit() {
        FixedDeposit stored = fixedDeposit("fd-1", "ICICI");
        when(mongoTemplate.findById("fd-1", FixedDeposit.class)).thenReturn(stored);

        Map<String, Object> body = controller.deleteFixedDeposit("fd-1");

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("ICICI").contains("deleted successfully");
    }

    @Test
    void deleteOfUnknownFixedDepositReportsNotFound() {
        when(mongoTemplate.findById("missing", FixedDeposit.class)).thenReturn(null);

        Map<String, Object> body = controller.deleteFixedDeposit("missing");

        verify(mongoTemplate, never()).remove(any(FixedDeposit.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }

    @Test
    void updateCopiesIncomingFieldsOntoStoredFixedDeposit() {
        FixedDeposit stored = fixedDeposit("fd-1", "Old Bank");
        when(mongoTemplate.findById("fd-1", FixedDeposit.class)).thenReturn(stored);
        when(mongoTemplate.save(any(FixedDeposit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> body = controller.updateFixedDeposit(fixedDeposit("fd-1", "New Bank"));

        ArgumentCaptor<FixedDeposit> captor = ArgumentCaptor.forClass(FixedDeposit.class);
        verify(mongoTemplate).save(captor.capture());
        FixedDeposit saved = captor.getValue();

        assertThat(saved.bankName).isEqualTo("New Bank");
        assertThat(saved.accountNumber).isEqualTo("ACC-001");
        assertThat(saved.interestRate).isEqualTo(7.1);
        assertThat(saved.depositAmount).isEqualTo(100000.0);
        assertThat(saved.expectedMaturityAmount).isEqualTo(123000.0);
        assertThat(saved.nomineeName).isEqualTo("Spouse");
        assertThat(saved.updatedDate).isNotBlank();

        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("New Bank").contains("updated successfully");
    }

    @Test
    void updateOfUnknownFixedDepositReportsDataNotFound() {
        when(mongoTemplate.findById("ghost", FixedDeposit.class)).thenReturn(null);

        Map<String, Object> body = controller.updateFixedDeposit(fixedDeposit("ghost", "Ghost Bank"));

        verify(mongoTemplate, never()).save(any(FixedDeposit.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Data not found");
    }
}

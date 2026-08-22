package com.audit.myexpense.controller;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.audit.myexpense.model.Appliances;
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
 * Unit tests for {@link AppliancesController}.
 */
@ExtendWith(MockitoExtension.class)
class AppliancesControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private AppliancesController controller;

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    private static Appliances appliance(String id, String name, String amc) {
        Appliances appliances = new Appliances();
        appliances.appliancesId = id;
        appliances.applianceName = name;
        appliances.amc = amc;
        return appliances;
    }

    @Test
    void saveAssignsGeneratedIdAndInserts() {
        when(mongoTemplate.insert(any(Appliances.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appliances request = appliance(null, "Washing Machine", "Yes");
        Appliances result = controller.saveAppliancesDetail(request);

        assertThat(result.appliancesId).isNotBlank();
        verify(mongoTemplate).insert(request);
    }

    @Test
    void fetchReturnsAllAppliances() {
        Appliances stored = appliance("ap-1", "Refrigerator", "No");
        when(mongoTemplate.findAll(Appliances.class)).thenReturn(Arrays.asList(stored));

        assertThat(controller.fetchAppliancesDetails()).containsExactly(stored);
    }

    @Test
    void deleteRemovesExistingAppliance() {
        Appliances stored = appliance("ap-1", "Air Conditioner", "Yes");
        when(mongoTemplate.findById("ap-1", Appliances.class)).thenReturn(stored);

        Map<String, Object> body = controller.deleteAppliances("ap-1");

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Air Conditioner").contains("deleted");
    }

    @Test
    void deleteOfUnknownApplianceReportsNotFound() {
        when(mongoTemplate.findById("missing", Appliances.class)).thenReturn(null);

        Map<String, Object> body = controller.deleteAppliances("missing");

        verify(mongoTemplate, never()).remove(any(Appliances.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }

    @Test
    void updateCopiesIncomingFieldsOntoStoredAppliance() {
        Appliances stored = appliance("ap-1", "Old Name", "No");
        when(mongoTemplate.findById("ap-1", Appliances.class)).thenReturn(stored);
        when(mongoTemplate.save(any(Appliances.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appliances change = appliance("ap-1", "Washing Machine", "Yes");
        change.additionalDetails = "front load";
        change.amcEndDate = date(2027, 3, 31);
        change.lastServicedDate = date(2026, 3, 31);

        Map<String, Object> body = controller.updateAppliancesDetail(change);

        assertThat(stored.applianceName).isEqualTo("Washing Machine");
        assertThat(stored.amc).isEqualTo("Yes");
        assertThat(stored.additionalDetails).isEqualTo("front load");
        assertThat(stored.amcEndDate).isEqualTo(date(2027, 3, 31));
        assertThat(stored.lastServicedDate).isEqualTo(date(2026, 3, 31));
        assertThat(stored.updatedDate).isNotBlank();
        verify(mongoTemplate).save(stored);

        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("updated successfully");
    }

    @Test
    void updateOfUnknownApplianceReportsDataNotFound() {
        when(mongoTemplate.findById("ghost", Appliances.class)).thenReturn(null);

        Map<String, Object> body = controller.updateAppliancesDetail(appliance("ghost", "Ghost", "No"));

        verify(mongoTemplate, never()).save(any(Appliances.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Data not found");
    }
}

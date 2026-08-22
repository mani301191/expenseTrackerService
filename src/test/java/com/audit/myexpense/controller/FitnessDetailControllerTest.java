package com.audit.myexpense.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.audit.myexpense.model.Dropdown;
import com.audit.myexpense.model.FitnessChartData;
import com.audit.myexpense.model.FitnessDetails;
import com.audit.myexpense.model.MedicalDetails;
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
 * Unit tests for {@link FitnessDetailController}.
 */
@ExtendWith(MockitoExtension.class)
class FitnessDetailControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private FitnessDetailController controller;

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day, 12, 0, 0);
        return calendar.getTime();
    }

    private static FitnessDetails person(String personName) {
        FitnessDetails details = new FitnessDetails();
        details.personName = personName;
        return details;
    }

    private static FitnessChartData weightEntry(String personName, Date date, double height, double weight) {
        FitnessChartData data = new FitnessChartData();
        data.personName = personName;
        data.date = date;
        data.height = height;
        data.weight = weight;
        return data;
    }

    private static MedicalDetails medical(String id, String patientName, Date date) {
        MedicalDetails details = new MedicalDetails();
        details.id = id;
        details.patientName = patientName;
        details.date = date;
        return details;
    }

    @Test
    void personDetailInsertsAsIs() {
        when(mongoTemplate.insert(any(FitnessDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FitnessDetails request = person("Alex");
        assertThat(controller.personDetail(request)).isSameAs(request);
        verify(mongoTemplate).insert(request);
    }

    @Test
    void personDetailsEnrichesEachPersonWithSortedTrendAndCurrentMeasures() {
        List<FitnessDetails> persons = new ArrayList<>(Arrays.asList(person("Alex"), person("Brad")));
        when(mongoTemplate.findAll(FitnessDetails.class)).thenReturn(persons);

        Map<String, List<FitnessChartData>> trendByPerson = new HashMap<>();
        trendByPerson.put("Alex", new ArrayList<>(Arrays.asList(
                weightEntry("Alex", date(2024, 1, 10), 170.0, 80.0),
                weightEntry("Alex", date(2024, 2, 10), 171.0, 75.0))));
        trendByPerson.put("Brad", new ArrayList<>(Collections.singletonList(
                weightEntry("Brad", date(2024, 3, 5), 180.0, 95.0))));
        when(mongoTemplate.find(any(Query.class), eq(FitnessChartData.class)))
                .thenAnswer(invocation -> {
                    Query query = invocation.getArgument(0);
                    String name = (String) query.getQueryObject().get("personName");
                    return new ArrayList<>(trendByPerson.getOrDefault(name, Collections.emptyList()));
                });

        List<FitnessDetails> result = controller.personDetails();

        assertThat(result).hasSize(2);

        FitnessDetails alex = result.get(0);
        assertThat(alex.personName).isEqualTo("Alex");
        assertThat(alex.trend).extracting(data -> data.weight).containsExactly(80.0, 75.0);
        assertThat(alex.currentHeight).isEqualTo(171.0);
        assertThat(alex.currentWeight).isEqualTo(75.0);

        FitnessDetails brad = result.get(1);
        assertThat(brad.trend).extracting(data -> data.weight).containsExactly(95.0);
        assertThat(brad.currentHeight).isEqualTo(180.0);
        assertThat(brad.currentWeight).isEqualTo(95.0);
    }

    @Test
    void deleteRemovesExistingFitnessPerson() {
        FitnessDetails stored = person("Alex");
        when(mongoTemplate.findById("Alex", FitnessDetails.class)).thenReturn(stored);

        Map<String, Object> body = controller.deleteFitnessDetail("Alex");

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Alex").contains("deleted");
    }

    @Test
    void deleteOfUnknownFitnessPersonReportsNotFound() {
        when(mongoTemplate.findById("Ghost", FitnessDetails.class)).thenReturn(null);

        Map<String, Object> body = controller.deleteFitnessDetail("Ghost");

        verify(mongoTemplate, never()).remove(any(FitnessDetails.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }

    @Test
    void medicalDetailAssignsGeneratedIdBeforeInsert() {
        when(mongoTemplate.insert(any(MedicalDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MedicalDetails request = medical("static-id", "Alex", date(2024, 2, 20));
        MedicalDetails result = controller.medicalDetail(request);

        assertThat(result.id).isNotEqualTo("static-id").isNotBlank();
        verify(mongoTemplate).insert(request);
    }

    @Test
    void medicalDetailsAreSortedByDateDescending() {
        when(mongoTemplate.find(any(Query.class), eq(MedicalDetails.class)))
                .thenReturn(new ArrayList<>(Arrays.asList(
                        medical("m-1", "Alex", date(2024, 1, 5)),
                        medical("m-2", "Alex", date(2024, 3, 15)))));

        List<MedicalDetails> result = controller.medicalDetails("Alex");

        assertThat(result).extracting(details -> details.id)
                .containsExactly("m-2", "m-1");
    }

    @Test
    void deleteRemovesExistingMedicalDetail() {
        MedicalDetails stored = medical("m-1", "Alex", date(2024, 1, 5));
        when(mongoTemplate.findById("m-1", MedicalDetails.class)).thenReturn(stored);

        Map<String, Object> body = controller.deleteMedicalDetail("m-1");

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Alex").contains("deleted");
    }

    @Test
    void deleteOfUnknownMedicalDetailReportsNotFound() {
        when(mongoTemplate.findById("ghost", MedicalDetails.class)).thenReturn(null);

        Map<String, Object> body = controller.deleteMedicalDetail("ghost");

        verify(mongoTemplate, never()).remove(any(MedicalDetails.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }

    @Test
    void updateCopiesIncomingFieldsOntoStoredMedicalDetail() {
        MedicalDetails stored = medical("m-1", "Alex", date(2024, 1, 5));
        stored.problem = "old problem";
        when(mongoTemplate.findById("m-1", MedicalDetails.class)).thenReturn(stored);
        when(mongoTemplate.save(any(MedicalDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MedicalDetails change = medical("m-1", "ignored", null);
        change.problem = "fever";
        change.diagnosis = "viral";
        change.docterName = "Dr Rao";
        change.hospital = "Apollo";
        change.otherDetails = "rest for 3 days";

        Map<String, Object> body = controller.updateMedicalDetail(change);

        assertThat(stored.problem).isEqualTo("fever");
        assertThat(stored.diagnosis).isEqualTo("viral");
        assertThat(stored.docterName).isEqualTo("Dr Rao");
        assertThat(stored.hospital).isEqualTo("Apollo");
        assertThat(stored.otherDetails).isEqualTo("rest for 3 days");
        assertThat(stored.updatedDate).isNotBlank();
        verify(mongoTemplate).save(stored);

        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("updated successfully");
    }

    @Test
    void updateOfUnknownMedicalDetailReportsDataNotFound() {
        when(mongoTemplate.findById("ghost", MedicalDetails.class)).thenReturn(null);

        Map<String, Object> body = controller.updateMedicalDetail(medical("ghost", "x", null));

        verify(mongoTemplate, never()).save(any(MedicalDetails.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Data not found");
    }

    @Test
    void personNamesReturnsDropdownForEveryPerson() {
        when(mongoTemplate.findAll(FitnessDetails.class))
                .thenReturn(Arrays.asList(person("Alex"), person("Brad")));

        List<Dropdown> result = controller.personNames();

        assertThat(result).extracting(Dropdown::getId).containsExactly("Alex", "Brad");
    }

    @Test
    void weightDetailAssignsGeneratedIdBeforeSave() {
        when(mongoTemplate.save(any(FitnessChartData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FitnessChartData request = weightEntry("Alex", date(2024, 2, 20), 172.0, 76.0);
        request.id = null;
        FitnessChartData result = controller.weightDetail(request);

        assertThat(result.id).isNotBlank();
        verify(mongoTemplate).save(request);
    }

    @Test
    void weightHistoryIsSortedByDateDescending() {
        when(mongoTemplate.find(any(Query.class), eq(FitnessChartData.class)))
                .thenReturn(new ArrayList<>(Arrays.asList(
                        weightEntry("Alex", date(2024, 1, 10), 170.0, 80.0),
                        weightEntry("Alex", date(2024, 4, 10), 171.0, 77.0))));

        List<FitnessChartData> result = controller.personName("Alex");

        assertThat(result).extracting(data -> data.date)
                .containsExactly(date(2024, 4, 10), date(2024, 1, 10));
    }

    @Test
    void deleteRemovesExistingWeightEntry() {
        FitnessChartData stored = weightEntry("Alex", date(2024, 1, 10), 170.0, 80.0);
        when(mongoTemplate.findById("w-1", FitnessChartData.class)).thenReturn(stored);

        Map<String, Object> body = controller.deletePersonDetail("w-1");

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Alex").contains("deleted");
    }

    @Test
    void deleteOfUnknownWeightEntryReportsNotFound() {
        when(mongoTemplate.findById("ghost", FitnessChartData.class)).thenReturn(null);

        Map<String, Object> body = controller.deletePersonDetail("ghost");

        verify(mongoTemplate, never()).remove(any(FitnessChartData.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }

    @Test
    void updateCopiesIncomingWeightAndHeight() {
        FitnessChartData stored = weightEntry("Alex", date(2024, 1, 10), 170.0, 80.0);
        when(mongoTemplate.findById("w-1", FitnessChartData.class)).thenReturn(stored);
        when(mongoTemplate.save(any(FitnessChartData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FitnessChartData change = weightEntry("ignored", null, 172.5, 78.5);
        change.id = "w-1";
        Map<String, Object> body = controller.updateWeightDetail(change);

        assertThat(stored.weight).isEqualTo(78.5);
        assertThat(stored.height).isEqualTo(172.5);
        assertThat(stored.updatedDate).isNotBlank();
        verify(mongoTemplate).save(stored);

        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("updated successfully");
    }

    @Test
    void updateOfUnknownWeightEntryReportsDataNotFound() {
        when(mongoTemplate.findById("ghost", FitnessChartData.class)).thenReturn(null);

        FitnessChartData change = weightEntry("Alex", null, 170.0, 80.0);
        change.id = "ghost";
        Map<String, Object> body = controller.updateWeightDetail(change);

        verify(mongoTemplate, never()).save(any(FitnessChartData.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Data not found");
    }
}

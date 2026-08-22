package com.audit.myexpense.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.audit.myexpense.model.MonthlyTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MonthlyTargetController}.
 */
@ExtendWith(MockitoExtension.class)
class MonthlyTargetControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private MonthlyTargetController controller;

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day, 12, 0, 0);
        return calendar.getTime();
    }

    private static MonthlyTarget target(int year, String month, String description, double amount, Date date) {
        MonthlyTarget target = new MonthlyTarget(year, month, description, amount);
        target.date = date;
        return target;
    }

    @Test
    void saveDerivesMonthAndYearForEachTarget() {
        List<MonthlyTarget> request = new ArrayList<>(Arrays.asList(
                target(0, null, "Groceries", 200.0, date(2024, 1, 15)),
                target(0, null, "Fuel", 100.0, date(2023, 3, 20))));

        Collection<MonthlyTarget> result = controller.monthlyTargetDetail(request);

        assertThat(result).hasSize(2);
        assertThat(request.get(0).month).isEqualTo("January");
        assertThat(request.get(0).year).isEqualTo(2024);
        assertThat(request.get(1).month).isEqualTo("March");
        assertThat(request.get(1).year).isEqualTo(2023);
        verify(mongoTemplate, times(2)).save(any(MonthlyTarget.class));
    }

    @Test
    void searchReturnsTargetsForGivenYearAndMonth() {
        List<MonthlyTarget> stored = Arrays.asList(
                target(2024, "January", "Groceries", 200.0, date(2024, 1, 15)));
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class))).thenReturn(stored);

        List<MonthlyTarget> result = controller.monthlyTarget(2024, "January");

        assertThat(result).containsExactlyElementsOf(stored);
    }

    @Test
    void deleteRemovesExistingTarget() {
        MonthlyTarget stored = target(2024, "January", "Groceries", 200.0, date(2024, 1, 15));
        when(mongoTemplate.findById("target-1", MonthlyTarget.class)).thenReturn(stored);

        Map<String, Object> body = controller.deleteMonthlyTarget("target-1");

        verify(mongoTemplate).remove(stored);
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("Groceries").contains("deleted");
    }

    @Test
    void deleteOfUnknownTargetReportsNotFound() {
        when(mongoTemplate.findById("missing", MonthlyTarget.class)).thenReturn(null);

        Map<String, Object> body = controller.deleteMonthlyTarget("missing");

        verify(mongoTemplate, never()).remove(any(MonthlyTarget.class));
        String message = String.join(" ", body.values().stream().map(String::valueOf).toArray(String[]::new));
        assertThat(message).contains("not found");
    }

    @Test
    void cloneCreatesFreshCopiesOfExistingTargets() {
        Date originalDate = date(2000, 1, 1);
        MonthlyTarget original = new MonthlyTarget(2024, "January", "Groceries", 200.0);
        original.id = "fixed-id";
        original.date = originalDate;
        original.updatedDate = "old";
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class)))
                .thenReturn(new ArrayList<>(Arrays.asList(original)));
        when(mongoTemplate.save(any(MonthlyTarget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Collection<MonthlyTarget> result = controller.monthlyTargetDetailClone(2024, "February");

        ArgumentCaptor<MonthlyTarget> captor = ArgumentCaptor.forClass(MonthlyTarget.class);
        verify(mongoTemplate).save(captor.capture());
        MonthlyTarget saved = captor.getValue();

        assertThat(saved.id).isNotEqualTo("fixed-id").isNotBlank();
        assertThat(saved.date).isAfter(originalDate);
        assertThat(saved.updatedDate).isNotEqualTo("old").isNotBlank();
        assertThat(result).containsExactly(saved);
    }
}

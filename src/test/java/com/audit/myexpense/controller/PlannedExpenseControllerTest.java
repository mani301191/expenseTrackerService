package com.audit.myexpense.controller;

import java.util.Arrays;
import java.util.List;

import com.audit.myexpense.model.Dropdown;
import com.audit.myexpense.model.MonthlyTarget;
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
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PlannedExpenseController}.
 */
@ExtendWith(MockitoExtension.class)
class PlannedExpenseControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private PlannedExpenseController controller;

    private static MonthlyTarget target(String description) {
        MonthlyTarget target = new MonthlyTarget();
        target.description = description;
        target.amount = 100.0;
        return target;
    }

    @Test
    void plannedExpensesAreMappedToDropdownsSortedById() {
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class)))
                .thenReturn(Arrays.asList(target("Zeta"), target("alpha"), target("Mid")));

        List<Dropdown> result = controller.plannedExpense(2024, "January");

        assertThat(result).extracting(Dropdown::getId)
                .containsExactly("Mid", "Zeta", "alpha");
        assertThat(result).allSatisfy(dropdown -> assertThat(dropdown.value).isEqualTo(dropdown.id));
    }

    @Test
    void noPlannedExpensesYieldsEmptyList() {
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class)))
                .thenReturn(Arrays.asList());

        assertThat(controller.plannedExpense(2024, "January")).isEmpty();
    }
}

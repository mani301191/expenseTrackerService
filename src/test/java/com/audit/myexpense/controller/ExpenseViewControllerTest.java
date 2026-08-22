package com.audit.myexpense.controller;

import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link ExpenseViewController}.
 */
class ExpenseViewControllerTest {

    private final ExpenseViewController controller = new ExpenseViewController();

    @Test
    void dashboardRouteResolvesIndexView() {
        Model model = mock(Model.class);

        assertThat(controller.myexpense(model)).isEqualTo("index");
    }
}

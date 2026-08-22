package com.audit.myexpense.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Dropdown} equals/hashCode contract.
 */
class DropdownTest {

    @Test
    void dropdownsWithSameIdAndValueAreEqualWithMatchingHashCodes() {
        Dropdown first = new Dropdown("Gold", "Gold");
        Dropdown second = new Dropdown("Gold", "Gold");

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }

    @Test
    void dropdownsDifferWhenIdOrValueDiffers() {
        Dropdown base = new Dropdown("Gold", "Gold");

        assertThat(base).isNotEqualTo(new Dropdown("Silver", "Gold"));
        assertThat(base).isNotEqualTo(new Dropdown("Gold", "Silver"));
        assertThat(base).isNotEqualTo(null);
        assertThat(base).isNotEqualTo("Gold");
    }

    @Test
    void idIsExposedThroughGetter() {
        assertThat(new Dropdown("Gold", "G").getId()).isEqualTo("Gold");
    }
}

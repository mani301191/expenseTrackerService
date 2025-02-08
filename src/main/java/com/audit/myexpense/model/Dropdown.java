/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */
package com.audit.myexpense.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * @author Manikandan Narasimhan
 *
 */
public class Dropdown {

    @JsonProperty("id")
    public  String id;

    @JsonProperty("value")
    public  String value;

    public Dropdown(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dropdown dropdown = (Dropdown) o;
        return Objects.equals(id, dropdown.id) && Objects.equals(value, dropdown.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }
}

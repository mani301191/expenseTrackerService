package com.audit.myexpense.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

/**
 * @author Manikandan Narasimhan
 *
 */
@Document(collection = "appConfig")
public class AppConfig {
    @Id
    @JsonProperty("key")
    public String key;

    @NotNull
    @JsonProperty("value")
    public String value;

    public AppConfig() {
        super();
    }

    public AppConfig(String key, String value) {
        this.key = key;
        this.value = value;
    }
}

package com.audit.myexpense.model;

import com.audit.myexpense.util.ExpenseCommonUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Manikandan Narasimhan
 *
 */
@Document(collection = "myEventDetail")
public class EventDetails {
    @Id
    @JsonProperty("eventId")
    public String eventId;

    @NotNull
    @JsonFormat(pattern="dd/MM/yyyy")
    @JsonProperty("eventDate")
    public Date eventDate;

    @NotNull
    @JsonProperty("eventType")
    public String eventType;

    @NotNull
    @JsonProperty("eventDetail")
    public String eventDetail;

    @JsonIgnore
    @JsonProperty("updatedDate")
    public String updatedDate = ExpenseCommonUtil.formattedDate(new Date());

    public EventDetails() {
        super();
    }
}

package pl.clickdrop.economy.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DailySkinDropResponse.class, name = "SKIN"),
        @JsonSubTypes.Type(value = DailyCashDropResponse.class, name = "CASH")
})
public sealed interface DailyDropResponse
        permits DailySkinDropResponse, DailyCashDropResponse {
}

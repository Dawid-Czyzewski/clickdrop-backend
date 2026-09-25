package pl.clickdrop.economy.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DailySkinClaimResponse.class, name = "SKIN"),
        @JsonSubTypes.Type(value = DailyCashClaimResponse.class, name = "CASH")
})
public sealed interface DailyClaimResponse
        permits DailySkinClaimResponse, DailyCashClaimResponse {

}

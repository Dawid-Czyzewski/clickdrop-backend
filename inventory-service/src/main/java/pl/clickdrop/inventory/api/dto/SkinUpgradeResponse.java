package pl.clickdrop.inventory.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "result")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SkinUpgradeSuccessResponse.class, name = "SUCCESS"),
        @JsonSubTypes.Type(value = SkinUpgradeFailResponse.class, name = "FAIL")
})
public sealed interface SkinUpgradeResponse permits SkinUpgradeFailResponse, SkinUpgradeSuccessResponse {
}

package net.megogo.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Category(
        @JsonProperty("id")
        Integer id,
        @JsonProperty("external_id")
        Integer externalId,
        @JsonProperty("title")
        String title
) {}

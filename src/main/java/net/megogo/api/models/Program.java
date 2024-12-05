package net.megogo.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Program(
        @JsonProperty("id")
        Integer id,
        @JsonProperty("external_id")
        Integer externalId,
        @JsonProperty("title")
        String title,
        @JsonProperty("category")
        Category category,
        @JsonProperty("pictures")
        Object pictures,
        @JsonProperty("start_timestamp")
        Integer startTimestamp,
        @JsonProperty("end_timestamp")
        Integer endTimestamp,
        @JsonProperty("start")
        String start,
        @JsonProperty("end")
        String end,
        @JsonProperty("virtual_object_id")
        String virtualObjectId,
        @JsonProperty("schedule_type")
        String scheduleType
) {}

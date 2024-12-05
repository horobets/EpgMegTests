package net.megogo.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TimestampData(
        @JsonProperty("utc_offset")
        Integer utcOffset,
        @JsonProperty("timestamp_gmt")
        Long timestampGmt,
        @JsonProperty("timestamp_local")
        Long timestampLocal,
        @JsonProperty("timezone")
        String timezone,
        @JsonProperty("timestamp")
        Long timestamp,
        @JsonProperty("time_local")
        String timeLocal) {
}

package net.megogo.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TimestampData(
        @JsonProperty("utc_offset")
        Integer utcOffset,
        @JsonProperty("timestamp_gmt")
        Integer timestampGmt,
        @JsonProperty("timestamp_local")
        Integer timestampLocal,
        @JsonProperty("timezone")
        String timezone,
        @JsonProperty("timestamp")
        Integer timestamp,
        @JsonProperty("time_local")
        String timeLocal) {
}

package net.megogo.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ChannelData(
        @JsonProperty("id")
        Integer id,
        @JsonProperty("external_id")
        Integer externalId,
        @JsonProperty("title")
        String title,
        @JsonProperty("pictures")
        Object pictures,
        @JsonProperty("video_id")
        Integer videoId,
        @JsonProperty("programs")
        List<Program> programs
) {}

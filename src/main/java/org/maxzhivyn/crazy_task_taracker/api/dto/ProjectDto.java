package org.maxzhivyn.crazy_task_taracker.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    @NonNull
    private Long id;

    @NonNull
    private String name;

    @NonNull
    @JsonProperty("created_at")
    private Instant createdAt;
}

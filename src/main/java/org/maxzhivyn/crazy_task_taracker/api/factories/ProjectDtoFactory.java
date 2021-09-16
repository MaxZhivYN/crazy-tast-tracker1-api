package org.maxzhivyn.crazy_task_taracker.api.factories;

import org.maxzhivyn.crazy_task_taracker.api.dto.ProjectDto;
import org.maxzhivyn.crazy_task_taracker.store.entities.ProjectEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjectDtoFactory {
    public ProjectDto makeProjectDto(ProjectEntity entity) {
        return ProjectDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
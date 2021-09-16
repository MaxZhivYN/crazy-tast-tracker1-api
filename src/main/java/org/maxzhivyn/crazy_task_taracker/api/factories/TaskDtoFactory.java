package org.maxzhivyn.crazy_task_taracker.api.factories;

import org.maxzhivyn.crazy_task_taracker.api.dto.TaskDto;
import org.maxzhivyn.crazy_task_taracker.store.entities.TaskEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskDtoFactory {
    public TaskDto makeTaskDto(TaskEntity entity) {
        return TaskDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .description(entity.getDescription())
                .build();
    }
}

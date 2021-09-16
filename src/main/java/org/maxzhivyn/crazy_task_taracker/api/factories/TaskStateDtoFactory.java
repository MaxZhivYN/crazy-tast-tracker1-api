package org.maxzhivyn.crazy_task_taracker.api.factories;

import org.maxzhivyn.crazy_task_taracker.api.dto.TaskStateDto;
import org.maxzhivyn.crazy_task_taracker.store.entities.TaskStateEntity;
import org.springframework.stereotype.Component;

@Component
public class TaskStateDtoFactory {
    public TaskStateDto makeTaskStateDto(TaskStateEntity entity) {
        return TaskStateDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .ordinal(entity.getOrdinal())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

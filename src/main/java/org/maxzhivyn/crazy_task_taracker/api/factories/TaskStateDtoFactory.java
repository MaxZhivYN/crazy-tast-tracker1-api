package org.maxzhivyn.crazy_task_taracker.api.factories;

import lombok.RequiredArgsConstructor;
import org.maxzhivyn.crazy_task_taracker.api.dto.TaskStateDto;
import org.maxzhivyn.crazy_task_taracker.store.entities.TaskStateEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collector;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class TaskStateDtoFactory {
    private final TaskDtoFactory taskDtoFactory;

    public TaskStateDto makeTaskStateDto(TaskStateEntity entity) {
        return TaskStateDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .leftTaskStateId(entity.getLeftTaskState().map(TaskStateEntity::getId).orElse(null))
                .rightTaskStateId(entity.getRightTaskState().map(TaskStateEntity::getId).orElse(null))
                .createdAt(entity.getCreatedAt())
                .tasks(entity
                            .getTasks()
                            .stream()
                            .map(taskDtoFactory::makeTaskDto)
                            .collect(Collectors.toList())
                )
                .build();
    }
}

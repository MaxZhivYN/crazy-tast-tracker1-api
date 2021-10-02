package org.maxzhivyn.crazy_task_taracker.api.controllers;

import lombok.RequiredArgsConstructor;
import org.maxzhivyn.crazy_task_taracker.api.controllers.helpers.ControllerHelper;
import org.maxzhivyn.crazy_task_taracker.api.dto.AckDto;
import org.maxzhivyn.crazy_task_taracker.api.dto.ProjectDto;
import org.maxzhivyn.crazy_task_taracker.api.dto.TaskStateDto;
import org.maxzhivyn.crazy_task_taracker.api.exceptions.BadRequestException;
import org.maxzhivyn.crazy_task_taracker.api.factories.TaskStateDtoFactory;
import org.maxzhivyn.crazy_task_taracker.store.entities.ProjectEntity;
import org.maxzhivyn.crazy_task_taracker.store.entities.TaskStateEntity;
import org.maxzhivyn.crazy_task_taracker.store.repositories.ProjectRepository;
import org.maxzhivyn.crazy_task_taracker.store.repositories.TaskStateRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.websocket.server.PathParam;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Transactional
@RequiredArgsConstructor
public class TaskStateController {
    private final TaskStateRepository taskStateRepository;
    private final TaskStateDtoFactory taskStateDtoFactory;

    private final ControllerHelper controllerHelper;

    public static final String FETCH_TASK_SATES = "api/projects/{project_id}/tasks-states";
    public static final String CREATE_TASK_STATE = "api/projects/{project_id}/tasks-states";
    public static final String DELETE_TASK_STATE = "api/projects/{project_id}/tasks-states/{task_state_id}";
    public static final String EDIT_TASK_STATE = "api/projects/{project_id}/tasks-states/{task_state_id}";
    public static final String CHANGE_TASK_STATE_POSITION = "api/projects/{project_id}/tasks-states/{task_state_id}/position/change";


    @GetMapping(FETCH_TASK_SATES)
    public List<TaskStateDto> fetch(
            @PathVariable("project_id") Long projectId) {

        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        return project.getTaskStates().stream()
                .map(taskStateDtoFactory::makeTaskStateDto)
                //.sorted(Comparator.comparingLong(TaskStateDto::getId))
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_TASK_STATE)
    public TaskStateDto create(
            @PathVariable("project_id") Long projectId,
            @RequestParam(name = "task_state_name") String taskStateName) {

        if (taskStateName.trim().isEmpty()) {
            throw new BadRequestException("task_state_name can't be empty");
        }

        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        Optional<TaskStateEntity> optionalLastTaskSate = Optional.empty();

        for (TaskStateEntity currTaskState : project.getTaskStates()) {
            if (currTaskState.getName().equalsIgnoreCase(taskStateName)) {
                throw new BadRequestException("Task state %s already exists");
            }

            if (currTaskState.getRightTaskState().isEmpty()) {
                optionalLastTaskSate = Optional.of(currTaskState);
            }
        }

        TaskStateEntity newTaskState = TaskStateEntity.builder()
                .name(taskStateName)
                .project(project)
                .build();

        optionalLastTaskSate.ifPresent(lastTaskState -> {
            lastTaskState.setRightTaskState(newTaskState);
            newTaskState.setLeftTaskState(lastTaskState);

            taskStateRepository.save(lastTaskState);
        });

        taskStateRepository.saveAndFlush(newTaskState);

        return taskStateDtoFactory.makeTaskStateDto(newTaskState);
    }

    @DeleteMapping(DELETE_TASK_STATE)
    public AckDto delete(
            @PathVariable("project_id") Long projectId,
            @PathVariable("task_state_id") Long taskStateId) {

        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        boolean isExists = false;

        for (TaskStateEntity currTaskState : project.getTaskStates()) {
            if (currTaskState.getId().equals(taskStateId)) {
                isExists = true;
            }

            currTaskState.getRightTaskState().ifPresent(rightTaskState -> {
                if (rightTaskState.getId().equals(taskStateId)) {
                    currTaskState.setRightTaskState(null);
                }
            });

            currTaskState.getLeftTaskState().ifPresent(leftTaskState -> {
                if (leftTaskState.getId().equals(taskStateId)) {
                    currTaskState.setLeftTaskState(null);
                }
            });
        }

        if (!isExists) {
            throw new BadRequestException(String.format("No task state with id %s", taskStateId));
        }

        taskStateRepository.deleteById(taskStateId);


        return AckDto.makeDefault(true);
    }

    @PutMapping(EDIT_TASK_STATE)
    public TaskStateDto edit(
            @PathVariable("project_id") Long projectId,
            @PathVariable("task_state_id") Long taskStateId,
            @RequestParam(name = "task_state_name") String taskStateName) {

        if (taskStateName.trim().isEmpty()) {
            throw new BadRequestException("Name can't be empty");
        }

        controllerHelper.getProjectOrThrowException(projectId);

        taskStateRepository
                .findByName(taskStateName)
                .filter(taskState -> taskState.getProject().getId().equals(projectId))
                .ifPresent(taskState -> {
                    throw new BadRequestException("Task state already exists in this project");
                });

        TaskStateEntity taskState = taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() -> new BadRequestException("Incorrect task_state_id"));

        taskState.setName(taskStateName);

        taskStateRepository.saveAndFlush(taskState);

        return taskStateDtoFactory.makeTaskStateDto(taskState);
    }

    @PatchMapping(CHANGE_TASK_STATE_POSITION)
    public List<TaskStateDto> changePosition(
            @PathVariable("project_id") Long projectId,
            @PathVariable("task_state_id") Long taskStateId,
            @RequestParam(name = "changed_task_state_id") Long changedTaskStateId) {

        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        TaskStateEntity taskSate = taskStateRepository
                .findById(taskStateId)
                .filter(taskState -> taskState.getProject().getId().equals(projectId))
                .orElseThrow(() -> new BadRequestException("Incorrect task_state_id"));

        TaskStateEntity changedTaskSate = taskStateRepository
                .findById(changedTaskStateId)
                .filter(taskState -> taskState.getProject().getId().equals(projectId))
                .orElseThrow(() -> new BadRequestException("Incorrect changed_task_state_id"));

        Optional<TaskStateEntity> optionalLeftChangedTaskState = changedTaskSate.getLeftTaskState();
        Optional<TaskStateEntity> optionalLeftTaskState = taskSate.getLeftTaskState();
        Optional<TaskStateEntity> optionalRightTaskState = taskSate.getRightTaskState();

        if (optionalLeftChangedTaskState.isPresent()) {
            TaskStateEntity leftChangedTaskState = optionalLeftChangedTaskState.get();

            leftChangedTaskState.setRightTaskState(taskSate);

            taskSate.setLeftTaskState(leftChangedTaskState);

            taskStateRepository.save(leftChangedTaskState);
        }

        changedTaskSate.setLeftTaskState(taskSate);
        taskSate.setRightTaskState(changedTaskSate);

        optionalLeftTaskState
                .ifPresent(leftTaskState -> {
                    optionalRightTaskState.ifPresent(leftTaskState::setRightTaskState);
                    taskStateRepository.save(leftTaskState);
                });

        optionalRightTaskState
                .ifPresent(rightTaskState -> {
                    optionalLeftTaskState.ifPresent(rightTaskState::setLeftTaskState);
                    taskStateRepository.save(rightTaskState);
                });

        taskStateRepository.save(changedTaskSate);
        taskStateRepository.save(taskSate);

        return project.getTaskStates().stream()
                .map(taskStateDtoFactory::makeTaskStateDto)
                .collect(Collectors.toList());
    }
}

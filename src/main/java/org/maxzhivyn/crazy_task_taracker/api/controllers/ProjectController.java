package org.maxzhivyn.crazy_task_taracker.api.controllers;

import lombok.RequiredArgsConstructor;
import org.maxzhivyn.crazy_task_taracker.api.controllers.helpers.ControllerHelper;
import org.maxzhivyn.crazy_task_taracker.api.dto.AckDto;
import org.maxzhivyn.crazy_task_taracker.api.exceptions.BadRequestException;
import org.maxzhivyn.crazy_task_taracker.api.dto.ProjectDto;
import org.maxzhivyn.crazy_task_taracker.api.exceptions.NotFoundException;
import org.maxzhivyn.crazy_task_taracker.api.factories.ProjectDtoFactory;
import org.maxzhivyn.crazy_task_taracker.store.entities.ProjectEntity;
import org.maxzhivyn.crazy_task_taracker.store.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Transactional
@RestController
public class ProjectController {
    private final ProjectDtoFactory projectDtoFactory;
    private final ProjectRepository projectRepository;

    private final ControllerHelper controllerHelper;

    public static final String FETCH_PROJECT = "api/projects";
    public static final String CREATE_PROJECT = "api/projects";
    public static final String EDIT_PROJECT = "api/projects/{project_id}";
    public static final String DELETE_PROJECT = "api/projects/{project_id}";
    public static final String CREATE_OR_UPDATE_PROJECT = "api/projects";


    @GetMapping(FETCH_PROJECT)
    public List<ProjectDto> fetch(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {
        optionalPrefixName = optionalPrefixName
                .filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAllBy);

        return projectStream
                .map(projectDtoFactory::makeProjectDto)
                .collect(Collectors.toList());
    }


    @PostMapping(CREATE_PROJECT)
    public ProjectDto create(@RequestParam String name) {
        if (name.trim().isEmpty()) {
            throw new BadRequestException("Name can't be empty");
        }

        projectRepository
                .findByName(name)
                .ifPresent(projectEntity -> {
                    throw new BadRequestException(String.format("Project %s already exists.", name));
                });

        ProjectEntity project = projectRepository.saveAndFlush(
                ProjectEntity.builder()
                        .name(name)
                        .build()
        );

        return projectDtoFactory.makeProjectDto(project);
    }

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDto createOrUpdate(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_name", required = false) Optional<String> optionalProjectName) {

        optionalProjectName = optionalProjectName.filter(projectName -> !projectName.trim().isEmpty());

        if (optionalProjectId.isEmpty() && optionalProjectName.isEmpty()) {
            throw new BadRequestException("Project name can't be empty");
        }

        final ProjectEntity project = optionalProjectId
                .map(controllerHelper::getProjectOrThrowException)
                .orElseGet(() -> ProjectEntity.builder().build());

        optionalProjectName
                .ifPresent(projectName -> {
                    projectRepository
                            .findByName(projectName)
                            .filter(anotherProject -> !Objects.equals(project.getId(), anotherProject.getId()))
                            .ifPresent(anotherProject -> {
                                throw new BadRequestException(
                                        String.format("Project \"%s\" already exists.", projectName)
                                );
                            });

                    project.setName(projectName);
                });

        final ProjectEntity savedProject = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(savedProject);
    }


    @PatchMapping(EDIT_PROJECT)
    public ProjectDto edit(
            @PathVariable("project_id") Long projectId,
            @RequestParam String name) {

        if (name.trim().isEmpty()) {
            throw new BadRequestException("Name can't be empty");
        }

        ProjectEntity project = controllerHelper.getProjectOrThrowException(projectId);

        projectRepository
                .findByName(name)
                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), projectId))
                .ifPresent(anotherProject -> {
                    throw new BadRequestException(String.format("Project %s already exists.", name));
                });

        project.setName(name);
        project = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(project);
    }

    @DeleteMapping(DELETE_PROJECT)
    public AckDto delete(@PathVariable(value = "project_id") Long projectId)    {
        controllerHelper.getProjectOrThrowException(projectId);

        projectRepository.deleteById(projectId);

        return AckDto.makeDefault(true);
    }
}

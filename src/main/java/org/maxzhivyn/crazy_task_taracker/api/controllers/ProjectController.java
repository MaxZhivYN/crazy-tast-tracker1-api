package org.maxzhivyn.crazy_task_taracker.api.controllers;

import lombok.RequiredArgsConstructor;
import org.maxzhivyn.crazy_task_taracker.api.dto.AckDto;
import org.maxzhivyn.crazy_task_taracker.api.exceptions.BadRequestException;
import org.maxzhivyn.crazy_task_taracker.api.dto.ProjectDto;
import org.maxzhivyn.crazy_task_taracker.api.exceptions.NotFoundException;
import org.maxzhivyn.crazy_task_taracker.api.factories.ProjectDtoFactory;
import org.maxzhivyn.crazy_task_taracker.store.entities.ProjectEntity;
import org.maxzhivyn.crazy_task_taracker.store.repositories.ProjectRepository;
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

    public static final String FETCH_PROJECT = "api/projects";
    public static final String CREATE_PROJECT = "api/projects";
    public static final String EDIT_PROJECT = "api/projects/{project_id}";
    public static final String DELETE_PROJECT = "api/projects/{project_id}";


    @GetMapping(FETCH_PROJECT)
    public List<ProjectDto> fetch(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {
        optionalPrefixName = optionalPrefixName
                .filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElseGet(projectRepository::streamAll);

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


    @PatchMapping(EDIT_PROJECT)
    public ProjectDto edit(
            @PathVariable("project_id") Long projectId,
            @RequestParam String name) {

        if (name.trim().isEmpty()) {
            throw new BadRequestException("Name can't be empty");
        }

        ProjectEntity project = getProjectOrThrowException(projectId);

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
    public AckDto delete(@PathVariable(value = "project_id") Long projectId) {
        getProjectOrThrowException(projectId);

        projectRepository.deleteById(projectId);

        return AckDto.makeDefault(true);
    }

    private ProjectEntity getProjectOrThrowException(Long projectId) {
        return projectRepository
               .findById(projectId)
               .orElseThrow(() ->
                    new NotFoundException(String.format("Project with id %s not found", projectId))
               );
    }
}

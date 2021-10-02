package org.maxzhivyn.crazy_task_taracker.api.controllers.helpers;


import lombok.RequiredArgsConstructor;
import org.maxzhivyn.crazy_task_taracker.api.exceptions.NotFoundException;
import org.maxzhivyn.crazy_task_taracker.store.entities.ProjectEntity;
import org.maxzhivyn.crazy_task_taracker.store.repositories.ProjectRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ControllerHelper {
    private final ProjectRepository projectRepository;

    public ProjectEntity getProjectOrThrowException(Long projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Project with id %s not found", projectId))
                );
    }
}

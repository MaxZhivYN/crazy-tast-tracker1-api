package org.maxzhivyn.crazy_task_taracker.store.repositories;

import org.maxzhivyn.crazy_task_taracker.store.entities.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskStateRepository extends JpaRepository<TaskStateEntity, Long> {
    Optional<TaskStateEntity> findTaskStateEntityByRightTaskStateIdIsNullAndProjectId(Long projectId);
    Optional<TaskStateEntity> findByName(String taskStateName);
}

package org.maxzhivyn.crazy_task_taracker.store.repositories;

import org.maxzhivyn.crazy_task_taracker.store.entities.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskStateRepository extends JpaRepository<TaskStateEntity, Long> {
}

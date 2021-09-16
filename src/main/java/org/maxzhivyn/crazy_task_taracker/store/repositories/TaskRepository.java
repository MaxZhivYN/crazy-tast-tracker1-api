package org.maxzhivyn.crazy_task_taracker.store.repositories;

import org.maxzhivyn.crazy_task_taracker.store.entities.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
}

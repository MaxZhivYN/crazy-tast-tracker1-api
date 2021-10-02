package org.maxzhivyn.crazy_task_taracker.store.repositories;

import org.maxzhivyn.crazy_task_taracker.store.entities.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    Optional<ProjectEntity> findByName(String name);
    Stream<ProjectEntity> streamAllBy();
    Stream<ProjectEntity> streamAllByNameStartsWithIgnoreCase(String prefixName);
}

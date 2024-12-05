package org.example.project_ice.repository;

import org.example.project_ice.Category;
import org.example.project_ice.entity.Task;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskRepo extends JpaRepository<Task, Long> {
    List<Task> findByUser_Username(String username);
    Task findByTaskIdAndUser_Username(Long taskId, String username);
    Task findByTaskId(Long taskId);
    List<Task> findByUser_UsernameAndStatus(String username, String status);

    List<Task> findByUser_UsernameAndCategory(String username, Category category);
    List<Task> findByUser_Username(String username, Sort sort);

    List<Task> findByCategory(Category category);

    List<Task> findByStatus(String status);

    Page<Task> findByTitleContainingIgnoreCase(String searchTerm, Pageable pageable);

    Page<Task> findByUser_Username(String username, Pageable pageable);
    Page<Task> findByUser_UsernameAndCategory(String username, Category category, Pageable pageable);
    Page<Task> findByUser_UsernameAndStatus(String username, String status, Pageable pageable);

    Page<Task> findByCategory(Category category, Pageable pageable);
    Page<Task> findByStatus(String status, Pageable pageable);

    Page<Task> findByTitleContaining(String title, Pageable pageable);
}


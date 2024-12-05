package org.example.project_ice.repository;
import org.example.project_ice.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepo extends JpaRepository<Category, Long> {
}


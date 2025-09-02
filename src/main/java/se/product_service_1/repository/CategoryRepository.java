package se.product_service_1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.product_service_1.model.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    void deleteByName(String name);
}

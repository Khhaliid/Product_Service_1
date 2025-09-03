package se.product_service_1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.product_service_1.model.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findByNameContainingIgnoreCase(String name);

    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) IN :tagNames")
    List<Tag> findByNamesIgnoreCase(@Param("tagNames") List<String> tagNames);

    boolean existsByName(String name);
}
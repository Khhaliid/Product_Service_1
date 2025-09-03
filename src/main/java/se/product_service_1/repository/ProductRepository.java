package se.product_service_1.repository;

import se.product_service_1.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategoryName(String categoryName);

    Optional<Product> findByName(String name);

    // Alla sök-metoder använder JOIN FETCH för att ladda taggar direkt
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.tags JOIN p.tags t WHERE t.name IN :tagNames")
    List<Product> findByTagNames(@Param("tagNames") List<String> tagNames);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.tags WHERE p.id IN " +
            "(SELECT pt.id FROM Product pt JOIN pt.tags t WHERE t.name IN :tagNames " +
            "GROUP BY pt.id HAVING COUNT(DISTINCT t.name) = :tagCount)")
    List<Product> findByAllTagNames(@Param("tagNames") List<String> tagNames, @Param("tagCount") long tagCount);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.tags JOIN p.tags t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :tagName, '%'))")
    List<Product> findByTagNameContaining(@Param("tagName") String tagName);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.tags JOIN p.tags t WHERE p.category.name = :categoryName AND t.name IN :tagNames")
    List<Product> findByCategoryAndTagNames(@Param("categoryName") String categoryName, @Param("tagNames") List<String> tagNames);

    // Lägg till eager loading för findAll och andra metoder
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.tags")
    List<Product> findAllWithTags();
}
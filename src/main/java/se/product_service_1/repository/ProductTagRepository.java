package se.product_service_1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.product_service_1.model.ProductTag;

import java.util.List;

public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {

    List<ProductTag> findByProductId(Long productId);

    List<ProductTag> findByTagId(Long tagId);

    void deleteByProductId(Long productId);

    void deleteByProductIdAndTagId(Long productId, Long tagId);

    @Query("SELECT pt.productId FROM ProductTag pt WHERE pt.tagId IN :tagIds")
    List<Long> findProductIdsByTagIds(@Param("tagIds") List<Long> tagIds);

    @Query("SELECT pt.tagId FROM ProductTag pt WHERE pt.productId = :productId")
    List<Long> findTagIdsByProductId(@Param("productId") Long productId);
}
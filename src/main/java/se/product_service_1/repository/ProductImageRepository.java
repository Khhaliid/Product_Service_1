package se.product_service_1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.product_service_1.model.ProductImage;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);
    Optional<ProductImage> findByProductIdAndFileName(Long productId, String fileName);
    void deleteByProductId(Long productId);
}
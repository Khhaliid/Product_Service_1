package se.product_service_1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import se.product_service_1.config.FileStorageConfig;
import se.product_service_1.exception.BadRequestException;
import se.product_service_1.exception.ResourceNotFoundException;
import se.product_service_1.model.Product;
import se.product_service_1.model.ProductImage;
import se.product_service_1.repository.ProductImageRepository;
import se.product_service_1.repository.ProductRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "application/pdf"
    );

    private final Path fileStorageLocation;
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    public FileStorageService(FileStorageConfig fileStorageConfig,
                              ProductImageRepository productImageRepository,
                              ProductRepository productRepository) {
        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public ProductImage storeFile(Long productId, MultipartFile file) {
        log.info("Storing file for product ID: {}", productId);

        // Validera produkten
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Validera filtypen
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid file type. Allowed types: JPEG, PNG, GIF, PDF");
        }

        try {
            // Normalisera filnamnet
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFilename.contains("..")) {
                throw new BadRequestException("Filename contains invalid path sequence: " + originalFilename);
            }

            // Generera unikt filnamn för att undvika konflikter
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Spara filen på filsystemet
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Skapa och spara ProductImage-entitet
            ProductImage productImage = ProductImage.builder()
                    .productId(productId)
                    .fileName(originalFilename)
                    .contentType(contentType)
                    .filePath(uniqueFilename)
                    .build();

            return productImageRepository.save(productImage);
        } catch (IOException ex) {
            log.error("Could not store file", ex);
            throw new RuntimeException("Could not store file. Please try again.", ex);
        }
    }

    public Resource loadFileAsResource(Long productId, String filename) {
        log.info("Loading file for product ID: {} with filename: {}", productId, filename);

        try {
            ProductImage productImage = productImageRepository.findByProductIdAndFileName(productId, filename)
                    .orElseThrow(() -> new ResourceNotFoundException("File not found: " + filename));

            Path filePath = this.fileStorageLocation.resolve(productImage.getFilePath()).normalize();
            Resource resource = new FileSystemResource(filePath.toFile());

            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + filename);
            }
        } catch (Exception ex) {
            log.error("Could not load file", ex);
            throw new ResourceNotFoundException("File not found: " + filename);
        }
    }

    public List<ProductImage> getProductImages(Long productId) {
        // Validera produkten
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        return productImageRepository.findByProductId(productId);
    }

    public void deleteProductImage(Long productId, Long imageId) {
        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        if (!productImage.getProductId().equals(productId)) {
            throw new BadRequestException("Image does not belong to the specified product");
        }

        try {
            // Ta bort filen från filsystemet
            Path filePath = this.fileStorageLocation.resolve(productImage.getFilePath()).normalize();
            Files.deleteIfExists(filePath);

            // Ta bort databasposten
            productImageRepository.deleteById(imageId);
            log.info("Deleted product image with ID: {}", imageId);
        } catch (IOException ex) {
            log.error("Could not delete file", ex);
            throw new RuntimeException("Could not delete file. Please try again.", ex);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
package se.product_service_1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import se.product_service_1.dto.ProductImageResponse;
import se.product_service_1.exception.BadRequestException;
import se.product_service_1.model.ProductImage;
import se.product_service_1.service.FileStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
@AllArgsConstructor
public class ProductImageController {

    private final FileStorageService fileStorageService;

    // Användning av @Data för att få getter/setter automatiskt
    @Data
    public static class FileUploadRequest {
        @Schema(type = "string", format = "binary")
        private MultipartFile file;
    }

    @Operation(summary = "Upload product image",
            description = "Upload an image for a specific product")
    @PostMapping(value = "/{productId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductImageResponse> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Filen är tom eller saknas");
        }

        ProductImage productImage = fileStorageService.storeFile(productId, file);

        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/product/{productId}/image/{fileName}")
                .buildAndExpand(productId, productImage.getFileName())
                .toUriString();

        ProductImageResponse response = ProductImageResponse.builder()
                .id(productImage.getId())
                .productId(productImage.getProductId())
                .fileName(productImage.getFileName())
                .contentType(productImage.getContentType())
                .downloadUrl(downloadUrl)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get product image",
            description = "Get an image for a specific product by filename")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "image/*"))
    @GetMapping("/{productId}/image/{fileName}")
    public ResponseEntity<Resource> getProductImage(
            @PathVariable Long productId,
            @PathVariable String fileName) {

        Resource resource = fileStorageService.loadFileAsResource(productId, fileName);
        String contentType = determineContentType(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "List product images",
            description = "Get a list of all images for a specific product")
    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImageResponse>> listProductImages(@PathVariable Long productId) {
        List<ProductImage> productImages = fileStorageService.getProductImages(productId);

        List<ProductImageResponse> responseList = productImages.stream()
                .map(image -> {
                    String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/product/{productId}/image/{fileName}")
                            .buildAndExpand(productId, image.getFileName())
                            .toUriString();

                    return ProductImageResponse.builder()
                            .id(image.getId())
                            .productId(image.getProductId())
                            .fileName(image.getFileName())
                            .contentType(image.getContentType())
                            .downloadUrl(downloadUrl)
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "Delete product image",
            description = "Delete an image for a specific product")
    @DeleteMapping("/{productId}/image/{imageId}")
    public ResponseEntity<String> deleteProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        fileStorageService.deleteProductImage(productId, imageId);
        return ResponseEntity.ok("Image deleted successfully");
    }

    @Operation(summary = "Test file upload",
            description = "Test endpoint for file upload")
    @PostMapping(value = "/test-file-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> testFileUpload(
            @RequestParam("file") MultipartFile file) {

        Map<String, String> response = new HashMap<>();

        if (file == null) {
            response.put("error", "Ingen fil hittades i förfrågan");
            return ResponseEntity.badRequest().body(response);
        }

        if (file.isEmpty()) {
            response.put("error", "Filen är tom");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Skapa uploads-mappen om den inte finns
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generera ett unikt filnamn
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String uniqueFilename = UUID.randomUUID().toString();
            if (originalFilename.contains(".")) {
                uniqueFilename += originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Spara filen
            Path filePath = uploadDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            response.put("status", "success");
            response.put("originalFilename", originalFilename);
            response.put("savedAs", uniqueFilename);
            response.put("size", String.valueOf(file.getSize()));
            response.put("contentType", file.getContentType());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("error", "Kunde inte ladda upp filen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String determineContentType(String fileName) {
        if (fileName.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.toLowerCase().endsWith(".pdf")) {
            return "application/pdf";
        } else {
            return "application/octet-stream";
        }
    }
}
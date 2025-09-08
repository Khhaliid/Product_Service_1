package se.product_service_1.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.product_service_1.dto.*;
import se.product_service_1.model.Category;
import se.product_service_1.model.Product;
import se.product_service_1.service.CategoryService;
import se.product_service_1.service.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
@AllArgsConstructor
public class ProductController {
    private ProductService productService;
    private CategoryService categoryService;

    @Operation(summary = "Get all products", description = "Returns all products")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductResponse> responseList = new ArrayList<>();
        for (Product product : products) {
            ProductResponse productResponse = buildProductResponse(product);
            responseList.add(productResponse);
        }
        return ResponseEntity.ok(responseList);
    }
    @Operation(summary = "Get all products from a category", description = "Returns all products from a specific category")
    @GetMapping("/{productCategory}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String productCategory) {
        Category category = categoryService.getCategoryByName(productCategory);
        List<Product> products = productService.getProductsByCategory(category.getName());
        List<ProductResponse> responseList = new ArrayList<>();
        for (Product product : products) {
            ProductResponse productResponse = buildProductResponse(product);
            responseList.add(productResponse);
        }
        return ResponseEntity.ok(responseList);
    }
    @Operation(summary = "Add a new product", description = "Saves a new product")
    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(@RequestBody ProductRequest productRequest) {
        Category category = categoryService.getCategoryByName(productRequest.getCategoryName());
        Product product = Product.builder()
                .category(category)
                .price(productRequest.getPrice())
                .name(productRequest.getProductName())
                .stockQuantity(productRequest.getStockQuantity())
                .build();

        Product savedProduct;
        if (productRequest.getTagNames() != null && !productRequest.getTagNames().isEmpty()) {
            savedProduct = productService.addProductWithTags(product, productRequest.getTagNames());
        } else {
            savedProduct = productService.addProduct(product);
        }

        ProductResponse productResponse = buildProductResponse(savedProduct);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
    }
    @Operation(summary = "Delete a product", description = "Delete a product by name")
    @DeleteMapping
    public ResponseEntity<String> deleteProduct(@RequestBody ProductDeleteRequest productDelete) {
        Product product = productService.getProductByName(productDelete.getProductName());
        productService.deleteProduct(product.getId());
        return ResponseEntity.ok("Product deleted.");
    }
    @Operation(summary = "Update existing product", description = "Update existing product, remove fields you dont want to update")
    @PutMapping
    public ResponseEntity<ProductResponse> updateProduct(@RequestBody ProductUpdateRequest productUpdate) {
        Product product = productService.getProductByName(productUpdate.getCurrentProductName());

        if (productUpdate.getNewProductName() != null) {
            product.setName(productUpdate.getNewProductName());
        }
        if (productUpdate.getPrice() != null) {
            product.setPrice(productUpdate.getPrice());
        }
        if (productUpdate.getCategoryName() != null) {
            Category category = categoryService.getCategoryByName(productUpdate.getCategoryName());
            product.setCategory(category);
        }

        Product updatedProduct = productService.updateProduct(product);
        ProductResponse productResponse = buildProductResponse(updatedProduct);
        return ResponseEntity.ok(productResponse);
    }
    @Operation(summary = "Update stockQuantity", description = "Add or subtract stockQuantity from current stock")
    @PostMapping("/inventoryManager")
    public ResponseEntity<ProductResponse> updateStockQuantity(@RequestBody InventoryManagementRequest inventoryManagementRequest) {
        Product product = productService.getProductByName(inventoryManagementRequest.getProductName());

        product.setStockQuantity(product.getStockQuantity() + inventoryManagementRequest.getInventoryChange());
        Product updatedProduct = productService.updateProduct(product);

        return ResponseEntity.ok(buildProductResponse(updatedProduct));
    }


    // === NYA ENDPOINTS FÖR TAGG-FUNKTIONALITET ===

    @PostMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestBody ProductSearchRequest searchRequest) {
        List<Product> products = productService.searchProducts(searchRequest);
        List<ProductResponse> responseList = products.stream()
                .map(this::buildProductResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/search/tags")
    public ResponseEntity<List<ProductResponse>> searchProductsByTags(@RequestParam List<String> tags) {
        List<Product> products = productService.searchProductsByTags(tags);
        List<ProductResponse> responseList = products.stream()
                .map(this::buildProductResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/search/tags/all")
    public ResponseEntity<List<ProductResponse>> searchProductsByAllTags(@RequestParam List<String> tags) {
        List<Product> products = productService.searchProductsByAllTags(tags);
        List<ProductResponse> responseList = products.stream()
                .map(this::buildProductResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/search/tag-pattern")
    public ResponseEntity<List<ProductResponse>> searchProductsByTagPattern(@RequestParam String pattern) {
        List<Product> products = productService.searchProductsByTagPattern(pattern);
        List<ProductResponse> responseList = products.stream()
                .map(this::buildProductResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @PostMapping("/{productId}/tags")
    public ResponseEntity<ProductResponse> addTagsToProduct(
            @PathVariable Long productId,
            @RequestBody List<String> tagNames) {
        Product product = productService.addTagsToProduct(productId, tagNames);
        ProductResponse response = buildProductResponse(product);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}/tags")
    public ResponseEntity<ProductResponse> removeTagsFromProduct(
            @PathVariable Long productId,
            @RequestBody List<String> tagNames) {
        Product product = productService.removeTagsFromProduct(productId, tagNames);
        ProductResponse response = buildProductResponse(product);
        return ResponseEntity.ok(response);
    }

    private ProductResponse buildProductResponse(Product product) {
        // Hämta taggar för produkten via ProductService
        List<String> tagNames = productService.getTagNamesForProduct(product.getId());

        return ProductResponse.builder()
                .id(product.getId())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "Unknown")
                .productName(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .tagNames(tagNames)
                .build();
    }
}
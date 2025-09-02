package se.product_service_1.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.product_service_1.dto.ProductDeleteRequest;
import se.product_service_1.dto.ProductRequest;
import se.product_service_1.dto.ProductResponse;
import se.product_service_1.dto.ProductUpdateRequest;
import se.product_service_1.model.Category;
import se.product_service_1.model.Product;
import se.product_service_1.service.CategoryService;
import se.product_service_1.service.ProductService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/product")
@AllArgsConstructor
public class ProductController {
    private ProductService productService;
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductResponse> responseList = new ArrayList<>();
        ProductResponse productResponse;
        for (Product product : products) {
            productResponse = buildProductResponse(product);
            responseList.add(productResponse);
        }
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{productCategory}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable String productCategory) {
        Category category = categoryService.getCategoryByName(productCategory);
        List<Product> products = productService.getProductsByCategory(category.getName());
        List<ProductResponse> responseList = new ArrayList<>();
        ProductResponse productResponse;
        for (Product product : products) {
            productResponse = buildProductResponse(product);
            responseList.add(productResponse);
        }
        return ResponseEntity.ok(responseList);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(@RequestBody ProductRequest productRequest) {
        Category category = categoryService.getCategoryByName(productRequest.getCategoryName());
        Product product = Product.builder()
                .category(category)
                .price(productRequest.getPrice())
                .name(productRequest.getProductName())
                .build();
        productService.addProduct(product);
        ProductResponse productResponse = buildProductResponse(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteProduct(@RequestBody ProductDeleteRequest productDelete) {
        Product product = productService.getProductByName(productDelete.getProductName());
        productService.deleteProduct(product.getId());
        return ResponseEntity.ok("Product deleted.");
    }

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

    private ProductResponse buildProductResponse(Product product) {
        return ProductResponse.builder()
                .categoryName(product.getCategory().getName())
                .productName(product.getName())
                .price(product.getPrice())
                .build();
    }
}

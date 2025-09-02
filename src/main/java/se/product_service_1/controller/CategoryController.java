package se.product_service_1.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.product_service_1.dto.CategoryRequest;
import se.product_service_1.dto.CategoryResponse;
import se.product_service_1.model.Category;
import se.product_service_1.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/category")
@AllArgsConstructor
public class CategoryController {

    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Category> getCategoryByName(@PathVariable String name) {
        Category category = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(category);
    }


    @PostMapping
    public ResponseEntity<CategoryResponse> addCategory(@RequestBody CategoryRequest categoryRequest) {
        Category category = Category.builder()
                .name(categoryRequest.getName())
                .build();
        Category savedCategory = categoryService.addCategory(category);
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .name(savedCategory.getName())
                .build();
        return ResponseEntity.ok(categoryResponse);
    }

    @DeleteMapping("/name/{name}")
    public ResponseEntity<Category> deleteCategoryByName(@PathVariable String name) {
        categoryService.deleteCategoryByName(name);
        return ResponseEntity.ok().build();
    }

}

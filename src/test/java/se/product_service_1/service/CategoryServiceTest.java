package se.product_service_1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.product_service_1.exception.CategoryAlreadyExistsException;
import se.product_service_1.exception.CategoryNotEmptyException;
import se.product_service_1.exception.CategoryNotFoundException;
import se.product_service_1.model.Category;
import se.product_service_1.model.Product;
import se.product_service_1.repository.CategoryRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CategoryService categoryService;

    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();
    }

    @Test
    void getCategoryByName_ShouldReturnCategory_WhenExists() {
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(sampleCategory));

        Category result = categoryService.getCategoryByName("Electronics");

        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        verify(categoryRepository, times(1)).findByName("Electronics");
    }

    @Test
    void getCategoryByName_ShouldThrow_WhenNotFound() {
        when(categoryRepository.findByName("Unknown")).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.getCategoryByName("Unknown"));
    }

    @Test
    void addCategory_ShouldSave_WhenNotExists() {
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(sampleCategory)).thenReturn(sampleCategory);

        Category result = categoryService.addCategory(sampleCategory);

        assertEquals("Electronics", result.getName());
        verify(categoryRepository).save(sampleCategory);
    }

    @Test
    void addCategory_ShouldThrow_WhenAlreadyExists() {
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(sampleCategory));

        assertThrows(CategoryAlreadyExistsException.class,
                () -> categoryService.addCategory(sampleCategory));
    }

    @Test
    void deleteCategoryByName_ShouldDelete_WhenEmptyCategory() {
        when(productService.getProductsByCategory("Electronics")).thenReturn(Collections.emptyList());
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(sampleCategory));

        categoryService.deleteCategoryByName("Electronics");

        verify(categoryRepository).deleteByName("Electronics");
    }

    @Test
    void deleteCategoryByName_ShouldThrow_WhenCategoryNotEmpty() {
        when(productService.getProductsByCategory("Electronics"))
                .thenReturn(List.of(Product.builder().id(1L).name("TestProduct").build()));

        assertThrows(CategoryNotEmptyException.class,
                () -> categoryService.deleteCategoryByName("Electronics"));

        verify(categoryRepository, never()).deleteByName(anyString());
    }

    @Test
    void deleteCategoryByName_ShouldThrow_WhenCategoryNotFound() {
        when(productService.getProductsByCategory("Electronics")).thenReturn(Collections.emptyList());
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.deleteCategoryByName("Electronics"));
    }

    @Test
    void getAllCategories_ShouldReturnList() {
        when(categoryRepository.findAll()).thenReturn(List.of(sampleCategory));

        List<Category> result = categoryService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
    }
}
package se.product_service_1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.product_service_1.exception.ProductAlreadyExistsException;
import se.product_service_1.exception.ProductNotFoundException;
import se.product_service_1.model.Category;
import se.product_service_1.model.Product;
import se.product_service_1.model.Tag;
import se.product_service_1.repository.CategoryRepository;
import se.product_service_1.repository.ProductRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagService tagService;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleProduct = Product.builder()
                .id(1L)
                .name("Laptop")
                .price(999.99)
                .category(Category.builder().name("Electronics").build())
                .tags(new HashSet<>())
                .build();
    }

    @Test
    void addProduct_ShouldSave_WhenNotExists() {
        when(productRepository.findByName("Laptop")).thenReturn(Optional.empty());
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);

        Product result = productService.addProduct(sampleProduct);

        assertEquals("Laptop", result.getName());
        verify(productRepository).save(sampleProduct);
    }

    @Test
    void addProduct_ShouldThrow_WhenAlreadyExists() {
        when(productRepository.findByName("Laptop")).thenReturn(Optional.of(sampleProduct));

        assertThrows(ProductAlreadyExistsException.class,
                () -> productService.addProduct(sampleProduct));

        verify(productRepository, never()).save(any());
    }

    @Test
    void addProductWithTags_ShouldSaveWithTags() {
        List<String> tagNames = List.of("Tech", "Gaming");
        Set<Tag> tags = Set.of(Tag.builder().name("Tech").build(), Tag.builder().name("Gaming").build());

        when(productRepository.findByName("Laptop")).thenReturn(Optional.empty());
        when(tagService.getOrCreateTags(tagNames)).thenReturn(tags);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.addProductWithTags(sampleProduct, tagNames);

        assertEquals(2, result.getTags().size());
        verify(tagService).getOrCreateTags(tagNames);
        verify(productRepository).save(sampleProduct);
    }

    @Test
    void addProductWithTags_ShouldThrow_WhenAlreadyExists() {
        when(productRepository.findByName("Laptop")).thenReturn(Optional.of(sampleProduct));

        assertThrows(ProductAlreadyExistsException.class,
                () -> productService.addProductWithTags(sampleProduct, List.of("Tech")));
    }

    @Test
    void deleteProduct_ShouldDelete_WhenExists() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_ShouldNotDelete_WhenNotExists() {
        when(productRepository.existsById(1L)).thenReturn(false);

        productService.deleteProduct(1L);

        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void getProductByName_ShouldReturn_WhenExists() {
        when(productRepository.findByName("Laptop")).thenReturn(Optional.of(sampleProduct));

        Product result = productService.getProductByName("Laptop");

        assertEquals("Laptop", result.getName());
    }

    @Test
    void getProductByName_ShouldThrow_WhenNotFound() {
        when(productRepository.findByName("Laptop")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductByName("Laptop"));
    }

    @Test
    void getProductById_ShouldReturn_WhenExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Product result = productService.getProductById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getProductById_ShouldThrow_WhenNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> productService.getProductById(1L));
    }

    @Test
    void getAllProducts_ShouldReturnList() {
        when(productRepository.findAllWithTags()).thenReturn(List.of(sampleProduct));

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
    }

    @Test
    void getProductsByCategory_ShouldReturnList() {
        when(productRepository.findByCategoryName("Electronics")).thenReturn(List.of(sampleProduct));

        List<Product> result = productService.getProductsByCategory("Electronics");

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
    }

    @Test
    void updateProduct_ShouldSaveAndReturnUpdated() {
        sampleProduct.setName("Updated Laptop");
        when(productRepository.save(sampleProduct)).thenReturn(sampleProduct);

        Product result = productService.updateProduct(sampleProduct);

        assertEquals("Updated Laptop", result.getName());
        verify(productRepository).save(sampleProduct);
    }
}
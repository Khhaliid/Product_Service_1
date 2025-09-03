package se.product_service_1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import se.product_service_1.dto.*;
import se.product_service_1.model.Category;
import se.product_service_1.model.Product;
import se.product_service_1.model.Tag;
import se.product_service_1.service.CategoryService;
import se.product_service_1.service.ProductService;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product createSampleProduct() {
        return Product.builder()
                .id(1L)
                .name("Laptop")
                .price(999.99) // Double
                .category(Category.builder().name("Electronics").build())
                .tags(Set.of(Tag.builder().name("Tech").build()))
                .build();
    }

    @Test
    void getAllProducts_ShouldReturnList() throws Exception {
        Mockito.when(productService.getAllProducts()).thenReturn(List.of(createSampleProduct()));

        mockMvc.perform(get("/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Laptop"))
                .andExpect(jsonPath("$[0].categoryName").value("Electronics"))
                .andExpect(jsonPath("$[0].tagNames[0]").value("Tech"));
    }

    @Test
    void getProductsByCategory_ShouldReturnList() throws Exception {
        Mockito.when(categoryService.getCategoryByName("Electronics"))
                .thenReturn(Category.builder().name("Electronics").build());
        Mockito.when(productService.getProductsByCategory("Electronics"))
                .thenReturn(List.of(createSampleProduct()));

        mockMvc.perform(get("/product/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Laptop"));
    }

    @Test
    void addProduct_WithTags_ShouldReturnCreatedProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setCategoryName("Electronics");
        request.setProductName("Laptop");
        request.setPrice(999.99);
        request.setTagNames(List.of("Tech"));

        Mockito.when(categoryService.getCategoryByName("Electronics"))
                .thenReturn(Category.builder().name("Electronics").build());
        Mockito.when(productService.addProductWithTags(any(Product.class), anyList()))
                .thenReturn(createSampleProduct());

        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.tagNames[0]").value("Tech"));
    }

    @Test
    void addProduct_WithoutTags_ShouldReturnCreatedProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setCategoryName("Electronics");
        request.setProductName("Laptop");
        request.setPrice(999.99);

        Mockito.when(categoryService.getCategoryByName("Electronics"))
                .thenReturn(Category.builder().name("Electronics").build());
        Mockito.when(productService.addProduct(any(Product.class)))
                .thenReturn(createSampleProduct());

        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Laptop"));
    }

    @Test
    void deleteProduct_ShouldReturnOk() throws Exception {
        ProductDeleteRequest deleteRequest = new ProductDeleteRequest();
        deleteRequest.setProductName("Laptop");

        Mockito.when(productService.getProductByName("Laptop")).thenReturn(createSampleProduct());
        Mockito.doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Product deleted."));
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setCurrentProductName("Laptop");
        updateRequest.setNewProductName("Gaming Laptop");
        updateRequest.setPrice(1299.99);
        updateRequest.setCategoryName("Gaming");

        Product existing = createSampleProduct();
        Product updated = Product.builder()
                .id(1L)
                .name("Gaming Laptop")
                .price(1299.99)
                .category(Category.builder().name("Gaming").build())
                .tags(existing.getTags())
                .build();

        Mockito.when(productService.getProductByName("Laptop")).thenReturn(existing);
        Mockito.when(categoryService.getCategoryByName("Gaming"))
                .thenReturn(Category.builder().name("Gaming").build());
        Mockito.when(productService.updateProduct(any(Product.class))).thenReturn(updated);

        mockMvc.perform(put("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Gaming Laptop"))
                .andExpect(jsonPath("$.categoryName").value("Gaming"));
    }
}
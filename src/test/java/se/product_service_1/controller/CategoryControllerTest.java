package se.product_service_1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import se.product_service_1.dto.CategoryRequest;
import se.product_service_1.model.Category;
import se.product_service_1.service.CategoryService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCategories_ShouldReturnList() throws Exception {
        List<Category> categories = Arrays.asList(
                Category.builder().name("Electronics").build(),
                Category.builder().name("Books").build()
        );

        Mockito.when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[1].name").value("Books"));
    }

    @Test
    void getCategoryByName_ShouldReturnCategory() throws Exception {
        Category category = Category.builder().name("Toys").build();
        Mockito.when(categoryService.getCategoryByName("Toys")).thenReturn(category);

        mockMvc.perform(get("/category/name/Toys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Toys"));
    }

    @Test
    void addCategory_ShouldReturnCategoryResponse() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Garden");

        Category savedCategory = Category.builder().name("Garden").build();
        Mockito.when(categoryService.addCategory(any(Category.class))).thenReturn(savedCategory);

        mockMvc.perform(post("/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Garden"));
    }

    @Test
    void deleteCategoryByName_ShouldReturnOk() throws Exception {
        Mockito.doNothing().when(categoryService).deleteCategoryByName(eq("Clothes"));

        mockMvc.perform(delete("/category/name/Clothes"))
                .andExpect(status().isOk());
    }
}

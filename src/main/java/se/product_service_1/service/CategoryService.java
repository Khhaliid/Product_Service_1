package se.product_service_1.service;


import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.product_service_1.exception.CategoryAlreadyExistsException;
import se.product_service_1.exception.CategoryNotEmptyException;
import se.product_service_1.exception.CategoryNotFoundException;
import se.product_service_1.model.Category;
import se.product_service_1.repository.CategoryRepository;

import java.util.List;

@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryRepository categoryRepository;
    private final ProductService productService;


    public CategoryService(CategoryRepository categoryRepository, ProductService productService) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
    }

    public Category getCategoryByName(String name) {
        try {
            log.info("Hämtar kategori med namn {}", name);
            return categoryRepository.findByName(name).orElseThrow(() -> new CategoryNotFoundException(name));
        } catch (Exception e) {
            log.error("Fel uppstod vid hämtning av kategori med namn: {}", name, e);
            throw new CategoryNotFoundException("Kategori med namn:" + name + " existerar inte.");
        }
    }

    public Category addCategory(Category category) {
        log.info("addCategory - försök spara category: categoryName={}", category.getName());

        if(categoryRepository.findByName(category.getName()).isPresent()) {
            throw new CategoryAlreadyExistsException("Category med Id " + category.getId() + "finns redan.");
        }

        Category savedCategory = categoryRepository.save(category);
        log.info("addCategory - sparad category med Id={}", savedCategory.getId());
        return savedCategory;
    }
    @Transactional
    public void deleteCategoryByName(String name) {
        if (productService.getProductsByCategory(name).isEmpty()) {
            Category category = categoryRepository.findByName(name)
                    .orElseThrow(() -> {
                        log.error("Fel uppstod vid hämtning av kategori med namn: {}", name);
                        return new CategoryNotFoundException("Kategori med namn: " + name + " existerar inte.");
                    });

            categoryRepository.deleteByName(name);
        } else {
            throw new CategoryNotEmptyException("Kategori med namn: " + name + " är inte tom.");
        }

    }

    public List<Category> getAllCategories() {
        log.debug("getAllCategories - hämta alla categories");
        List<Category> list = categoryRepository.findAll();
        log.debug("getAllCategories - antal categories={}", list.size());
        return list;
    }



}

package se.product_service_1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.product_service_1.dto.ProductRequest;
import se.product_service_1.exception.CategoryNotFoundException;
import se.product_service_1.exception.ProductAlreadyExistsException;
import se.product_service_1.exception.ProductNotFoundException;
import se.product_service_1.model.Category;
import se.product_service_1.model.Product;
import se.product_service_1.repository.CategoryRepository;
import se.product_service_1.repository.ProductRepository;

import java.util.List;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Constructor for dependency injection of ProductRepository.
     *
     * @param productRepository repository used to interact with product data
     */
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Adds a new product to the database. Throws an exception if a product
     * with the same productId already exists.
     *
     * @param product the product entity to save
     * @return the saved Product entity
     * @throws ProductAlreadyExistsException if productId is already taken
     */
    public Product addProduct(Product product) {
        log.info("addProduct – försök spara produkt: productName={}, category={}",
                product.getName(), product.getCategory());
        // Check if a product with this ID already exists
        if (productRepository.findByName(product.getName()).isPresent()) {
            throw new ProductAlreadyExistsException("Produkt med namn " + product.getName() + " finns redan.");
        }
        // Save the new product
        Product saved = productRepository.save(product);
        log.info("addProduct – sparad produkt med productId={}", saved.getId());
        return saved;
    }

    /**
     * Deletes a product by its ID if it exists. Logs a warning if not found.
     *
     * @param productId the fullProductId of the product to delete
     */
    public void deleteProduct(Long productId) {
        log.info("deleteProduct – försök radera produktId={}", productId);
        // Only delete if the product actually exists
        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);
            log.info("deleteProduct – produkt raderad produktId={}", productId);
        } else {
            log.warn("deleteProduct – ingen produkt att radera för produktId={}", productId);
        }
    }

    public Product getProductByName(String name) {
        log.info("Hämtar kategori med namn {}", name);
        return productRepository.findByName(name).orElseThrow(() -> {
            log.error("Fel uppstod vid hämtning av kategori med namn: {}", name);
            return new ProductNotFoundException("Product med namn: " + name + " existerar inte.");
        });

    }

    /**
     * Retrieves a product by its ID. Throws RuntimeException if not found.
     *
     * @param productId the fullProductId to find
     * @return the found Product entity
     * @throws RuntimeException if no product is found for the given ID
     */
    public Product getProductById(Long productId) {
        log.debug("getProductById – hämta produkt produktId={}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("getProductById – ingen produkt hittades för produktId={}", productId);
                    return new RuntimeException("Produkt med ID " + productId + " finns inte.");
                });
        log.debug("getProductById – hittade produkt={}", product);
        return product;
    }

    /**
     * Retrieves all products in the database.
     *
     * @return a list of all Product entities
     */
    public List<Product> getAllProducts() {
        log.debug("getAllProducts – hämta alla produkter");
        List<Product> list = productRepository.findAll();
        log.debug("getAllProducts – antal produkter={}", list.size());
        return list;
    }

    public List<Product> getProductsByCategory(String categoryName) {
        return productRepository.findByCategoryName(categoryName);
    }

    public Product updateProduct(Product product) {

        Product updatedProduct = productRepository.save(product);
        return updatedProduct;
    }
}

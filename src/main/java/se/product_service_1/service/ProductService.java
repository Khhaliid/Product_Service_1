package se.product_service_1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.product_service_1.dto.ProductSearchRequest;
import se.product_service_1.exception.CategoryNotFoundException;
import se.product_service_1.exception.ProductAlreadyExistsException;
import se.product_service_1.exception.ProductNotFoundException;
import se.product_service_1.model.Product;
import se.product_service_1.model.Tag;
import se.product_service_1.repository.CategoryRepository;
import se.product_service_1.repository.ProductRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagService tagService;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, TagService tagService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.tagService = tagService;
    }

    public Product addProduct(Product product) {
        log.info("addProduct – försök spara produkt: productName={}, category={}",
                product.getName(), product.getCategory());

        if (productRepository.findByName(product.getName()).isPresent()) {
            throw new ProductAlreadyExistsException("Produkt med namn " + product.getName() + " finns redan.");
        }

        Product saved = productRepository.save(product);
        log.info("addProduct – sparad produkt med productId={}", saved.getId());
        return saved;
    }

    public Product addProductWithTags(Product product, List<String> tagNames) {
        log.info("addProductWithTags – försök spara produkt med taggar: productName={}, tags={}",
                product.getName(), tagNames);

        if (productRepository.findByName(product.getName()).isPresent()) {
            throw new ProductAlreadyExistsException("Produkt med namn " + product.getName() + " finns redan.");
        }

        // Hantera taggar
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> tags = tagService.getOrCreateTags(tagNames);
            product.setTags(tags);
        }

        Product saved = productRepository.save(product);
        log.info("addProductWithTags – sparad produkt med productId={} och {} taggar",
                saved.getId(), saved.getTags().size());
        return saved;
    }

    public void deleteProduct(Long productId) {
        log.info("deleteProduct – försök radera produktId={}", productId);
        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);
            log.info("deleteProduct – produkt raderad produktId={}", productId);
        } else {
            log.warn("deleteProduct – ingen produkt att radera för produktId={}", productId);
        }
    }

    public Product getProductByName(String name) {
        log.info("getProductByName – hämtar produkt med namn {}", name);
        return productRepository.findByName(name).orElseThrow(() -> {
            log.error("getProductByName – fel uppstod vid hämtning av produkt med namn: {}", name);
            return new ProductNotFoundException("Product med namn: " + name + " existerar inte.");
        });
    }

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

    public List<Product> getAllProducts() {
        log.debug("getAllProducts – hämta alla produkter");
        List<Product> list = productRepository.findAllWithTags();
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

    // === NYA SÖKFUNKTIONER FÖR TAGGAR ===

    public List<Product> searchProductsByTags(List<String> tagNames) {
        log.info("searchProductsByTags – söker produkter med taggar: {}", tagNames);
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }
        return productRepository.findByTagNames(tagNames);
    }

    public List<Product> searchProductsByAllTags(List<String> tagNames) {
        log.info("searchProductsByAllTags – söker produkter som har ALLA taggar: {}", tagNames);
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }
        return productRepository.findByAllTagNames(tagNames, tagNames.size());
    }

    public List<Product> searchProductsByTagPattern(String tagPattern) {
        log.info("searchProductsByTagPattern – söker produkter med tagg-mönster: {}", tagPattern);
        return productRepository.findByTagNameContaining(tagPattern);
    }

    public List<Product> searchProducts(ProductSearchRequest searchRequest) {
        log.info("searchProducts – avancerad sökning: {}", searchRequest);

        // Om både taggar och kategori är specificerade
        if (searchRequest.getTagNames() != null && !searchRequest.getTagNames().isEmpty()
                && searchRequest.getCategoryName() != null) {
            return productRepository.findByCategoryAndTagNames(
                    searchRequest.getCategoryName(),
                    searchRequest.getTagNames()
            );
        }

        // Om endast taggar är specificerade
        if (searchRequest.getTagNames() != null && !searchRequest.getTagNames().isEmpty()) {
            if (searchRequest.isRequireAllTags()) {
                return searchProductsByAllTags(searchRequest.getTagNames());
            } else {
                return searchProductsByTags(searchRequest.getTagNames());
            }
        }

        // Om sökterm för taggar är specificerad
        if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().trim().isEmpty()) {
            return searchProductsByTagPattern(searchRequest.getSearchTerm());
        }

        // Om endast kategori är specificerad
        if (searchRequest.getCategoryName() != null) {
            return getProductsByCategory(searchRequest.getCategoryName());
        }

        // Ingen specifik sökning - returnera alla produkter
        return getAllProducts();
    }

    public Product addTagsToProduct(Long productId, List<String> tagNames) {
        log.info("addTagsToProduct – lägger till taggar {} till produkt {}", tagNames, productId);

        Product product = getProductById(productId);
        Set<Tag> newTags = tagService.getOrCreateTags(tagNames);

        // Lägg till nya taggar till befintliga
        product.getTags().addAll(newTags);

        return productRepository.save(product);
    }

    public Product removeTagsFromProduct(Long productId, List<String> tagNames) {
        log.info("removeTagsFromProduct – tar bort taggar {} från produkt {}", tagNames, productId);

        Product product = getProductById(productId);

        // Ta bort taggar baserat på namn
        product.getTags().removeIf(tag -> tagNames.contains(tag.getName()));

        return productRepository.save(product);
    }
}
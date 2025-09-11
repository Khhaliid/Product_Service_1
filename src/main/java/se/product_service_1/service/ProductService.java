package se.product_service_1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.product_service_1.dto.InventoryManagementRequest;
import se.product_service_1.dto.InventoryManagementRequest.InventoryChange;
import se.product_service_1.dto.ProductSearchRequest;
import se.product_service_1.exception.NotEnoughStockException;
import se.product_service_1.exception.ProductAlreadyExistsException;
import se.product_service_1.exception.ProductNotFoundException;
import se.product_service_1.model.Product;
import se.product_service_1.model.ProductTag;
import se.product_service_1.model.Tag;
import se.product_service_1.repository.CategoryRepository;
import se.product_service_1.repository.ProductRepository;
import se.product_service_1.repository.ProductTagRepository;
import se.product_service_1.repository.TagRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagService tagService;
    private final ProductTagRepository productTagRepository;
    private final TagRepository tagRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          TagService tagService, ProductTagRepository productTagRepository, TagRepository tagRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.tagService = tagService;
        this.productTagRepository = productTagRepository;
        this.tagRepository = tagRepository;
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

    @Transactional
    public Product addProductWithTags(Product product, List<String> tagNames) {
        log.info("addProductWithTags – försök spara produkt med taggar: productName={}, tags={}",
                product.getName(), tagNames);

        if (productRepository.findByName(product.getName()).isPresent()) {
            throw new ProductAlreadyExistsException("Produkt med namn " + product.getName() + " finns redan.");
        }

        // Spara produkten först
        Product saved = productRepository.save(product);

        // Hantera taggar
        if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> tags = tagService.getOrCreateTags(tagNames);

            // Skapa ProductTag-kopplingar
            for (Tag tag : tags) {
                ProductTag productTag = ProductTag.builder()
                        .productId(saved.getId())
                        .tagId(tag.getId())
                        .build();
                productTagRepository.save(productTag);
            }
        }

        log.info("addProductWithTags – sparad produkt med productId={} och {} taggar",
                saved.getId(), tagNames != null ? tagNames.size() : 0);
        return saved;
    }

    @Transactional
    public void deleteProduct(Long productId) {
        log.info("deleteProduct – försök radera produktId={}", productId);
        if (productRepository.existsById(productId)) {
            // Ta bort alla ProductTag-kopplingar först
            productTagRepository.deleteByProductId(productId);
            // Ta bort produkten
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
        List<Product> list = productRepository.findAll();
        log.debug("getAllProducts – antal produkter={}", list.size());
        return list;
    }

    public List<String> getTagNamesForProduct(Long productId) {
        List<Long> tagIds = productTagRepository.findTagIdsByProductId(productId);
        List<Tag> tags = tagRepository.findAllById(tagIds);
        return tags.stream().map(Tag::getName).collect(Collectors.toList());
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

        // Hitta tag-IDs baserat på namn
        List<Tag> tags = tagNames.stream()
                .map(name -> tagRepository.findByName(name))
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toList());

        if (tags.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> tagIds = tags.stream().map(Tag::getId).collect(Collectors.toList());
        List<Long> productIds = productTagRepository.findProductIdsByTagIds(tagIds);

        return productRepository.findAllById(productIds);
    }

    public List<Product> searchProductsByAllTags(List<String> tagNames) {
        log.info("searchProductsByAllTags – söker produkter som har ALLA taggar: {}", tagNames);
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        // Denna implementation kräver mer komplex logik - returnera tom för nu
        return new ArrayList<>();
    }

    public List<Product> searchProductsByTagPattern(String tagPattern) {
        log.info("searchProductsByTagPattern – söker produkter med tagg-mönster: {}", tagPattern);
        List<Tag> tags = tagRepository.findByNameContainingIgnoreCase(tagPattern);
        List<Long> tagIds = tags.stream().map(Tag::getId).collect(Collectors.toList());

        if (tagIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> productIds = productTagRepository.findProductIdsByTagIds(tagIds);
        return productRepository.findAllById(productIds);
    }

    public List<Product> searchProducts(ProductSearchRequest searchRequest) {
        log.info("searchProducts – avancerad sökning: {}", searchRequest);

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

    @Transactional
    public Product addTagsToProduct(Long productId, List<String> tagNames) {
        log.info("addTagsToProduct – lägger till taggar {} till produkt {}", tagNames, productId);

        Product product = getProductById(productId);
        Set<Tag> newTags = tagService.getOrCreateTags(tagNames);

        // Skapa nya ProductTag-kopplingar
        for (Tag tag : newTags) {
            // Kontrollera om kopplingen redan finns
            List<ProductTag> existing = productTagRepository.findByProductId(productId);
            boolean alreadyExists = existing.stream()
                    .anyMatch(pt -> pt.getTagId().equals(tag.getId()));

            if (!alreadyExists) {
                ProductTag productTag = ProductTag.builder()
                        .productId(productId)
                        .tagId(tag.getId())
                        .build();
                productTagRepository.save(productTag);
            }
        }

        return product;
    }

    @Transactional
    public Product removeTagsFromProduct(Long productId, List<String> tagNames) {
        log.info("removeTagsFromProduct – tar bort taggar {} från produkt {}", tagNames, productId);

        Product product = getProductById(productId);

        // Hitta tag-IDs baserat på namn
        for (String tagName : tagNames) {
            tagRepository.findByName(tagName).ifPresent(tag -> {
                productTagRepository.deleteByProductIdAndTagId(productId, tag.getId());
            });
        }

        return product;
    }

    @Transactional
    public List<Product> updateInventoryChange(InventoryManagementRequest inventoryManagementRequest) {
        List<InventoryChange> inventoryChanges = inventoryManagementRequest.getInventoryChange();
        List<Product> productList = new ArrayList<>(inventoryChanges.size());
        Product product;
        for (InventoryChange change : inventoryChanges) {
            product = getProductById(change.getProductId());
            int newStockQuantity = product.getStockQuantity() + change.getInventoryChange();
            if (newStockQuantity < 0) {
                throw new NotEnoughStockException("Not enough stock of " + product.getName() + ". Stock quantity: " + product.getStockQuantity());
            }
            product.setStockQuantity(newStockQuantity);
            productRepository.save(product);
            productList.add(product);
        }
        return productList;
    }
}
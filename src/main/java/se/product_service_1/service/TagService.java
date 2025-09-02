package se.product_service_1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.product_service_1.exception.ResourceNotFoundException;
import se.product_service_1.model.Tag;
import se.product_service_1.repository.TagRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

@Service
public class TagService {

    private static final Logger log = LoggerFactory.getLogger(TagService.class);
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getAllTags() {
        log.debug("getAllTags - hämta alla taggar");
        List<Tag> tags = tagRepository.findAll();
        log.debug("getAllTags - antal taggar={}", tags.size());
        return tags;
    }

    public Tag getTagByName(String name) {
        log.info("getTagByName - hämta tagg med namn: {}", name);
        return tagRepository.findByName(name)
                .orElseThrow(() -> {
                    log.warn("getTagByName - ingen tagg hittades för namn: {}", name);
                    return new ResourceNotFoundException("Tagg med namn '" + name + "' finns inte");
                });
    }

    public Tag createTag(String name, String description) {
        log.info("createTag - skapa ny tagg: {}", name);

        if (tagRepository.existsByName(name)) {
            log.warn("createTag - tagg med namn '{}' finns redan", name);
            throw new IllegalArgumentException("Tagg med namn '" + name + "' finns redan");
        }

        Tag tag = Tag.builder()
                .name(name)
                .description(description)
                .build();

        Tag savedTag = tagRepository.save(tag);
        log.info("createTag - tagg skapad med ID: {}", savedTag.getId());
        return savedTag;
    }

    public void deleteTag(Long tagId) {
        log.info("deleteTag - radera tagg med ID: {}", tagId);
        if (!tagRepository.existsById(tagId)) {
            throw new ResourceNotFoundException("Tagg med ID " + tagId + " finns inte");
        }
        tagRepository.deleteById(tagId);
        log.info("deleteTag - tagg raderad: {}", tagId);
    }

    public List<Tag> searchTagsByName(String searchTerm) {
        log.info("searchTagsByName - söka taggar med term: {}", searchTerm);
        return tagRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    public Set<Tag> getOrCreateTags(List<String> tagNames) {
        log.info("getOrCreateTags - hantera taggar: {}", tagNames);
        Set<Tag> tags = new HashSet<>();

        for (String tagName : tagNames) {
            Optional<Tag> existingTag = tagRepository.findByName(tagName);
            if (existingTag.isPresent()) {
                tags.add(existingTag.get());
                log.debug("getOrCreateTags - använder befintlig tagg: {}", tagName);
            } else {
                Tag newTag = Tag.builder()
                        .name(tagName)
                        .description("Auto-skapad tagg")
                        .build();
                Tag savedTag = tagRepository.save(newTag);
                tags.add(savedTag);
                log.info("getOrCreateTags - skapade ny tagg: {}", tagName);
            }
        }

        return tags;
    }
}
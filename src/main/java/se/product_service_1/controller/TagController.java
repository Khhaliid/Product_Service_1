package se.product_service_1.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.product_service_1.dto.TagRequest;
import se.product_service_1.dto.TagResponse;
import se.product_service_1.model.Tag;
import se.product_service_1.repository.ProductTagRepository;
import se.product_service_1.service.TagService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tag")
@AllArgsConstructor
public class TagController {

    private final TagService tagService;
    private final ProductTagRepository productTagRepository;

    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        List<TagResponse> responses = tags.stream()
                .map(this::buildTagResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<TagResponse> getTagByName(@PathVariable String name) {
        Tag tag = tagService.getTagByName(name);
        TagResponse response = buildTagResponse(tag);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TagResponse> createTag(@RequestBody TagRequest tagRequest) {
        Tag tag = tagService.createTag(tagRequest.getName(), tagRequest.getDescription());
        TagResponse response = buildTagResponse(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok("Tag deleted successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<List<TagResponse>> searchTags(@RequestParam String searchTerm) {
        List<Tag> tags = tagService.searchTagsByName(searchTerm);
        List<TagResponse> responses = tags.stream()
                .map(this::buildTagResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private TagResponse buildTagResponse(Tag tag) {
        // Räkna produkter som använder denna tagg via ProductTagRepository
        int productCount = productTagRepository.findByTagId(tag.getId()).size();

        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .productCount(productCount)
                .build();
    }
}
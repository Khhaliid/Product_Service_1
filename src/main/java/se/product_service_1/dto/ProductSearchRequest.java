package se.product_service_1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductSearchRequest {
    private List<String> tagNames;
    private String categoryName;
    private boolean requireAllTags; // true = måste ha alla taggar, false = minst en tagg
    private String searchTerm; // för att söka i tagg-namn
}
package se.product_service_1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryManagementRequest {
    private String productName;
    private Integer inventoryChange;
}

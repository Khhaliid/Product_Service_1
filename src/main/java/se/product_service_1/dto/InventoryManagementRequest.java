package se.product_service_1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryManagementRequest {
    List<InventoryChange> inventoryChanges;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InventoryChange {
        private Long productId;
        private Integer inventoryChange;
    }
}

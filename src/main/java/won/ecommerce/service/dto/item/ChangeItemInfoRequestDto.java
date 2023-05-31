package won.ecommerce.service.dto.item;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import won.ecommerce.entity.Category;

@Data
public class ChangeItemInfoRequestDto {
    @NotNull(message = "상품 ID(필수)")
    Long itemId;
    Integer changePrice;
    Integer changeStockQuantity;
    Long changeCategoryId;
}

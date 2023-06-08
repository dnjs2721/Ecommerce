package won.ecommerce.controller.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuyerCancelOrderItemRequestDto {
    @NotNull(message = "주문상품 ID")
    Long orderItemId;
}

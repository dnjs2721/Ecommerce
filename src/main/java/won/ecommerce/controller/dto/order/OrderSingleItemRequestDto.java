package won.ecommerce.controller.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderSingleItemRequestDto {
    @NotNull(message = "상품 Id")
    Long itemId;
    @NotNull(message = "상품 수량")
    Integer itemCount;
}

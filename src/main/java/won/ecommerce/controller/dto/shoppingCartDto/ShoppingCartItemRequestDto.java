package won.ecommerce.controller.dto.shoppingCartDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShoppingCartItemRequestDto {
    @NotNull(message = "상품 Id(필수)")
    Long itemId;

    Integer itemCount;
}

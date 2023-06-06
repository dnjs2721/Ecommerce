package won.ecommerce.controller.dto.shoppingCartDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeShoppingCartItemCountRequestDto {
    @NotNull(message = "쇼핑 카트 상품 Id")
    Long shoppingCartItemId;

    @NotNull(message = "변경할 수량")
    Integer changCount;
}

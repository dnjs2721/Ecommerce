package won.ecommerce.controller.dto.userDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class cancelExchangeRefundRequestDto {
    @NotNull(message = "주문상품 Id")
    Long orderItemId;
}

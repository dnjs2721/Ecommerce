package won.ecommerce.controller.dto.userDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import won.ecommerce.entity.ExchangeRefundStatus;

@Data
public class CreateExchangeRefundLogRequestDto {
    @NotNull(message = "주문상품 Id")
    Long orderItemId;
    @NotNull(message = "교환/환불 종류")
    ExchangeRefundStatus status;
    @NotBlank(message = "교환/환불 이유")
    String reason;
}

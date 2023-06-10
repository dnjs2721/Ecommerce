package won.ecommerce.controller.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import won.ecommerce.entity.OrderItemStatus;

@Data
public class ChangeOrderStatusRequestDto {
    @NotNull(message = "주문상품 ID")
    Long orderItemId;
    OrderItemStatus orderItemStatus;
    String comment;
}

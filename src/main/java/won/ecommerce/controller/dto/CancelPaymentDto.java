package won.ecommerce.controller.dto;

import lombok.Data;

@Data
public class CancelPaymentDto {
    Long cancelPaymentUserId;
    Long orderItemId;
}

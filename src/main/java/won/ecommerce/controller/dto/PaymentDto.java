package won.ecommerce.controller.dto;

import lombok.Data;

@Data
public class PaymentDto {
    Long paymentUserId;
    Long orderId;
}

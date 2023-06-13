package won.ecommerce.controller.dto.paymentDto;

import lombok.Data;

@Data
public class CancelPaymentDto {
    Long cancelPaymentUserId;
    Long orderItemId;
}

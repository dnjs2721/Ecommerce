package won.ecommerce.controller.dto.paymentDto;

import lombok.Data;

@Data
public class CancelPaymentForSellerDto {
    Long cancelPaymentSellerId;
    Long orderItemIdForSeller;
}

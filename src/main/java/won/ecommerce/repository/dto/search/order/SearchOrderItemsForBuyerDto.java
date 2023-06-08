package won.ecommerce.repository.dto.search.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import won.ecommerce.entity.OrderStatus;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchOrderItemsForBuyerDto {
    Long orderItemId;
    Long itemId;
    String sellerName;
    String itemName;
    int price;
    int count;
    int totalPrice;
    OrderStatus orderStatus;
    String cancelReason;

    @QueryProjection
    public SearchOrderItemsForBuyerDto(Long orderItemId, Long itemId, String sellerName, String itemName, int price, int count, int totalPrice, OrderStatus orderStatus, String cancelReason) {
        this.orderItemId = orderItemId;
        this.itemId = itemId;
        this.sellerName = sellerName;
        this.itemName = itemName;
        this.price = price;
        this.count = count;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.cancelReason = cancelReason;
    }
}

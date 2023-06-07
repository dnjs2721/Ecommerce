package won.ecommerce.repository.dto.search.order;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class SearchOrderItemsForBuyerDto {
    Long orderItemId;
    Long itemId;
    String sellerName;
    String itemName;
    int price;
    int count;
    int totalPrice;

    @QueryProjection
    public SearchOrderItemsForBuyerDto(Long orderItemId, Long itemId, String sellerName, String itemName, int price, int count, int totalPrice) {
        this.orderItemId = orderItemId;
        this.itemId = itemId;
        this.sellerName = sellerName;
        this.itemName = itemName;
        this.price = price;
        this.count = count;
        this.totalPrice = totalPrice;
    }
}

package won.ecommerce.repository.dto.search.categoryItem;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class CategoryItemDto {
    Long sellerId;
    String sellerName;
    String sellerEmail;
    String categoryName;
    Long itemId;
    String itemName;

    @QueryProjection

    public CategoryItemDto(Long sellerId, String sellerName, String sellerEmail, String categoryName, Long itemId, String itemName) {
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.sellerEmail = sellerEmail;
        this.categoryName = categoryName;
        this.itemId = itemId;
        this.itemName = itemName;
    }
}

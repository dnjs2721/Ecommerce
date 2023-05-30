package won.ecommerce.repository.dto.search;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class SubCategoryItemDto {
    Long sellerId;
    String categoryName;
    Long itemId;
    String itemName;

    @QueryProjection
    public SubCategoryItemDto(Long sellerId, String categoryName, Long itemId, String itemName) {
        this.sellerId = sellerId;
        this.categoryName = categoryName;
        this.itemId = itemId;
        this.itemName = itemName;
    }
}

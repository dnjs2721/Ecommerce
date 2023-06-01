package won.ecommerce.repository.dto.search.item;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class ItemSearchFromCommonCondition {
    private String sellerNickName;
    private String itemName;
    private Integer priceGoe;
    private Integer priceLoe;
    private Long categoryId;
}

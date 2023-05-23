package won.ecommerce.service.dto;

import lombok.Data;
import won.ecommerce.entity.Category;

@Data
public class ItemCreateRequestDto {
    String name;
    int price;
    int stockQuantity;
    Long categoryId;
}

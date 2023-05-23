package won.ecommerce.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemCreateRequestDto {
    @NotBlank(message = "상품 이름(필수)")
    String name;
    @NotNull(message = "상품 가격(필수)")
    int price;
    @NotNull(message = "상품 재고(필수)")
    int stockQuantity;
    @NotNull(message = "카테고리 id(필수)")
    Long categoryId;
}

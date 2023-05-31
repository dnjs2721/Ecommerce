package won.ecommerce.service.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateRequestDto {
    @NotBlank(message = "카테고리 이름(필수)")
    String name;
    Long parentId;
}

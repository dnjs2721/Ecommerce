package won.ecommerce.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateRequestDto {
    @NotBlank(message = "카테고리 이름(필수)")
    String name;
    Long parentId;
}

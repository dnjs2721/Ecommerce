package won.ecommerce.controller.dto.adminDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatchChangeItemCategoryRequestDto {
    @NotNull(message = "기존 카테고리 Id (필수)")
    Long categoryId;

    @NotNull(message = "변경할 카테고리 Id (필수)")
    Long changeCategoryId;
}

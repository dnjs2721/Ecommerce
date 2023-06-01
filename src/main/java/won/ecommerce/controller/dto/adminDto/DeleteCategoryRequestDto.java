package won.ecommerce.controller.dto.adminDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteCategoryRequestDto {
    @NotNull(message = "카테고리 Id (필수)")
    Long categoryId;
}

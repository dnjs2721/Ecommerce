package won.ecommerce.controller.dto.itemDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteItemRequestDto {
    @NotNull(message = "아이템 ID(필수)")
    Long itemId;
}

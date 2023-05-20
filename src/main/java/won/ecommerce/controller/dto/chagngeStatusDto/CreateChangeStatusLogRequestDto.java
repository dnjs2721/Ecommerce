package won.ecommerce.controller.dto.chagngeStatusDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateChangeStatusLogRequestDto {
    @NotNull(message = "사용자 ID(필수)")
    Long id;
}

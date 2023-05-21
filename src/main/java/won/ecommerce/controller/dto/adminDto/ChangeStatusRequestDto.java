package won.ecommerce.controller.dto.adminDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeStatusRequestDto {
    @NotNull(message = "ADMIN ID(필수)")
    Long adminId;

    @NotBlank(message = "응답(필수)")
    String stat;
}

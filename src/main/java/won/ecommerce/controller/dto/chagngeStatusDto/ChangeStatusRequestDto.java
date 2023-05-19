package won.ecommerce.controller.dto.chagngeStatusDto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeStatusRequestDto {
    @NotNull(message = "로그 ID(필수)")
    Long logId;

    @NotNull(message = "ADMIN ID(필수)")
    Long adminId;

    @NotBlank(message = "응답(필수)")
    String stat;
}

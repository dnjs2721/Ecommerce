package won.ecommerce.controller.dto.adminDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import won.ecommerce.entity.LogStatus;

@Data
public class ChangeStatusRequestDto {
    @NotNull(message = "ADMIN ID(필수)")
    Long adminId;
    @NotNull(message = "응답(필수)")
    LogStatus stat;
    String cancelReason;
}

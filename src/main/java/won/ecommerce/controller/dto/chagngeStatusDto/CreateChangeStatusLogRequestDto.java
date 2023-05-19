package won.ecommerce.controller.dto.chagngeStatusDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateChangeStatusLogRequestDto {
    @NotBlank(message = "이메일(필수)")
    @Email
    String email;
}

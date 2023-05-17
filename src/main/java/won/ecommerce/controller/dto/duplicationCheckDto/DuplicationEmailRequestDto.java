package won.ecommerce.controller.dto.duplicationCheckDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DuplicationEmailRequestDto {
    @Email
    @NotBlank(message = "이메일(필수)")
    private String email;
}

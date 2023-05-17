package won.ecommerce.controller.dto.mailDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidateEmailRequestDto {
    @Email
    @NotBlank(message = "이메일(필수)")
    private String email;

    @NotBlank(message = "인증코드(필수)")
    private String authCode;
}

package won.ecommerce.controller.dto.userDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    @Email
    @NotBlank(message = "이메일(필수)")
    private String email;

    @NotBlank(message = "비밀번호(필수)")
    private String password;
}

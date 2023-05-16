package won.ecommerce.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginRequestDto {
    @Email
    @NotEmpty(message = "이메일(필수)")
    private String email;

    @NotEmpty(message = "비밀번호(필수)")
    private String password;
}

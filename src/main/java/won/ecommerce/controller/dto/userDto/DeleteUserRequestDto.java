package won.ecommerce.controller.dto.userDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteUserRequestDto {
    @NotBlank(message = "이메일(필수)")
    @Email
    String email;
    @NotBlank(message = "패스워드(필수)")
    String password;
}

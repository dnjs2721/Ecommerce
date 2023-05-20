package won.ecommerce.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeUserInfoRequestDto {
    @NotBlank(message = "이메일(필수)")
    @Email
    String email;
    @NotBlank(message = "패스워드(필수)")
    String password;
    String nickname;
    String region;
    String city;
    String street;
    String detail;
    String zipcode;
}

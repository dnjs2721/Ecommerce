package won.ecommerce.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidateMessageRequestDto {
    @NotBlank(message = "전화번호(필수)")
    private String pNum;

    @NotBlank(message = "인증코드(필수)")
    private String authCode;
}

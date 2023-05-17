package won.ecommerce.controller.dto.messageDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidateMessageRequestDto {
    @NotBlank(message = "전화번호(필수)")
    private String pNum;

    @NotBlank(message = "인증코드(필수)")
    private String authCode;
}

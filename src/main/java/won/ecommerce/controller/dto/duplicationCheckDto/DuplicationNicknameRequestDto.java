package won.ecommerce.controller.dto.duplicationCheckDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DuplicationNicknameRequestDto {
    @NotBlank(message = "닉네임(필수)")
    private String nickname;
}

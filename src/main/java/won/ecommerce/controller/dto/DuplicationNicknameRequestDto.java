package won.ecommerce.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class DuplicationNicknameRequestDto {
    @NotEmpty(message = "닉네임(필수)")
    private String nickname;
}

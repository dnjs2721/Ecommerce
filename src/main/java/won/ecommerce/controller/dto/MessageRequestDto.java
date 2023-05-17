package won.ecommerce.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequestDto {
    @NotBlank(message = "전화번호(필수)")
    String pNum;
}

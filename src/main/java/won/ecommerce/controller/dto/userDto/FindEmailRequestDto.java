package won.ecommerce.controller.dto.userDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FindEmailRequestDto {
    @NotBlank(message = "이름(필수)")
    private String name;

    @NotBlank(message = "전화번호(필수)")
    private String pNum;
}

package won.ecommerce.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class FindEmailRequestDto {
    @NotEmpty(message = "이름(필수)")
    private String name;

    @NotEmpty(message = "전화번호(필수)")
    private String pNum;
}

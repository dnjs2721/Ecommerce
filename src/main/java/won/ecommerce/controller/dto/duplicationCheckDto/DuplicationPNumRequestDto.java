package won.ecommerce.controller.dto.duplicationCheckDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DuplicationPNumRequestDto {
    @NotBlank(message = "전화번호(필수)")
    private String pNum;
}

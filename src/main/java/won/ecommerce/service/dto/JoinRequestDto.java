package won.ecommerce.service.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Data
public class JoinRequestDto {
    @NotEmpty(message = "사용자 이름(필수)")
    private String name;
    @NotEmpty(message = "이메일(필수)")
    @Email
    private String email;
    @NotEmpty(message = "이메일(필수)")
    private String password;
    @NotEmpty(message = "전화번호(필수)")
    private String pNum;
    @NotEmpty(message = "생알(필수)")
    private String birth;
    @NotEmpty(message = "주소(필수)")
    private String region;
    @NotEmpty(message = "주소(필수)")
    private String city;
    @NotEmpty(message = "주소(필수)")
    private String street;
    @NotEmpty(message = "주소(필수)")
    private String zipcode;
}

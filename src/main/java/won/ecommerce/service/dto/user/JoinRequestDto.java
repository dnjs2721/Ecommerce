package won.ecommerce.service.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinRequestDto {
    @NotBlank(message = "사용자 이름(필수)")
    private String name;
    @NotBlank(message = "닉네임(필수)")
    private String nickname;
    @NotBlank(message = "이메일(필수)")
    @Email
    private String email;
    @NotBlank(message = "이메일(필수)")
    private String password;
    @NotBlank(message = "전화번호(필수)")
    private String pNum;
    @NotBlank(message = "생알(필수)")
    private String birth;
    @NotBlank(message = "주소(필수)")
    private String region;
    @NotBlank(message = "주소(필수)")
    private String city;
    @NotBlank(message = "주소(필수)")
    private String street;
    @NotBlank(message = "주소(필수)")
    private String detail;
    @NotBlank(message = "주소(필수)")
    private String zipcode;
}

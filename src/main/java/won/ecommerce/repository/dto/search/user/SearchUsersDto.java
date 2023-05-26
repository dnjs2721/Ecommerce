package won.ecommerce.repository.dto.search.user;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import won.ecommerce.entity.Address;
import won.ecommerce.entity.UserStatus;

import java.time.LocalDateTime;

@Data
public class SearchUsersDto {
    @Id
    @GeneratedValue
    private Long id;
    private UserStatus status;
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String pNum;
    private String birth;
    private Address address;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModifiedDate;

    @QueryProjection
    public SearchUsersDto(Long id, UserStatus status, String email, String password, String name, String nickname, String pNum, String birth, Address address, LocalDateTime createdDate, LocalDateTime lastModifiedDate) {
        this.id = id;
        this.status = status;
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.pNum = pNum;
        this.birth = birth;
        this.address = address;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }
}

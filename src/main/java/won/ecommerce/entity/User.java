package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String nickname;
    private String email;
    private String password;
    private String pNum;
    private String birth;
    @Embedded
    private Address address;
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Builder
    public User(String name, String nickname, String email, String password, String pNum, String birth, Address address, UserStatus status) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.pNum = pNum;
        this.birth = birth;
        this.address = address;
        this.status = status;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}

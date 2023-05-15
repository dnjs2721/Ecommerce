package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private String pNum;
    private String birth;
    @Embedded
    private Address address;
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Builder
    public User(String name, String email, String password, String pNum, String birth, Address address, UserStatus status) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.pNum = pNum;
        this.birth = birth;
        this.address = address;
        this.status = status;
    }
}

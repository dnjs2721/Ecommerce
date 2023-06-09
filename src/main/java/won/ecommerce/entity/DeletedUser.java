package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeletedUser extends BaseTimeEntity {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private String userName;
    private String userNickname;
    private String userEmail;
    private String userPassword;
    private String userPNum;
    private String userBirth;
    @Embedded
    private Address userAddress;
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Builder
    public DeletedUser(Long userId, String userName, String userNickname, String userEmail, String userPassword, String userPNum, String userBirth, Address userAddress, UserStatus userStatus) {
        this.userId = userId;
        this.userName = userName;
        this.userNickname = userNickname;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userPNum = userPNum;
        this.userBirth = userBirth;
        this.userAddress = userAddress;
        this.userStatus = userStatus;
    }
}

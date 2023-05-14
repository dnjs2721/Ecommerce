package won.ecommerce.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.Address;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;

    @Test
    public void joinTest() throws Exception{
        User user = User.builder()
                .name("최상원")
                .email("skadu66@gmail.com")
                .password("Ska@1012")
                .pNum("01035582721")
                .birth("981012")
                .address(new Address("대구광역시", "북구", "대현로 17길 5", "41525"))
                .status(UserStatus.COMMON)
                .build();

        User user1 = User.builder()
                .name("최상원")
                .email("skadu66@gmail.com")
                .password("Ska@1012")
                .pNum("01035582721")
                .birth("981012")
                .address(new Address("대구광역시", "북구", "대현로 17길 5", "41525"))
                .status(UserStatus.COMMON)
                .build();

        userService.join(user);

        Assertions.assertThrows(IllegalStateException.class, () -> {
           userService.join(user1);
        });
    }
}
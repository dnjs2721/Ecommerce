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
                .name("test")
                .email("###@##.###")
                .password("test")
                .pNum("test")
                .birth("test")
                .address(new Address("test", "test", "test", "test", "test"))
                .build();

        User user1 = User.builder()
                .name("test")
                .email("###@##.###")
                .password("test")
                .pNum("test")
                .birth("test")
                .address(new Address("test", "test", "test", "test", "test"))
                .build();

        userService.join(user);

        Assertions.assertThrows(IllegalStateException.class, () -> {
           userService.join(user1);
        });
    }
}
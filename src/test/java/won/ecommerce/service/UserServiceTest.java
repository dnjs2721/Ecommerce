package won.ecommerce.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import won.ecommerce.entity.Address;
import won.ecommerce.entity.User;
import won.ecommerce.repository.user.UserRepository;

@SpringBootTest
class UserServiceTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
}
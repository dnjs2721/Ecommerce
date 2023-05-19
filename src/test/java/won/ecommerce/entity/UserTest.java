package won.ecommerce.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class UserTest {
    @Autowired
    EntityManager em;

    @Test
    void userTest(){

        User user = User.builder()
                .name("test")
                .email("###@##.###")
                .password("test")
                .pNum("test")
                .birth("test")
                .address(new Address("test", "test", "test", "test", "test"))
                .status(UserStatus.COMMON)
                .build();

        em.persist(user);
    }
}
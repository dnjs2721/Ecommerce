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
                .name("최상원")
                .email("skadu66@gmail.com")
                .password("Ska@1012")
                .pNum("01035582721")
                .birth("981012")
                .address(new Address("대구광역시", "북구", "대현로 17길 5", "41525"))
                .status(UserStatus.COMMON)
                .build();

        em.persist(user);
    }
}
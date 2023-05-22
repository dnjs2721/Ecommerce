package won.ecommerce.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class ItemTest {
    @Autowired
    EntityManager em;

    @Test
    public void addItem() throws Exception {
        User user = em.find(User.class, 1L);
        
        Item keyChronK2 = Item.builder()
                .name("키크론 K2")
                .price(80000)
                .stockQuantity(20)
                .build();

        Item keyChronK10 = Item.builder()
                .name("키크론 K10")
                .price(100000)
                .stockQuantity(30)
                .build();

        keyChronK2.setSeller(user);
        keyChronK10.setSeller(user);

        Category mechanicalKeyboard = em.find(Category.class, 3L);// 카테고리 기계식 키보드
        Category lowNoiseKeyboard = em.find(Category.class, 2L);
        keyChronK2.addCategory(mechanicalKeyboard);
        keyChronK10.addCategory(lowNoiseKeyboard);
        em.persist(keyChronK2);
        em.persist(keyChronK10);
        System.out.println("--------------------------------");
        System.out.println("keyChronK2 = " + keyChronK2.getCategory().getName());
        System.out.println("--------------------------------");
        System.out.println("--------------------------------");
        System.out.println("keyChronK10 = " + keyChronK10.getCategory().getName());
        System.out.println("--------------------------------");
        List<Item> items = mechanicalKeyboard.getItems();
        for (Item load : items) {
            System.out.println("--------------------------------");
            System.out.println("load = " + load.getName());
            System.out.println("--------------------------------");
        }
        System.out.println("--------------------------------");
        System.out.println("user.getSellItems() = " + user.getSellItems());
        System.out.println("--------------------------------");

    }
}
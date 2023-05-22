package won.ecommerce.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class CategoryTest {

    @Autowired
    EntityManager em;

    @Test
    public void createCategory() throws Exception {
        Category category1 = new Category("카테고리 1번");
        em.persist(category1);
        for (int i = 2; i < 7; i++) {
            Category category = new Category("카테고리" + i + "번");
            category.addParentCategory(category1);
            em.persist(category);
        }

        Category category = em.find(Category.class, category1.getId());
        List<Category> child = category.getChild();

        for (Category load : child) {
            System.out.println("load.getName() = " + load.getName());
        }

    }
}
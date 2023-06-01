package won.ecommerce.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.Category;
import won.ecommerce.repository.category.CategoryRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class CategoryServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    CategoryService categoryService;

    @Autowired
    CategoryRepository categoryRepository;

    @Test
    @Transactional
    void deleteCategory() {
        Category category1 = new Category("기타 1");
        Category category2 = new Category("기타 2");

        Optional<Category> parentOt = categoryRepository.findById(1L);
        Category parent = parentOt.get();

        category1.addParentCategory(parent);
        category2.addParentCategory(parent);

        em.persist(category1);
        em.persist(category2);

        em.flush();
        em.clear();

        String s = categoryService.deleteCategory(1L);
        System.out.println("s = " + s);
    }
}
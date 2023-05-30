package won.ecommerce.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryRepositoryCustom {
    Optional<Category> findByName(String name);
}

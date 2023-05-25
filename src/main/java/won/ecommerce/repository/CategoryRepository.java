package won.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

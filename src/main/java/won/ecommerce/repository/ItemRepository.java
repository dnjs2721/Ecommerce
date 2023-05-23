package won.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
}

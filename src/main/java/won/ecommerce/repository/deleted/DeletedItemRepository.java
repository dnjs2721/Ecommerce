package won.ecommerce.repository.deleted;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.DeletedItem;
import won.ecommerce.entity.Item;
import won.ecommerce.entity.User;

import java.util.Optional;

public interface DeletedItemRepository extends JpaRepository<DeletedItem, Long>{

}

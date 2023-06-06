package won.ecommerce.repository.deleted.item;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.DeletedItem;

public interface DeletedItemRepository extends JpaRepository<DeletedItem, Long>, DeletedItemRepositoryCustom{

}

package won.ecommerce.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.Item;
import won.ecommerce.entity.User;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {
    Optional<Item> findBySellerAndName(User seller, String itemName);

}

package won.ecommerce.repository.shoppingCart;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.ShoppingCartItem;

import java.util.Optional;

public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, Long>, ShoppingCartItemRepositoryCustom {
    Optional<ShoppingCartItem> findByShoppingCartIdAndItemId(Long shoppingCartId, Long itemId);
}

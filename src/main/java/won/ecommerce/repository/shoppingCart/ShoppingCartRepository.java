package won.ecommerce.repository.shoppingCart;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.ShoppingCart;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
}

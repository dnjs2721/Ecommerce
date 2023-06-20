package won.ecommerce.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.OrderItem;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>{
}

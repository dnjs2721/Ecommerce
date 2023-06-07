package won.ecommerce.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.OrdersForBuyer;

public interface OrdersForBuyerRepository extends JpaRepository<OrdersForBuyer, Long>, OrderSearchRepository {
}

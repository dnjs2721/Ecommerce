package won.ecommerce.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.OrdersForSeller;

public interface OrdersForSellerRepository extends JpaRepository<OrdersForSeller, Long>, OrderSearchRepository {
}

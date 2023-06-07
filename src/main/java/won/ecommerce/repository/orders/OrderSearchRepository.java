package won.ecommerce.repository.orders;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import won.ecommerce.repository.dto.search.order.*;

import java.util.List;

public interface OrderSearchRepository {
    Page<SearchOrdersForBuyerDto> searchOrdersForBuyer(Long buyerId, OrderSearchCondition condition, Pageable pageable);

    Page<SearchOrdersForSellerDto> searchOrdersForSeller(Long sellerId, OrderSearchCondition condition, Pageable pageable);

    List<SearchOrderItemForSellerDto> searchOrderItemsForSeller(Long orderId);
    List<SearchOrderItemsForBuyerDto> searchOrderItemsForBuyer(Long orderId);
}

package won.ecommerce.repository.orders;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.ExchangeRefundLog;
import won.ecommerce.entity.ExchangeRefundStatus;
import won.ecommerce.entity.LogStatus;

import java.util.Optional;

public interface ExchangeRefundRepository extends JpaRepository<ExchangeRefundLog, Long> {
    Optional<ExchangeRefundLog> findByUserIdAndOrderItemIdAndLogStatus(Long userId, Long orderItemId, LogStatus logStatus);
}

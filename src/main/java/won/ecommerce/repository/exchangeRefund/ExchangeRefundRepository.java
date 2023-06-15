package won.ecommerce.repository.exchangeRefund;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.ExchangeRefundLog;
import won.ecommerce.entity.ExchangeRefundStatus;
import won.ecommerce.entity.LogStatus;

import java.util.Optional;

public interface ExchangeRefundRepository extends JpaRepository<ExchangeRefundLog, Long>, ExchangeRefundRepositoryCustom {
    Optional<ExchangeRefundLog> findByUserIdAndOrderItemIdAndLogStatus(Long userId, Long orderItemId, LogStatus logStatus);
}

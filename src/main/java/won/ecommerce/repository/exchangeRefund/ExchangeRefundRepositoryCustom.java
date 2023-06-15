package won.ecommerce.repository.exchangeRefund;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import won.ecommerce.repository.dto.search.exchangeRefundLog.ExchangeRefundLogSearchCondition;
import won.ecommerce.repository.dto.search.exchangeRefundLog.SearchExchangeRefundLogDto;

public interface ExchangeRefundRepositoryCustom {
    Page<SearchExchangeRefundLogDto> searchExchangeRefundLog(Long sellerId, ExchangeRefundLogSearchCondition condition, Pageable pageable);
}

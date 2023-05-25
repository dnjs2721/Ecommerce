package won.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import won.ecommerce.repository.dto.ItemSearchCondition;
import won.ecommerce.repository.dto.SearchItemDto;

public interface ItemRepositoryCustom {
    Page<SearchItemDto> searchItemPage(Long sellerId, ItemSearchCondition condition, Pageable pageable);
}

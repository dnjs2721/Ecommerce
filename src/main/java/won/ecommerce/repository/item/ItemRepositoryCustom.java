package won.ecommerce.repository.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import won.ecommerce.repository.dto.search.item.ItemSearchCondition;
import won.ecommerce.repository.dto.search.item.SearchItemDto;

public interface ItemRepositoryCustom {
    Page<SearchItemDto> searchItemPage(Long sellerId, ItemSearchCondition condition, Pageable pageable);
}

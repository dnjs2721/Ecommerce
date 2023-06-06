package won.ecommerce.repository.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import won.ecommerce.entity.Category;
import won.ecommerce.repository.dto.search.item.OrderCondition;
import won.ecommerce.repository.dto.search.item.ItemSearchCondition;
import won.ecommerce.repository.dto.search.item.ItemSearchFromCommonCondition;
import won.ecommerce.repository.dto.search.item.SearchItemDto;
import won.ecommerce.repository.dto.search.item.SearchItemFromCommonDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepositoryCustom {
    Page<SearchItemDto> searchItemPage(Long sellerId, ItemSearchCondition condition, Pageable pageable);
    Page<SearchItemFromCommonDto> searchItemPageFromCommon(ItemSearchFromCommonCondition condition, OrderCondition orderCondition, Pageable pageable);
    void batchUpdateItemCategory(List<Long> itemIds, Category category);
    void changePrice(Long itemId, int price);
}

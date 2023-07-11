package won.ecommerce.repository.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import won.ecommerce.entity.Category;
import won.ecommerce.entity.Item;
import won.ecommerce.entity.ShoppingCartItem;
import won.ecommerce.repository.dto.search.item.SortCondition;
import won.ecommerce.repository.dto.search.item.ItemSearchCondition;
import won.ecommerce.repository.dto.search.item.ItemSearchFromCommonCondition;
import won.ecommerce.repository.dto.search.item.SearchItemDto;
import won.ecommerce.repository.dto.search.item.SearchItemFromCommonDto;
import won.ecommerce.service.dto.item.ChangeItemInfoRequestDto;

import java.util.List;

public interface ItemRepositoryCustom {
    Page<SearchItemDto> searchItemPage(Long sellerId, ItemSearchCondition condition, Pageable pageable);
    Page<SearchItemFromCommonDto> searchItemPageFromCommon(ItemSearchFromCommonCondition condition, SortCondition sortCondition, Pageable pageable);
    void batchUpdateItemCategory(Long beforeCategoryId, Long changeCategoryId);
    void changeItemInfo(ChangeItemInfoRequestDto request);
    List<Item> findItemBySellerIdAndItemIds(Long sellerId, List<Long> itemIds);
    List<ShoppingCartItem> getShoppingCartItem(List<Long> itemIds);
}

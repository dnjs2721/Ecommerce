package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.*;
import won.ecommerce.repository.deleted.item.DeletedItemRepository;
import won.ecommerce.repository.dto.search.categoryItem.CategoryItemDto;
import won.ecommerce.repository.dto.search.item.OrderCondition;
import won.ecommerce.repository.dto.search.item.ItemSearchCondition;
import won.ecommerce.repository.dto.search.item.ItemSearchFromCommonCondition;
import won.ecommerce.repository.dto.search.item.SearchItemDto;
import won.ecommerce.repository.dto.search.item.SearchItemFromCommonDto;
import won.ecommerce.repository.item.ItemRepository;
import won.ecommerce.service.dto.item.ChangeItemInfoRequestDto;
import won.ecommerce.service.dto.item.ItemCreateRequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final DeletedItemRepository deletedItemRepository;
    private final CategoryService categoryService;
    private final ShoppingCartService shoppingCartService;

    /**
     * 상품 생성
     */
    public Item createItem(User seller, ItemCreateRequestDto request){
        Item sellItem = Item.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();

        duplicationItemCheck(seller, request.getName()); // IllegalStateException 중복 아이템 예외

        Category category = categoryService.checkCategory(request.getCategoryId()); // NoSuchElementException 등록되지 않은 카테고리
        sellItem.setCategory(category);
        sellItem.setSeller(seller);

        itemRepository.save(sellItem);

        return sellItem;
    }

    /**
     * 상품 조회 - 판매자 본인것만
     */
    public Page<SearchItemDto> searchItems(Long sellerId, ItemSearchCondition condition, Pageable pageable){
        return itemRepository.searchItemPage(sellerId, condition, pageable);
    }

    /**
     * 상품 조회 - 일반 사용자
     */
    public Page<SearchItemFromCommonDto> searchItemFromCommon(ItemSearchFromCommonCondition condition, OrderCondition orderCondition, Pageable pageable) {
        return itemRepository.searchItemPageFromCommon(condition, orderCondition,pageable);
    }

    /**
     * 상풍 정보 변경
     */
    public Item changeItemInfo(Long sellerId, ChangeItemInfoRequestDto request) throws IllegalAccessException {
        Item item = checkItem(request.getItemId());
        if (!item.getSeller().getId().equals(sellerId)) {
            throw new IllegalAccessException("판매자의 상품이 아닙니다.");
        }
        if (request.getChangePrice() == null && request.getChangeStockQuantity() == null && request.getChangeCategoryId() == null) {
            throw new NoSuchElementException("변경할 정보가 없습니다.");
        }
        if (request.getChangePrice() != null){
            itemRepository.changePrice(item.getId(), request.getChangePrice()); // 쿼리 최적화
        }
        if (request.getChangeStockQuantity() != null) item.changeStockQuantity(request.getChangeStockQuantity());
        if (request.getChangeCategoryId() != null){
            Category category = categoryService.checkCategory(request.getChangeCategoryId()); // NoSuchElementException 없는 카테고리 예외
            item.changeCategory(category);
        }
        return item;
    }

    /**
     * 상품 삭제
     */
    public String deleteItem(User seller, Long itemId) throws IllegalAccessException {
        Item item = checkItem(itemId);
        if (!item.getSeller().getId().equals(seller.getId())) {
            throw new IllegalAccessException("판매자의 상품이 아닙니다.");
        }
        List<ShoppingCartItem> shoppingCartItems = item.getShoppingCartItems();
        if (!shoppingCartItems.isEmpty()) {
            shoppingCartService.deleteAllItemsByList(shoppingCartItems);
        }
        String name = item.getName();
        saveDeletedItem(seller, item);
        itemRepository.delete(item);
        return name;
    }

    /**
     * 카테고리 내 상품 카테고리 일괄 변경
     */
    public List<String> batchChangeItemCategory(List<CategoryItemDto> categoryItems, Category changeCategory) {
        List<String> itemNames = new ArrayList<>();
        List<Long> itemIds = new ArrayList<>();
        for (CategoryItemDto categoryItem : categoryItems) {
            itemIds.add(categoryItem.getItemId());
            itemNames.add(categoryItem.getItemName());
        }
        itemRepository.batchUpdateItemCategory(itemIds, changeCategory);
        return itemNames;
    }

    // 아이템 존재 확인
    public Item checkItem(Long itemId) {
        Optional<Item> findItem = itemRepository.findById(itemId);
        if (findItem.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 상품입니다.");
        }
        return findItem.get();
    }

    // 중복 상품 조회
    public void duplicationItemCheck(User seller, String itemName) {
        Optional<Item> bySellerAndName = itemRepository.findBySellerAndName(seller, itemName);
        if (bySellerAndName.isPresent()) {
            throw new IllegalStateException("이미 판매자가 판매중인 상품입니다.");
        }
    }

    /**
     * 삭제되는 상품 정보 보존
     */
    public void saveDeletedItem(User seller, Item item) {
        DeletedItem deletedItem = DeletedItem.builder()
                .sellerId(seller.getId())
                .sellerName(seller.getName())
                .sellerNickName(seller.getNickname())
                .sellerEmail(seller.getEmail())
                .sellerPNum(seller.getPNum())
                .sellerBirth(seller.getBirth())
                .sellerAddress(seller.getAddress())
                .itemId(item.getId())
                .itemName(item.getName())
                .itemPrice(item.getPrice())
                .build();
        deletedItemRepository.save(deletedItem);
    }

    /**
     * DeletedItem 보존 기간 지난 정보 삭제
     * 매일 자정(0시 0분 0초)에 보관기간이 7일 이상 지난 데이터 삭제
     */
    @Transactional
    @Async
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpireDeletedItem() {
        deletedItemRepository.deleteItemByCreatedAtLessThanEqual(LocalDateTime.now().minusDays(7));
    }
}

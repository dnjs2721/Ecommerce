package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import won.ecommerce.entity.*;
import won.ecommerce.repository.dto.search.item.OrderCondition;
import won.ecommerce.repository.dto.search.item.ItemSearchCondition;
import won.ecommerce.repository.dto.search.item.ItemSearchFromCommonCondition;
import won.ecommerce.repository.dto.search.item.SearchItemDto;
import won.ecommerce.repository.dto.search.item.SearchItemFromCommonDto;
import won.ecommerce.repository.item.ItemRepository;
import won.ecommerce.service.dto.item.ChangeItemInfoRequestDto;
import won.ecommerce.service.dto.item.ItemCreateRequestDto;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final CategoryService categoryService;

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
        if (request.getChangePrice() != null) item.changePrice(request.getChangePrice());
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
    public String deleteItem(Long sellerId, Long itemId) throws IllegalAccessException {
        Item item = checkItem(itemId);
        if (!item.getSeller().getId().equals(sellerId)) {
            throw new IllegalAccessException("판매자의 상품이 아닙니다.");
        }
        String name = item.getName();
        itemRepository.delete(item);
        return name;
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
}

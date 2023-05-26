package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.http.PUT;
import won.ecommerce.entity.*;
import won.ecommerce.repository.dto.search.item.ItemSearchCondition;
import won.ecommerce.repository.dto.search.item.SearchItemDto;
import won.ecommerce.repository.item.ItemRepository;
import won.ecommerce.service.dto.ChangeItemInfoRequestDto;
import won.ecommerce.service.dto.ItemCreateRequestDto;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
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

        Category category = categoryService.findCategoryById(request.getCategoryId()); // NoSuchElementException 등록되지 않은 카테고리
        sellItem.setCategory(category);
        sellItem.setSeller(seller);

        itemRepository.save(sellItem);

        return sellItem;
    }

    /**
     * 상풍 정보 변경
     */
    public Item changeItemInfo(Long sellerId, ChangeItemInfoRequestDto request) throws IllegalAccessException {
        Optional<Item> findItem = itemRepository.findById(request.getItemId());
        if (findItem.isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 상품입니다.");
        }
        Item item = findItem.get();
        if (!item.getSeller().getId().equals(sellerId)) {
            throw new IllegalAccessException("판매자의 상품이 아닙니다.");
        }
        if (request.getChangePrice() == null && request.getChangeStockQuantity() == null && request.getChangeCategoryId() == null) {
            throw new NoSuchElementException("변경할 정보가 없습니다.");
        }
        if (request.getChangePrice() != null) item.changePrice(request.getChangePrice());
        if (request.getChangeStockQuantity() != null) item.changeStockQuantity(request.getChangeStockQuantity());
        if (request.getChangeCategoryId() != null){
            Category category = categoryService.findCategoryById(request.getChangeCategoryId()); // NoSuchElementException 없는 카테고리 예외
            item.changeCategory(category);
        }

        return item;
    }

    // 중복 상품 조회
    public void duplicationItemCheck(User seller, String itemName) {
        Optional<Item> bySellerAndName = itemRepository.findBySellerAndName(seller, itemName);
        if (bySellerAndName.isPresent()) {
            throw new IllegalStateException("이미 판매자가 판매중인 상품입니다.");
        }
    }
}

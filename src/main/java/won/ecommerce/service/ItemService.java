package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.*;
import won.ecommerce.repository.ItemRepository;
import won.ecommerce.service.dto.ItemCreateRequestDto;

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
    @Transactional
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
     * 판매 상품 확인
     */

    public void duplicationItemCheck(User seller, String itemName) {
        Optional<Item> bySellerAndName = itemRepository.findBySellerAndName(seller, itemName);
        if (bySellerAndName.isPresent()) {
            throw new IllegalStateException("이미 판매자가 판매중인 상품입니다.");
        }
    }
}

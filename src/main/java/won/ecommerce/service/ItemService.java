package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.Category;
import won.ecommerce.entity.Item;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.repository.ItemRepository;
import won.ecommerce.service.dto.ItemCreateRequestDto;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    @Transactional
    public Item createItem(Long sellerId, ItemCreateRequestDto request) throws IllegalAccessException {
        User seller = userService.findUserById(sellerId); // NoSuchElementException 가입되지 않은 회원 에외
        if (!seller.getStatus().equals(UserStatus.SELLER)) {
            throw new IllegalAccessException("판매자가 아닙니다. 먼저 판매자 신청을 해주세요.");
        }
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

    public void duplicationItemCheck(User seller, String itemName) {
        Optional<Item> bySellerAndName = itemRepository.findBySellerAndName(seller, itemName);
        if (bySellerAndName.isPresent()) {
            throw new IllegalStateException("이미 판매자가 판매중인 상품입니다.");
        }
    }
}

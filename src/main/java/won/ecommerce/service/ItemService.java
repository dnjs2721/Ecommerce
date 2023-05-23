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

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    @Transactional
    public Long createItem(Long sellerId, ItemCreateRequestDto request) {
        User seller = userService.findUserById(sellerId);
        if (seller.getStatus().equals(UserStatus.SELLER)) {
            throw new IllegalStateException("판매자가 아닙니다. 먼저 판매자 신청을 해주세요.");
        }
        Item sellItem = Item.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();

        Category category = categoryService.findCategoryById(request.getCategoryId());
        sellItem.setCategory(category);

        sellItem.setSeller(seller);

        return sellItem.getId();
    }
}

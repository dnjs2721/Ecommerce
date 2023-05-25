package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import won.ecommerce.entity.Item;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.repository.ItemRepository;
import won.ecommerce.repository.dto.ItemSearchCondition;
import won.ecommerce.repository.dto.SearchItemDto;
import won.ecommerce.service.dto.ItemCreateRequestDto;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final ItemService itemService;
    private final UserService userService;
    private final ItemRepository itemRepository;

    /**
     * 판매 상품 등록
     */
    public Item registerItem(Long sellerId, ItemCreateRequestDto request) throws IllegalAccessException {
        User seller = userService.findUserById(sellerId); // NoSuchElementException 가입되지 않은 회원 에외
        if (!seller.getStatus().equals(UserStatus.SELLER)) {
            throw new IllegalAccessException("판매자가 아닙니다. 먼저 판매자 신청을 해주세요.");
        }
        return itemService.createItem(seller, request);
    }

    /**
     * 판매 상품 확인
     */
    public Page<SearchItemDto> searchItems(Long sellerId, ItemSearchCondition condition, Pageable pageable) {
        return itemRepository.searchItemPage(sellerId, condition, pageable);
    }
}

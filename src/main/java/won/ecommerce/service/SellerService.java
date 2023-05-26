package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.Item;
import won.ecommerce.entity.User;
import won.ecommerce.entity.UserStatus;
import won.ecommerce.repository.item.ItemRepository;
import won.ecommerce.repository.dto.search.item.ItemSearchCondition;
import won.ecommerce.repository.dto.search.item.SearchItemDto;
import won.ecommerce.service.dto.ChangeItemInfoRequestDto;
import won.ecommerce.service.dto.ItemCreateRequestDto;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final ItemService itemService;
    private final UserService userService;
    private final ItemRepository itemRepository;

    /**
     * 판매 상품 등록
     */
    @Transactional
    public Item registerItem(Long sellerId, ItemCreateRequestDto request) throws IllegalAccessException {
        User seller = checkSeller(sellerId); // NoSuchElementException 가입되지 않은 회원 에외, IllegalAccessException 판매자 아닐 때 예외
        return itemService.createItem(seller, request);
    }

    /**
     * 판매 상품 확인
     */
    public Page<SearchItemDto> searchItems(Long sellerId, ItemSearchCondition condition, Pageable pageable) throws IllegalAccessException {
        User seller = checkSeller(sellerId); // NoSuchElementException 가입되지 않은 회원, 변경 정보없음 에외, IllegalAccessException 판매자 아닐 때 예외
        return itemRepository.searchItemPage(sellerId, condition, pageable);
    }

    /**
     * 상품 정보 변경
     */
    @Transactional
    public Item changeItemInfo(Long sellerId ,ChangeItemInfoRequestDto request) throws IllegalAccessException {
        User seller = checkSeller(sellerId); // NoSuchElementException 가입되지 않은 회원 에외, IllegalAccessException 판매자 아닐 때 예외
        return itemService.changeItemInfo(sellerId, request); // NoSuchElementException 존재하지 않는 상품. IllegalAccessException 판매자의 상품이 아닐 떄 예외`
    }


    // 판매자 확인
    public User checkSeller(Long sellerId) throws IllegalAccessException {
        User seller = userService.findUserById(sellerId); // NoSuchElementException 가입되지 않은 회원 에외
        if (!seller.getStatus().equals(UserStatus.SELLER)) {
            throw new IllegalAccessException("판매자가 아닙니다. 먼저 판매자 신청을 해주세요.");
        }
        return seller;
    }
}

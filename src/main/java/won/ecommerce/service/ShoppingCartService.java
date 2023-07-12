package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.*;
import won.ecommerce.exception.NotEnoughStockException;
import won.ecommerce.repository.dto.search.shoppingCart.SearchShoppingCartDto;
import won.ecommerce.repository.shoppingCart.ShoppingCartItemRepository;
import won.ecommerce.repository.shoppingCart.ShoppingCartRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final OrdersService ordersService;

    /**
     * 장바구니 생성
     */
    @Transactional
    public ShoppingCart createShoppingCart() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCartRepository.save(shoppingCart);
        return shoppingCart;
    }

    /**
     * 장바구니 삭제
     */
    public void deleteShoppingCart(ShoppingCart shoppingCart) {
//        List<ShoppingCartItem> shoppingCartItems = shoppingCart.getShoppingCartItems();
//        if (!shoppingCartItems.isEmpty()) {
//            deleteShoppingCartItemByList(shoppingCartItems);
//        }
        shoppingCartRepository.delete(shoppingCart);
    }

    /**
     * 장바구니 상품 추가
     */
    public void addItem(ShoppingCart shoppingCart, Item item, int itemCount) {
        Optional<ShoppingCartItem> optionalShoppingCartItem = shoppingCartItemRepository.findByShoppingCartIdAndItemId(shoppingCart.getId(), item.getId());
        if (optionalShoppingCartItem.isEmpty()) {
            ShoppingCartItem shoppingCartItem = new ShoppingCartItem(shoppingCart, item, itemCount);
            shoppingCartItemRepository.save(shoppingCartItem);
        } else {
            ShoppingCartItem shoppingCartItem = optionalShoppingCartItem.get();
            shoppingCartItem.changeItemCount(itemCount + shoppingCartItem.getItemCount());
        }
    }

    /**
     * 장바구니 상품 수량 변경
     */
    public String changeCount(Long shoppingCartId, Long shoppingCartItemId, int changeCount) throws IllegalAccessException {
        Optional<ShoppingCartItem> optionalShoppingCartItem = shoppingCartItemRepository.findById(shoppingCartItemId);
        if (optionalShoppingCartItem.isEmpty()) {
            throw new NoSuchElementException("잘못된 장바구니 상품입니다.");
        }
        ShoppingCartItem shoppingCartItem = optionalShoppingCartItem.get();
        if (!shoppingCartItem.getShoppingCart().getId().equals(shoppingCartId)) {
            throw new IllegalAccessException("사용자의 장바구니 상품이 아닙니다.");
        }
        shoppingCartItem.changeItemCount(changeCount);

        return shoppingCartItem.getItem().getName();
    }

    /**
     * 장바구니 상품 전체 삭제
     */
    public void deleteAllItems(ShoppingCart shoppingCart) {
        List<ShoppingCartItem> shoppingCartItems = shoppingCart.getShoppingCartItems();
        if (shoppingCartItems.isEmpty()) {
            throw new NoSuchElementException("장바구니에 담긴 상품이 없습니다.");
        }
        deleteShoppingCartItemByList(shoppingCartItems);
    }


    /**
     * 장바구니 상품 리스트 삭제 List<ShoppingCartItem>
     */
    public void deleteShoppingCartItemByList(List<ShoppingCartItem> shoppingCartItems) {
        List<Long> shoppingCartItemIds = new ArrayList<>();
        for (ShoppingCartItem shoppingCartItem : shoppingCartItems) {
            shoppingCartItemIds.add(shoppingCartItem.getId());
        }
        shoppingCartItemRepository.deleteAllByIdInBatch(shoppingCartItemIds);
    }

    /**
     * 장바구니 상품 리스트 삭제 List<Long>
     */
    public List<String> deleteShoppingCartItemByListIds(User user, List<Long> shoppingCartItemIds) {
        List<ShoppingCartItem> shoppingCartItems = findShoppingCartItemByShoppingCartIdAndIds(user, shoppingCartItemIds);
        List<String> itemsName = new ArrayList<>();

        for (ShoppingCartItem shoppingCartItem : shoppingCartItems) {
            itemsName.add(shoppingCartItem.getItem().getName());
        }
        shoppingCartItemRepository.deleteAllByIdInBatch(shoppingCartItemIds);

        return itemsName;
    }

    /**
     * 장바구니 전체 상품 조회
     */
    public Page<SearchShoppingCartDto> getShoppingCartItems(Long shoppingCartId, Pageable pageable) {
        return shoppingCartItemRepository.searchShoppingCart(shoppingCartId, pageable);
    }

    /**
     * 장바구니 전체 상품 주문
     */
    public List<String> orderAllItemAtShoppingCart(User user) {
        ShoppingCart shoppingCart = user.getShoppingCart();

        List<ShoppingCartItem> shoppingCartItems = shoppingCart.getShoppingCartItems();
        if (shoppingCartItems.isEmpty()) {
            throw new NoSuchElementException("장바구니에 담긴 상품이 없습니다.");
        }

        List<String> itemsName = new ArrayList<>();
        Map<Item, Integer> itemAndCountMap = createItemAndCountMap(shoppingCartItems, itemsName);

        ordersService.createOrders(user, itemAndCountMap); // 구매자용, 판매자용 주문 생성
        deleteShoppingCartItemByList(shoppingCartItems); // 장바구니 비우기

        return itemsName;
    }

    /**
     * 장바구니 상품 선택 주문
     */
    public List<String> orderSelectItemAtShoppingCart(User user, List<Long> shoppingCartItemIds) {

        List<ShoppingCartItem> shoppingCartItems = findShoppingCartItemByShoppingCartIdAndIds(user, shoppingCartItemIds);

        List<String> itemsName = new ArrayList<>();

        Map<Item, Integer> itemAndCountMap = createItemAndCountMap(shoppingCartItems, itemsName);

        ordersService.createOrders(user, itemAndCountMap);
        shoppingCartItemRepository.deleteAllByIdInBatch(shoppingCartItemIds);

        return itemsName;
    }

    /**
     * Item, Count 맵 작성
     */
    public Map<Item, Integer> createItemAndCountMap(List<ShoppingCartItem> shoppingCartItems, List<String> itemsName) {
        Map<Item, Integer> itemsAndCount = new HashMap<>();

        for (ShoppingCartItem shoppingCartItem : shoppingCartItems) {
            Item item = shoppingCartItem.getItem();
            int shoppingCartItemCount = shoppingCartItem.getItemCount();

            if (shoppingCartItemCount > item.getStockQuantity()) {
                throw new NotEnoughStockException(item.getName() + "의 재고가 부족합니다.");
            }

            itemsAndCount.put(item, shoppingCartItemCount);

            itemsName.add(item.getName());
        }

        return itemsAndCount;
    }

    /**
     * 사용자의 장바구니 상품인지 검사
     */
    public List<ShoppingCartItem> findShoppingCartItemByShoppingCartIdAndIds(User user, List<Long> shoppingCartItemIds){
        Long shoppingCartId = user.getShoppingCart().getId();
        List<ShoppingCartItem> shoppingCartItems =
                shoppingCartItemRepository.findShoppingCartItemByShoppingCartIdAndIds(shoppingCartId, shoppingCartItemIds);
        if ((shoppingCartItemIds.size() != shoppingCartItems.size()) || shoppingCartItemIds.isEmpty()) {
            throw new IllegalArgumentException("잘못된 장바구니 상품 정보입니다.");
        }
        return shoppingCartItems;
    }

    public List<ShoppingCartItem> findShoppingCartItemByItemIds(List<Long> itemIds) {
        return shoppingCartItemRepository.findShoppingCartItemByItemIds(itemIds);
    }
}

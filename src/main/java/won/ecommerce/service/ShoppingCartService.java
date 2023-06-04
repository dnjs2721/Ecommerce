package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.*;
import won.ecommerce.repository.shoppingCart.ShoppingCartItemRepository;
import won.ecommerce.repository.shoppingCart.ShoppingCartRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final ShoppingCartRepository shoppingCartRepository;

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
        deleteAllItems(shoppingCart);
        shoppingCartRepository.delete(shoppingCart);
    }

    /**
     * 장바구니 상품 추가
     */
    public void addItem(ShoppingCart shoppingCart, Item item, Integer itemCount) {
        Optional<ShoppingCartItem> findItem = shoppingCartItemRepository.findByShoppingCartIdAndItemId(shoppingCart.getId(), item.getId());
        if (itemCount == null) {
            itemCount = 1;
        }
        if (findItem.isEmpty()) {
            ShoppingCartItem shoppingCartItem = new ShoppingCartItem(shoppingCart, item, itemCount);
            shoppingCartItemRepository.save(shoppingCartItem);
        } else {
            ShoppingCartItem shoppingCartItem = findItem.get();
            shoppingCartItem.changeItemCount(itemCount + shoppingCartItem.getItemCount());
        }
    }

    /**
     * 장바구니 상품 삭제
     */
    public void deleteItem(Long shoppingCartId, Long itemId, Integer itemCount) {
        ShoppingCartItem shoppingCartItem = findShoppingCartItem(shoppingCartId, itemId);
        if (itemCount == null || shoppingCartItem.getItemCount() <= itemCount) {
            shoppingCartItemRepository.delete(shoppingCartItem);
        } else{
            shoppingCartItem.changeItemCount(shoppingCartItem.getItemCount() - itemCount);
        }
    }

    /**
     * 장바구니 상품 전체 삭제
     */
    public void deleteAllItems(ShoppingCart shoppingCart) {
        List<ShoppingCartItem> shoppingCartItems = shoppingCart.getShoppingCartItems();
        if (shoppingCartItems.isEmpty()) {
            throw new NoSuchElementException("장바구니가 이미 비워져있습니다.");
        }
        deleteAllItemsByList(shoppingCartItems);
    }

    /**
     * 장바구니 상품 리스트 삭제
     * 쿼리 최적화
     */
    public void deleteAllItemsByList(List<ShoppingCartItem> shoppingCartItems) {
        List<Long> shoppingCartItemIds = new ArrayList<>();
        for (ShoppingCartItem shoppingCartItem : shoppingCartItems) {
            shoppingCartItemIds.add(shoppingCartItem.getId());
        }
        shoppingCartItemRepository.deleteAllByIds(shoppingCartItemIds);
    }

    /**
     * 장바구니 전체 가격 조회
     */
    public int getTotalPrice(ShoppingCart shoppingCart) {
        return shoppingCart.getTotalPrice();
    }

    /**
     *
     */

    public ShoppingCartItem findShoppingCartItem(Long shoppingCartId, Long itemId) {
        Optional<ShoppingCartItem> findShoppingCartItem = shoppingCartItemRepository.findByShoppingCartIdAndItemId(shoppingCartId, itemId);
        if (findShoppingCartItem.isEmpty()) {
            throw new NoSuchElementException("장바구니에서 상품을 찾지 못했습니다.");
        }
        return findShoppingCartItem.get();
    }
}

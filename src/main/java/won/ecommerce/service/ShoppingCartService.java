package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import won.ecommerce.entity.*;
import won.ecommerce.repository.ShoppingCartItemRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {
    private final ShoppingCartItemRepository shoppingCartItemRepository;

    public void addItem(User user, Item item, int itemCount) {
        ShoppingCart shoppingCart = user.getShoppingCart();
        ShoppingCartItem shoppingCartItem = new ShoppingCartItem(shoppingCart, item, itemCount);
        shoppingCartItemRepository.save(shoppingCartItem);
    }

    public void deleteItem(User user, Item item, Integer itemCount) {
        ShoppingCart shoppingCart = user.getShoppingCart();
        Long shoppingCartId = shoppingCart.getId();
        Long itemId = item.getId();
        ShoppingCartItem shoppingCartItem = findShoppingCartItem(shoppingCartId, itemId);
        if (itemCount == null || shoppingCartItem.getItemCount() <= itemCount) {
            shoppingCartItemRepository.delete(shoppingCartItem);
        } else{
            shoppingCartItem.changeItemCount(shoppingCartItem.getItemCount() - itemCount);
        }
    }

    public int getTotalPrice(ShoppingCart shoppingCart) {
        List<ShoppingCartItem> shoppingCartItems = shoppingCart.getShoppingCartItems();
        int totalPrice = 0;
        for (ShoppingCartItem item : shoppingCartItems) {
            totalPrice += item.getTotalItemPrice();
        }
        return totalPrice;
    }

    public ShoppingCartItem findShoppingCartItem(Long shoppingCartId, Long itemId) {
        Optional<ShoppingCartItem> findShoppingCartItem = shoppingCartItemRepository.findByShoppingCartIdAndItemId(shoppingCartId, itemId);
        if (findShoppingCartItem.isEmpty()) {
            throw new NoSuchElementException("장바구니에서 상품을 찾지 못했습니다.");
        }
        return findShoppingCartItem.get();
    }
}

package won.ecommerce.repository.shoppingCart;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import won.ecommerce.entity.ShoppingCartItem;
import won.ecommerce.repository.dto.search.shoppingCart.SearchShoppingCartDto;

import java.util.List;


public interface ShoppingCartItemRepositoryCustom{
    Page<SearchShoppingCartDto> searchShoppingCart(Long shoppingCartId, Pageable pageable);

    List<ShoppingCartItem> findShoppingCartItemByShoppingCartIdAndIds(Long shoppingCartId, List<Long> shoppingCartItemIds);

    List<ShoppingCartItem> findShoppingCartItemByItemIds(List<Long> itemIds);
}

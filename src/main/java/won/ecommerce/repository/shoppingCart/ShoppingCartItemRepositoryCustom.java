package won.ecommerce.repository.shoppingCart;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import won.ecommerce.repository.dto.search.shoppingCart.SearchShoppingCartDto;

import java.util.List;

public interface ShoppingCartItemRepositoryCustom{
    void deleteAllByIds(List<Long> ids);
    Page<SearchShoppingCartDto> searchShoppingCart(Long shoppingCartId, Pageable pageable);
}

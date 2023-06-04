package won.ecommerce.repository.shoppingCart;

import java.util.List;

public interface ShoppingCartItemRepositoryCustom{
    void deleteAllByIds(List<Long> ids);
}

package won.ecommerce.repository.dto.search.shoppingCart;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class SearchShoppingCartDto {
    private Long shoppingCartItemId;
    private String itemName;
    private String itemSellerNickName;
    private int itemCount;
    private int itemPrice;
    private int totalItemPrice;

    @QueryProjection
    public SearchShoppingCartDto( Long shoppingCartItemId, String itemName, String itemSellerNickName, int itemCount, int itemPrice, int totalItemPrice) {
        this.shoppingCartItemId = shoppingCartItemId;
        this.itemName = itemName;
        this.itemSellerNickName = itemSellerNickName;
        this.itemCount = itemCount;
        this.itemPrice = itemPrice;
        this.totalItemPrice = totalItemPrice;
    }
}

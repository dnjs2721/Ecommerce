package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingCartItem {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shoppingCartId")
    private ShoppingCart shoppingCart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemId")
    private Item item;
    private int itemCount;
    private int itemPrice;
    private int totalItemPrice;

    public ShoppingCartItem(ShoppingCart shoppingCart, Item item, int itemCount) {
        this.shoppingCart = shoppingCart;
        this.item = item;
        this.itemCount = itemCount;
        this.itemPrice = item.getPrice();
        this.totalItemPrice = item.getPrice() * itemCount;
    }

    public void changeItemCount(int itemCount) {
        this.itemCount = itemCount;
        this.totalItemPrice = this.itemPrice * itemCount;
    }
}

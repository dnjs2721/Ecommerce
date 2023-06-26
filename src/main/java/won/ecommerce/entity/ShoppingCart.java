package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class ShoppingCart extends BaseTimeEntity{
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(mappedBy = "shoppingCart")
    private User user;

    @OneToMany(mappedBy = "shoppingCart")
    private List<ShoppingCartItem> shoppingCartItems = new ArrayList<>();

    public int getTotalPrice() {
        List<ShoppingCartItem> findItems = this.getShoppingCartItems();
        int totalPrice = 0;
        for (ShoppingCartItem shoppingCartItem : findItems) {
            totalPrice += shoppingCartItem.getTotalItemPrice();
        }
        return totalPrice;
    }
}

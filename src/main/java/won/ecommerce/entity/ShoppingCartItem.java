package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class ShoppingCartItem {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shoppingCartId")
    private ShoppingCart shoppingCartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemId")
    private Item item;
}

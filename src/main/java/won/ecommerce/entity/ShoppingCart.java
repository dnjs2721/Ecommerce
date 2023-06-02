package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class ShoppingCart {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(mappedBy = "shoppingCart")
    private User user;

    @OneToMany(mappedBy = "shoppingCart")
    private final List<ShoppingCartItem> shoppingCartItems = new ArrayList<>();
}

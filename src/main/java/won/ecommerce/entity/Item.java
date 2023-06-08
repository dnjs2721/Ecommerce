package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import won.ecommerce.exception.NotEnoughStockException;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseTimeEntity{
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sellerId")
    private User seller;
    private String name;
    private int price;
    private int stockQuantity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId")
    private Category category;

    @OneToMany(mappedBy = "item")
    private final List<ShoppingCartItem> shoppingCartItems = new ArrayList<>();

    @Builder
    public Item(String name, int price, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public void setCategory(Category category) {
        this.category = category;
        category.getItems().add(this);
    }

    public void setSeller(User user) {
        this.seller = user;
        user.getSellItems().add(this);
    }

    public void changeStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void decreaseStockQuantity(int count) {
        int restStock = this.stockQuantity - count;
        if (restStock < 0) {
            throw new NotEnoughStockException(this.getName() + "의 재고가 부족합니다.");
        }
        this.stockQuantity = restStock;
    }

    public void increaseStockQuantity(int count) {
        this.stockQuantity += count;
    }


    public void changeCategory(Category category) {
        this.category.getItems().remove(this);
        this.category = category;
        category.getItems().add(this);
    }
}

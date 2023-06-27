package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String nickname;
    private String email;
    private String password;
    private String pNum;
    private String birth;
    @Embedded
    private Address address;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @OneToMany(mappedBy = "seller")
    private final List<Item> sellItems = new ArrayList<>();
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "shoppingCartId")
    private ShoppingCart shoppingCart;
    @OneToMany(mappedBy = "buyer")
    private final List<OrdersForBuyer> ordersForBuyer = new ArrayList<>();
    @OneToMany(mappedBy = "seller")
    private final List<OrdersForSeller> ordersForSeller = new ArrayList<>();


    @Builder
    public User(String name, String nickname, String email, String password, String pNum, String birth, Address address) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.pNum = pNum;
        this.birth = birth;
        this.address = address;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeAddress(Address address) {
        this.address = address;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }
}

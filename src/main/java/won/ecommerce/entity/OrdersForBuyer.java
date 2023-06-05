package won.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrdersForBuyer extends BaseTimeEntity {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyerId")
    private User buyer;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    @OneToMany(mappedBy = "buyerOrderId")
    private final List<OrderItem> orderItems = new ArrayList<>();

    public OrdersForBuyer(User buyer) {
        this.buyer = buyer;
        this.orderStatus = OrderStatus.WAITING_FOR_PAYMENT;
    }
}

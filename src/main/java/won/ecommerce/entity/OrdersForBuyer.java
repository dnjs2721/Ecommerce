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
    @OneToMany(mappedBy = "buyerOrderId")
    private final List<OrderItem> orderItems = new ArrayList<>();

    private String impUid;

    public OrdersForBuyer(User buyer) {
        this.buyer = buyer;
    }

    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : this.orderItems) {
            if (orderItem.getOrderItemStatus() != OrderItemStatus.CANCEL) {
                totalPrice += orderItem.getTotalPrice();
            }
        }
        return totalPrice;
    }

    public List<String> getOrderItemsName() {
        List<String> itemsName = new ArrayList<>();
        for (OrderItem orderItem : this.orderItems) {
            if (orderItem.getOrderItemStatus() != OrderItemStatus.CANCEL) {
                itemsName.add(orderItem.getItemName());
            }
        }
        return itemsName;
    }

    public void setImpUid(String impUid) {
        this.impUid = impUid;
    }
}

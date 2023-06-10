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

    private String merchantUid;

    public OrdersForBuyer(User buyer) {
        this.buyer = buyer;
    }

    public int getTotalPrice() {
        List<OrderItem> findItems = this.getOrderItems();
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getOrderItemStatus() != OrderItemStatus.CANCEL) {
                totalPrice += orderItem.getTotalPrice();
            }
        }
        return totalPrice;
    }

    public List<String> getOrderItemsName() {
        List<OrderItem> findItems = this.getOrderItems();
        List<String> itemsName = new ArrayList<>();
        for (OrderItem orderItem : findItems) {
            if (orderItem.getOrderItemStatus() != OrderItemStatus.CANCEL) {
                itemsName.add(orderItem.getItemName());
            }
        }
        return itemsName;
    }

    public void setMerchantUid(String merchantUid) {
        this.merchantUid = merchantUid;
    }
}

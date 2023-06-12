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
public class OrdersForSeller extends BaseTimeEntity {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sellerId")
    private User seller;
    private String buyerName;
    private String buyerPNum;
    @Embedded
    private Address buyerAddress;
    @OneToMany(mappedBy = "sellerOrderId")
    private final List<OrderItem> orderItems = new ArrayList<>();
    private String impUid;

    @Builder
    public OrdersForSeller(User seller, String buyerName, String buyerPNum, Address buyerAddress) {
        this.seller = seller;
        this.buyerName = buyerName;
        this.buyerPNum = buyerPNum;
        this.buyerAddress = buyerAddress;
    }

    public void setImpUid(String impUid) {
        this.impUid = impUid;
    }
}

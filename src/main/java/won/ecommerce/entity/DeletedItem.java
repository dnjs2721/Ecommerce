package won.ecommerce.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeletedItem extends BaseTimeEntity{
    @Id
    @GeneratedValue
    private Long id;
    private Long sellerId;
    private String sellerName;
    private String sellerNickName;
    private String sellerEmail;
    private String sellerPNum;
    private String sellerBirth;
    @Embedded
    private Address sellerAddress;
    private Long itemId;
    private String itemName;
    private int itemPrice;

    @Builder
    public DeletedItem(Long sellerId, String sellerName, String sellerNickName, String sellerEmail, String sellerPNum, String sellerBirth, Address sellerAddress, Long itemId, String itemName, int itemPrice) {
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.sellerNickName = sellerNickName;
        this.sellerEmail = sellerEmail;
        this.sellerPNum = sellerPNum;
        this.sellerBirth = sellerBirth;
        this.sellerAddress = sellerAddress;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
    }
}

package won.ecommerce.repository.dto.search.item;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class SearchItemFromCommonDto {
    private SellerInfoDto seller;
    private String category;
    private String itemName;
    private int price;


    @QueryProjection
    public SearchItemFromCommonDto(SellerInfoDto seller, String category, String itemName, int price) {
        this.seller = seller;
        this.category = category;
        this.itemName = itemName;
        this.price = price;
    }
}

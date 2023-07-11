package won.ecommerce.service.dto.category;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryItemMailElementDto {
    Long sellerId;
    String sellerName;
    String sellerEmail;
    List<String> itemsName = new ArrayList<>();

    @QueryProjection
    public CategoryItemMailElementDto(Long sellerId, String sellerName, String sellerEmail, List<String> itemsName) {
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.sellerEmail = sellerEmail;
        this.itemsName = itemsName;
    }
}

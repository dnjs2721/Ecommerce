package won.ecommerce.service.dto.category;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryItemMailElementDto {
    String sellerName;
    String sellerEmail;
    List<String> itemsName = new ArrayList<>();

    public CategoryItemMailElementDto(String sellerName, String sellerEmail, String itemName) {
        this.sellerName = sellerName;
        this.sellerEmail = sellerEmail;
        this.itemsName.add(itemName);
    }
}

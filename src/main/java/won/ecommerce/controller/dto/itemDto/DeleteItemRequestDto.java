package won.ecommerce.controller.dto.itemDto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DeleteItemRequestDto {
    List<Long> itemIds = new ArrayList<>();
}

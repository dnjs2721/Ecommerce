package won.ecommerce.repository.deleted.item;

import java.time.LocalDateTime;

public interface DeletedItemRepositoryCustom {
    void deleteItemByCreatedAtLessThanEqual(LocalDateTime localDateTime);
}

package won.ecommerce.repository.deleted.user;

import java.time.LocalDateTime;

public interface DeletedUserRepositoryCustom {
    void deleteUserByCreatedAtLessThanEqual(LocalDateTime localDateTime);
}

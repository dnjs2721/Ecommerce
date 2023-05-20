package won.ecommerce.repository.dto;

import lombok.Data;
import won.ecommerce.entity.UserStatus;

@Data
public class UserSearchCondition {
    private UserStatus userStatus;
}

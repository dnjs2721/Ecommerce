package won.ecommerce.repository.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import won.ecommerce.repository.dto.search.user.SearchUsersDto;
import won.ecommerce.repository.dto.search.user.UserSearchCondition;

public interface UserRepositoryCustom {
    String findEmailByNameAndPNum(String name, String pNum);

    Page<SearchUsersDto> searchUsersPage(UserSearchCondition condition, Pageable pageable);
}

package won.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import won.ecommerce.repository.dto.SearchUsersDto;
import won.ecommerce.repository.dto.UserSearchCondition;

public interface UserRepositoryCustom {
    String findEmailByNameAndPNum(String name, String pNum);

    Page<SearchUsersDto> searchUsersPage(UserSearchCondition condition, Pageable pageable);
}

package won.ecommerce.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    Optional<User> findBypNum(String pNum);
}

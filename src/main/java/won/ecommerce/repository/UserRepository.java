package won.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

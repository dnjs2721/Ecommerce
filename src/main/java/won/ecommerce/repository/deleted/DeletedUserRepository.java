package won.ecommerce.repository.deleted;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.DeletedUser;

public interface DeletedUserRepository extends JpaRepository<DeletedUser, Long> {
}

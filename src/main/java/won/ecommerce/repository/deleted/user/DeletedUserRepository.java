package won.ecommerce.repository.deleted.user;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.DeletedUser;
import won.ecommerce.repository.deleted.item.DeletedItemRepositoryCustom;

public interface DeletedUserRepository extends JpaRepository<DeletedUser, Long>, DeletedUserRepositoryCustom {
}

package won.ecommerce.repository.changeStatusLog;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.ChangeStatusLog;
import won.ecommerce.entity.LogStatus;

import java.util.Optional;

public interface ChangeStatusLogRepository extends JpaRepository<ChangeStatusLog, Long>, ChangeStatusLogRepositoryCustom {
    Optional<ChangeStatusLog> findByUserIdAndLogStat(Long id, LogStatus logStat);
}

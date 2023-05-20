package won.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import won.ecommerce.entity.ChangeStatusLog;
import won.ecommerce.entity.LogStat;

import java.util.Optional;

public interface ChangeStatusLogRepository extends JpaRepository<ChangeStatusLog, Long>, ChangeStatusLogRepositoryCustom {
    Optional<ChangeStatusLog> findByUserIdAndLogStat(Long id, LogStat logStat);
}

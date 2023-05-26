package won.ecommerce.repository.changeStatusLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import won.ecommerce.repository.dto.search.statusLog.SearchStatusLogDto;
import won.ecommerce.repository.dto.search.statusLog.StatusLogSearchCondition;

public interface ChangeStatusLogRepositoryCustom {
    Page<SearchStatusLogDto> searchLogsPage(StatusLogSearchCondition condition, Pageable pageable);
}

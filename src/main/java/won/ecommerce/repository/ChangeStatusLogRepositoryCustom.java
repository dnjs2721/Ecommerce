package won.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import won.ecommerce.repository.dto.SearchStatusLogDto;
import won.ecommerce.repository.dto.StatusLogSearchCondition;

public interface ChangeStatusLogRepositoryCustom {
    Page<SearchStatusLogDto> searchLogsPage(StatusLogSearchCondition condition, Pageable pageable);
}

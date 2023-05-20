package won.ecommerce.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import won.ecommerce.entity.ChangeStatusLog;
import won.ecommerce.repository.dto.SearchStatusLogDto;
import won.ecommerce.repository.dto.StatusLogSearchCondition;

public interface ChangeStatusLogRepositoryCustom {
    Page<SearchStatusLogDto> searchPage(StatusLogSearchCondition condition, Pageable pageable);
}

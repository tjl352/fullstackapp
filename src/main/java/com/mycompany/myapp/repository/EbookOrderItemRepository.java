package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.EbookOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link EbookOrderItem}.
 */
@Repository
public interface EbookOrderItemRepository extends JpaRepository<EbookOrderItem, Long> {
    boolean existsByEbookId(Long ebookId);
}

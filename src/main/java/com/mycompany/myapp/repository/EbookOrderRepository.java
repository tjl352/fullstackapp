package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.EbookOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link EbookOrder}.
 */
@Repository
public interface EbookOrderRepository extends JpaRepository<EbookOrder, Long> {
    @Query(
        """
        select distinct ebookOrder from EbookOrder ebookOrder
        join fetch ebookOrder.user
        left join fetch ebookOrder.items item
        left join fetch item.ebook
        where ebookOrder.user.id = :userId
        order by ebookOrder.id
        """
    )
    List<EbookOrder> findAllByUserIdOrderByIdAsc(@Param("userId") Long userId);

    @Query(
        """
        select distinct ebookOrder from EbookOrder ebookOrder
        join fetch ebookOrder.user
        left join fetch ebookOrder.items item
        left join fetch item.ebook
        order by ebookOrder.id
        """
    )
    List<EbookOrder> findAllForHistory();

    @Query(
        """
        select distinct ebookOrder from EbookOrder ebookOrder
        join fetch ebookOrder.user
        left join fetch ebookOrder.items item
        left join fetch item.ebook
        where ebookOrder.id = :id
        """
    )
    Optional<EbookOrder> findOneWithDetails(@Param("id") Long id);
}

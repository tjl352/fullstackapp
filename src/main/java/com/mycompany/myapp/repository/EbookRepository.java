package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Ebook;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for originals still tracked as {@link Ebook}.
 */
@Repository
public interface EbookRepository extends JpaRepository<Ebook, Long> {
    @Query(
        "select ebook from Ebook ebook where ebook.id not in (select item.ebook.id from EbookOrderItem item) order by ebook.id"
    )
    List<Ebook> findUnsold();
}

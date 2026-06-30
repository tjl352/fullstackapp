package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link ChatMessage} entity.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT chatMessage FROM ChatMessage chatMessage WHERE chatMessage.user.id = :userId ORDER BY chatMessage.timestamp ASC")
    List<ChatMessage> findAllByUserIdOrderByTimestampAsc(@Param("userId") Long userId);
}

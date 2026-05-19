package com.aac.backend.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    @Query("SELECT c FROM ConversationMessage c WHERE c.user.id = :userId AND c.createdAt > :threshold ORDER BY c.createdAt ASC")
    List<ConversationMessage> findRecentContext(@Param("userId") Long userId, @Param("threshold") LocalDateTime threshold, Pageable pageable);
}

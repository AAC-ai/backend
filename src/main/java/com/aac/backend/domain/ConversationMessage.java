package com.aac.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "conversation_messages")
public class ConversationMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userInput;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String aiResponse;

    private ConversationMessage(User user, String userInput, String aiResponse) {
        this.user = user;
        this.userInput = userInput;
        this.aiResponse = aiResponse;
    }

    public static ConversationMessage create(User user, String userInput, String aiResponse) {
        return new ConversationMessage(user, userInput, aiResponse);
    }
}

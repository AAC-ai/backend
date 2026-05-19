package com.aac.backend.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "conversation.context")
public record ConversationContextProperties(int limit, int thresholdHours) {

}

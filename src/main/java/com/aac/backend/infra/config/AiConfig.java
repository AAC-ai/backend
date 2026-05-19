package com.aac.backend.infra.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class AiConfig {

    @Bean
    @Primary
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean("summarizerChatClient")
    @Profile("!ollama")
    public ChatClient openaiSummarizerChatClient(
            ChatClient.Builder builder,
            @Value("${generation.summarizer-model:gpt-4o-mini}") String model) {
        return builder
                .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                .build();
    }

    @Bean("summarizerChatClient")
    @Profile("ollama")
    public ChatClient ollamaSummarizerChatClient(
            ChatClient.Builder builder,
            @Value("${generation.summarizer-model:llama3.2}") String model) {
        return builder
                .defaultOptions(OllamaOptions.builder().model(model).build())
                .build();
    }
}

package com.aac.backend.infra;

public record GenerationResult(
        String sentence,
        String model,
        long latencyMs,
        boolean success,
        String failureReason,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens
) {

    public static GenerationResult success(String sentence, String model, long latencyMs,
                                           Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        return new GenerationResult(sentence, model, latencyMs, true, null, promptTokens, completionTokens, totalTokens);
    }

    public static GenerationResult failure(String model, long latencyMs, String failureReason) {
        return new GenerationResult(null, model, latencyMs, false, failureReason, null, null, null);
    }
}

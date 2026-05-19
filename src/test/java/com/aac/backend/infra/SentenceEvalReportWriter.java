package com.aac.backend.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class SentenceEvalReportWriter {

    private static final Path REPORT_DIR = Path.of("build", "reports", "eval");

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    void write(List<SentenceQualityEvalTest.CaseResult> results) throws IOException {
        Files.createDirectories(REPORT_DIR);

        var timestamp = LocalDateTime.now().toString().replaceAll("[:.T]", "-");
        objectMapper.writeValue(
                REPORT_DIR.resolve("eval-result-" + timestamp + ".json").toFile(),
                buildJsonReport(results)
        );
        Files.writeString(
                REPORT_DIR.resolve("eval-result-" + timestamp + ".md"),
                buildMarkdownReport(results)
        );
    }

    private Map<String, Object> buildJsonReport(List<SentenceQualityEvalTest.CaseResult> results) {
        long passCount = results.stream().filter(SentenceQualityEvalTest.CaseResult::pass).count();
        double passRate = results.isEmpty() ? 0 : (double) passCount / results.size();
        double avgScore = results.stream()
                .filter(r -> r.score() != null)
                .mapToDouble(SentenceQualityEvalTest.CaseResult::score)
                .average().orElse(0);

        var bySymbolCount = results.stream()
                .collect(Collectors.groupingBy(r -> r.symbolCount()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        e -> e.getKey() + "개",
                        e -> {
                            var group = e.getValue();
                            long gPass = group.stream().filter(SentenceQualityEvalTest.CaseResult::pass).count();
                            return Map.of(
                                    "total", group.size(),
                                    "pass", gPass,
                                    "fail", group.size() - gPass,
                                    "passRate", String.format("%.0f%%", (double) gPass / group.size() * 100)
                            );
                        },
                        (a, b) -> a,
                        java.util.LinkedHashMap::new
                ));

        return Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "summary", Map.of(
                        "total", results.size(),
                        "pass", passCount,
                        "fail", results.size() - passCount,
                        "passRate", String.format("%.1f%%", passRate * 100),
                        "avgScore", String.format("%.3f", avgScore)
                ),
                "bySymbolCount", bySymbolCount,
                "cases", results
        );
    }

    private String buildMarkdownReport(List<SentenceQualityEvalTest.CaseResult> results) {
        long passCount = results.stream().filter(SentenceQualityEvalTest.CaseResult::pass).count();
        long failCount = results.size() - passCount;
        double avgScore = results.stream()
                .filter(r -> r.score() != null)
                .mapToDouble(SentenceQualityEvalTest.CaseResult::score)
                .average().orElse(0);

        var sb = new StringBuilder();
        sb.append("# AAC 문장 품질 평가 리포트\n\n")
                .append("> ").append(LocalDateTime.now()).append("\n\n")
                .append("- total: ").append(results.size()).append('\n')
                .append("- pass: ").append(passCount).append('\n')
                .append("- fail: ").append(failCount).append('\n')
                .append("- passRate: ").append(String.format("%.1f%%", (double) passCount / results.size() * 100)).append('\n')
                .append("- avgScore: ").append(String.format("%.3f", avgScore)).append("\n\n")
                .append("| id | status | symbolCount | hasHistory | symbols | generatedSentence | score | reason |\n")
                .append("|---|---|---:|:---:|---|---|---:|---|\n");

        for (var r : results) {
            sb.append("| ").append(r.id())
                    .append(" | ").append(r.pass() ? "✅ PASS" : "❌ FAIL")
                    .append(" | ").append(r.symbolCount())
                    .append(" | ").append(r.referenceAnswer() != null ? "✓" : "")
                    .append(" | ").append(escape(r.symbols()))
                    .append(" | ").append(escape(r.generatedSentence()))
                    .append(" | ").append(r.score() != null ? String.format("%.2f", r.score()) : "-")
                    .append(" | ").append(escape(r.reason() != null ? r.reason() : r.failureReason()))
                    .append(" |\n");
        }

        sb.append("\n## 실패 케이스\n\n");
        var failed = results.stream().filter(r -> !r.pass()).toList();
        if (failed.isEmpty()) {
            sb.append("없음 🎉\n");
        } else {
            failed.forEach(r -> sb.append("### id=").append(r.id()).append('\n')
                    .append("- 기호: `").append(r.symbols()).append("`\n")
                    .append("- 생성: `").append(r.generatedSentence() != null ? r.generatedSentence() : "생성 실패").append("`\n")
                    .append("- 사유: ").append(r.reason() != null ? r.reason() : r.failureReason()).append("\n\n"));
        }

        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("|", "\\|").replace("\n", "<br>");
    }
}

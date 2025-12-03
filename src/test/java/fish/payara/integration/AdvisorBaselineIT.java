package fish.payara.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration test for advisor plugin Maven Invoker runs.
 * Compares advisor output captured in invoker build logs to baseline files.
 * 
 * This test verifies that the advisor plugin produces consistent and expected
 * output when run via Maven Invoker against test projects (e.g., test-ee11, test-ee10, test-mp6).
 */
public class AdvisorBaselineIT {

    @Test
    public void compareAdvisorOutputToBaselineForEE11() throws IOException {
        verifyAdvisorBaselineForProject("test-ee11");
    }

    @Test
    public void compareAdvisorOutputToBaselineForEE10() throws IOException {
        verifyAdvisorBaselineForProject("test-ee10");
    }

    @Test
    public void compareAdvisorOutputToBaselineForMP6() throws IOException {
        verifyAdvisorBaselineForProject("test-mp6");
    }

    private String normalizeWhitespace(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+", " ").trim();
    }

    /**
     * Verifies that the advisor output for a given test project matches the baseline.
     * 
     * @param projectName the test project name (e.g., "test-ee11"), which should have:
     *                    - invoker build log at: target/it/{projectName}/build.log
     *                    - baseline file at: src/it/{projectName}/advisor-baseline.txt
     * @throws IOException if reading files fails
     */
    private void verifyAdvisorBaselineForProject(String projectName) throws IOException {
        Path projectBase = Path.of(System.getProperty("user.dir"));
        Path invokerBuildLog = projectBase.resolve("target/its/" + projectName + "/build.log");
        Path baseline = projectBase.resolve("src/it/" + projectName + "/advisor-baseline.txt");

        Assumptions.assumeTrue(Files.exists(invokerBuildLog), 
                "Invoker build log not found: " + invokerBuildLog);
        Assumptions.assumeTrue(Files.exists(baseline), 
                "Baseline file not found: " + baseline + " — provide it to enable this test.");

        List<String> buildLines = Files.readAllLines(invokerBuildLog).stream()
            .map(this::normalizeWhitespace).collect(Collectors.toList());
        List<String> baselineLines = Files.readAllLines(baseline).stream()
            .map(this::normalizeWhitespace).collect(Collectors.toList());

        // Extract advisor section starting at 'Showing Advisories'
        List<String> extracted = extractAdvisorOutput(buildLines);

        // Normalize: collapse whitespace and remove empty lines
        List<String> extractedNormalized = extracted.stream()
            .map(this::normalizeWhitespace).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        List<String> baselineNormalized = baselineLines.stream()
            .map(this::normalizeWhitespace).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        // Join normalized lines into a single canonical string to avoid incidental
        // line-wrapping differences between environments (invoker vs direct run).
        String extrJoined = String.join(" ", extractedNormalized);
        String baseJoined = String.join(" ", baselineNormalized);

        if (!Objects.equals(extrJoined, baseJoined)) {
            String header = "Advisor output differs after normalization and joining.\n";
            header += "--- Extracted (first 300 chars) ---\n" + (extrJoined.length() > 300 ? extrJoined.substring(0, 300) + "..." : extrJoined) + "\n\n";
            header += "--- Baseline (first 300 chars) ---\n" + (baseJoined.length() > 300 ? baseJoined.substring(0, 300) + "..." : baseJoined) + "\n\n";
            fail(header + formatComparison(projectName, extractedNormalized, baselineNormalized));
        }
    }

    /**
     * Extracts the advisor output section from invoker build log lines.
     * Looks for the "Showing Advisories" marker and extracts until the next "BUILD" line.
     * 
     * @param buildLines all lines from the invoker build log
     * @return list of extracted advisor output lines, or fallback lines if marker not found
     */
    private List<String> extractAdvisorOutput(List<String> buildLines) {
        int start = -1;
        for (int i = 0; i < buildLines.size(); i++) {
            if (buildLines.get(i).contains("Showing Advisories")) {
                start = i;
                break;
            }
        }

        if (start >= 0) {
            // Take lines from "Showing Advisories" until a the block BUILD SUCCESS/FAILURE line or end
            int end = buildLines.size();
            for (int i = start; i < buildLines.size(); i++) {
                if (buildLines.get(i).contains("------------------------------------------------------------")) {
                    end = i;
                    break;
                }
            }
            return new ArrayList<>(buildLines.subList(start, end));
        } else {
            // Fallback: pick lines that look like advisor output
            return buildLines.stream()
                    .filter(l -> l.startsWith("Line of code:") || l.startsWith("Source file:") 
                            || l.contains("Showing Advisories") || l.startsWith("[ERROR]") 
                            || l.startsWith("[WARNING]"))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Formats a detailed comparison message for test failure output.
     * 
     * @param projectName the test project name
     * @param extracted the extracted advisor output lines
     * @param baseline the baseline lines
     * @return a formatted multi-line comparison message
     */
    private String formatComparison(String projectName, List<String> extracted, List<String> baseline) {
        StringBuilder msg = new StringBuilder();
        msg.append("Advisor output for project '").append(projectName)
                .append("' does not match baseline.\n");
        msg.append("--- Extracted output (").append(extracted.size()).append(" lines) ---\n");
        for (int i = 0; i < Math.min(300, extracted.size()); i++) {
            msg.append(String.format("%4d: %s\n", i + 1, extracted.get(i)));
        }
        if (extracted.size() > 300) {
            msg.append("... (").append(extracted.size() - 300).append(" more lines)\n");
        }
        msg.append("\n--- Baseline (").append(baseline.size()).append(" lines) ---\n");
        for (int i = 0; i < Math.min(300, baseline.size()); i++) {
            msg.append(String.format("%4d: %s\n", i + 1, baseline.get(i)));
        }
        if (baseline.size() > 300) {
            msg.append("... (").append(baseline.size() - 300).append(" more lines)\n");
        }
        return msg.toString();
    }
}

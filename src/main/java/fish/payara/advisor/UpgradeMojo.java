/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2026 Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://github.com/payara/Payara/blob/master/LICENSE.txt
 * See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * The Payara Foundation designates this particular file as subject to the "Classpath"
 * exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package fish.payara.advisor;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import fish.payara.tools.ai.lang.PayaraChatModel;
import fish.payara.tools.ai.source.ProjectSourceTools;
import fish.payara.tools.ai.upgrade.JakartaUpgradeTools;
import fish.payara.tools.ai.util.MavenInvoker;
import fish.payara.tools.ai.util.RunLogger;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Runs the Payara Jakarta EE Upgrade Advisor ({@code advise} goal) and uses an
 * AI agent to apply every recommendation to the project's source files, then
 * compiles to verify.
 *
 * <p>Repeats up to {@code maxRounds} times until the advisor reports no
 * remaining issues or the round limit is reached.
 *
 * <p>Usage:
 * <pre>
 *   mvn fish.payara.advisor:advisor-maven-plugin:upgrade
 * </pre>
 *
 * <p>LLM configuration (env vars or system properties):
 * <pre>
 *   PAYARA_AI_PROVIDER=ANTHROPIC   (or -Dpayara.ai.provider=ANTHROPIC)
 *   PAYARA_AI_API_KEY=sk-ant-...   (or -Dpayara.ai.api.key=...)
 *   PAYARA_AI_MODEL=claude-sonnet-4-6   (optional)
 * </pre>
 */
@Mojo(name = "upgrade", requiresProject = true)
public class UpgradeMojo extends AbstractMojo {

    interface UpgradeAgent {
        String chat(String message);
    }

    private static final String SYSTEM_PROMPT = """
            You are a Jakarta EE migration assistant. You apply recommendations from the
            Payara Jakarta EE Upgrade Advisor to Java source files by rewriting each
            affected file in full.

            ## Severity
            - [ERROR]   Application will break - must fix.
            - [WARNING] Deprecated API - must fix.
            - [INFO]    Optional/informational - only act if a concrete code change is
                        clearly warranted; skip pure FYI notes.

            ## Precision - follow the advisor exactly

            Each advisor entry names a specific element (annotation, import, method call,
            field, class, or constant) and a specific action (remove, replace with X,
            comment out, etc.). When fixing an entry you MUST:

              - Change the exact element the advisor named. If it points at an
                annotation, change that annotation - not the import for that annotation.
                If it points at an import, change that import. If it points at a method
                call, change that call.
              - Perform the exact action the advisor specified. If it says "comment
                out", comment that element out. If it says "replace with X", replace it
                with X. Do not substitute one action for another.
              - When the recommended replacement implies multiple coordinated edits
                (replacing a type usually requires updating both the import and every
                usage in the file), make ALL of them in the same pass. Removing an
                import without updating its usages, or updating a usage without
                fixing its import, are both incomplete fixes.

            ## Workflow

            For each source file mentioned in the advisor output:
              1. Call read_source_file to understand the current contents and line numbers.
                 Note: read_source_file prepends "N  " line-number prefixes - do NOT
                 include those prefixes in any file you write back.
              2. Decide whether a code change is actually needed. If the advisor entry is
                 purely informational and no edit is required, skip the file.
              3. Produce the complete corrected file: imports, annotations, and code
                 updated to satisfy every [ERROR] and [WARNING] for that file in one pass.
                 Mentally walk through each advisor entry for the file and confirm your
                 rewrite addresses the specific element it names with the specific action
                 it specifies.
              4. Call write_source_file with the complete corrected file content (no line
                 number prefixes). This overwrites the file in a single write.

            After all files have been rewritten, call compile_maven_project. If it fails,
            re-read the offending file, produce a new corrected version, and overwrite it
            again. Repeat until the build passes.

            ## Rules for the rewritten file
            - Include all needed imports at the top - no fully-qualified class names
              inline (e.g. write @Named, not @jakarta.inject.Named).
            - Remove obsolete imports and annotations entirely. Never comment them out.
            - When you remove an import, remove every usage of that type in the file too.
            - Do not add inline comments explaining the migration.
            - Preserve unrelated code, formatting, and Javadoc as-is.

            Report a brief summary at the end: each file changed and what was migrated.
            """;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File projectBaseDir;

    /** Advisor plugin version to invoke for the read-only analysis pass. */
    @Parameter(property = "payara.advisor.version", defaultValue = "2.1-SNAPSHOT")
    private String advisorVersion;

    /**
     * Target Jakarta EE version. Accepted values: {@code 10} or {@code 11}.
     * <ul>
     *   <li>{@code 11} (default) - runs the advisor for both EE 10 and EE 11
     *       and sets {@code jakarta.jakartaee-api} to {@code 11.0.0} in pom.xml.</li>
     *   <li>{@code 10} - runs the advisor for EE 10 only and sets the
     *       dependency to {@code 10.0.0}.</li>
     * </ul>
     * Override on the command line with {@code -DadvisorVersion=10}.
     */
    @Parameter(property = "advisorVersion", defaultValue = "11")
    private String targetJakartaVersion;

    /** Maximum advisor → apply → compile rounds before stopping. */
    @Parameter(property = "payara.advisor.maxRounds", defaultValue = "10")
    private int maxRounds;

    /**
     * When true, pause before each file write and ask the user to accept or
     * reject the change. Use {@code -Dpayara.ai.interactive=true} to enable.
     */
    @Parameter(property = "payara.ai.interactive", defaultValue = "false")
    private boolean interactive;

    private RunLogger runLogger;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String projectPath = projectBaseDir.getAbsolutePath();
        getLog().info("Payara AI Agent - Jakarta EE upgrade");
        getLog().info("  project  : " + projectPath);
        getLog().info("  advisor  : fish.payara.advisor:advisor-maven-plugin:" + advisorVersion + ":advise");
        getLog().info("  target   : Jakarta EE " + targetJakartaVersion);
        getLog().info("  maxRounds: " + maxRounds);

        PayaraChatModel chatModel;
        try {
            chatModel = new PayaraChatModel();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to initialise LLM: " + e.getMessage(), e);
        }
        if (!chatModel.isInitialized()) {
            throw new MojoExecutionException(
                    "LLM not configured. Set PAYARA_AI_PROVIDER and PAYARA_AI_API_KEY "
                    + "environment variables (or -Dpayara.ai.provider / -Dpayara.ai.api.key). "
                    + "Supported providers: OPEN_AI, ANTHROPIC, GOOGLE, MISTRAL, OLLAMA.");
        }

        if (interactive) {
            getLog().info("  interactive: true (you will be prompted to accept/reject each change)");
        }

        try {
            runLogger = new RunLogger(projectBaseDir.toPath());
            getLog().info("  log      : " + runLogger.getLogFile());
        } catch (Exception e) {
            getLog().warn("Could not create run log: " + e.getMessage());
            runLogger = null;
        }

        ProjectSourceTools sourceTools = new ProjectSourceTools(interactive, runLogger);
        UpgradeAgent agent = AiServices.builder(UpgradeAgent.class)
                .chatModel(chatModel.getChatModel())
                .tools(sourceTools, new JakartaUpgradeTools(runLogger))
                .chatMemory(MessageWindowChatMemory.withMaxMessages(40))
                .systemMessageProvider(id -> SYSTEM_PROMPT)
                .build();

        // Update pom.xml to the target EE version BEFORE the loop so every compile
        // step in the loop can resolve EE 11 APIs. Without this, the AI correctly
        // rewrites source files to use EE 11 APIs, the compile fails (EE 10 is still
        // on the classpath), and the AI reverts the changes to restore a green build -
        // wasting rounds and leaving deprecated APIs in place.
        String eeVer = (targetJakartaVersion == null || targetJakartaVersion.isBlank())
                ? "11" : targetJakartaVersion;
        boolean pomUpdatedBeforeLoop = false;
        try {
            pomUpdatedBeforeLoop = PomUpdater.updateJakartaEeVersion(
                    projectBaseDir.toPath().resolve("pom.xml"), eeVer + ".0.0");
            if (pomUpdatedBeforeLoop) {
                getLog().info("pom.xml: updated Jakarta EE version to " + eeVer + ".0.0");
            }
        } catch (Exception e) {
            getLog().warn("pom.xml: pre-update failed (" + e.getMessage()
                    + ") - AI will handle it in final step.");
        }

        boolean anyChanges = false;
        boolean abortedByUser = false;
        Set<String> previousItemKeys = null;
        List<AdvisorItem> lastNoFile = new ArrayList<>();

        loop:
        for (int round = 1; round <= maxRounds; round++) {
            if (runLogger != null) runLogger.logRound(round, maxRounds);
            getLog().info("");
            getLog().info("--- Round " + round + " / " + maxRounds + " ---");
            getLog().info("Running advisor...");

            AdvisorRunResult runResult = runAdvisorMerged();
            List<AdvisorItem> items = runResult.items;
            boolean structured = !items.isEmpty();

            getLog().info("Advisor returned " + runResult.totalBytes + " bytes"
                    + (runResult.versionCount > 1 ? " across " + runResult.versionCount + " versions" : "")
                    + "; parsed " + items.size() + " item(s)"
                    + (structured ? " (JSON mode)" : " (text fallback)") + ".");

            // No structured items - either advisor is too old or nothing left to do.
            if (!structured && !hasRecommendations(runResult.rawOutput)) {
                if (anyChanges) {
                    getLog().info("Advisor reports no remaining issues - proceeding to final step.");
                } else {
                    getLog().info("Advisor found nothing to change - proceeding to final step.");
                }
                break loop;
            }

            if (structured) {
                // Split file-based items from project-level items up front so stall
                // detection and prompts only operate on file-based items. Project-level
                // items (beans.xml, etc.) are deferred to the final step.
                Map<String, List<AdvisorItem>> byFile = new LinkedHashMap<>();
                List<AdvisorItem> noFile = new ArrayList<>();
                for (AdvisorItem it : items) {
                    String key = !it.filePath.isEmpty() ? it.filePath : it.file;
                    if (key.isEmpty() || "-".equals(key)) noFile.add(it);
                    else byFile.computeIfAbsent(key, k -> new ArrayList<>()).add(it);
                }
                lastNoFile = noFile;

                // No file-based items - only project-level items remain. Skip to final step.
                if (byFile.isEmpty()) {
                    getLog().info("No file-based items remaining - proceeding to final step.");
                    if (!noFile.isEmpty()) {
                        getLog().info("  Project-level items deferred to final step:");
                        for (AdvisorItem it : noFile) {
                            getLog().info("    [" + it.severity + "] " + it.spec
                                    + (it.message.isEmpty() ? "" : " - " + it.message));
                        }
                    }
                    break loop;
                }

                // Stall detection: keyed on file-based items only. Line numbers drift as
                // the agent edits code, so use file:spec (stable rule ID) as the key.
                Set<String> itemKeys = new HashSet<>();
                byFile.values().forEach(list -> list.forEach(it -> itemKeys.add(it.file + ":" + it.spec)));
                if (previousItemKeys != null && itemKeys.equals(previousItemKeys)) {
                    getLog().info("Advisor reports the same " + itemKeys.size()
                            + " file-based item(s) as last round - no progress made, stopping early.");
                    byFile.values().forEach(list -> list.forEach(it ->
                            getLog().info("  [unresolved] " + it.file + ":" + it.line
                                    + " - " + it.expression + ": " + it.message)));
                    break loop;
                }
                previousItemKeys = itemKeys;

                // Pre-flight summary.
                getLog().info("Asking AI agent to apply recommendations...");
                for (Map.Entry<String, List<AdvisorItem>> e : byFile.entrySet()) {
                    String fname = new File(e.getKey()).getName();
                    getLog().info("  " + fname + " - " + e.getValue().size() + " item(s):");
                    for (AdvisorItem it : e.getValue()) {
                        getLog().info("    [" + it.severity + "] line " + it.line
                                + " " + it.expressionKind + " " + it.expression
                                + (it.message.isEmpty() ? "" : " - " + it.message));
                    }
                }
                if (!noFile.isEmpty()) {
                    getLog().info("  (project-level) - " + noFile.size()
                            + " item(s) deferred to final step");
                }
                getLog().info("");

                // Per-file AI pass.
                int idx = 0;
                for (Map.Entry<String, List<AdvisorItem>> e : byFile.entrySet()) {
                    idx++;
                    getLog().info("  [" + idx + "/" + byFile.size() + "] "
                            + new File(e.getKey()).getName()
                            + " (" + e.getValue().size() + " item(s))");
                    try {
                        getLog().info(agent.chat(buildFilePrompt(e.getValue(), e.getKey(), projectPath)));
                    } catch (Exception ex) {
                        throw new MojoExecutionException(friendlyApiError(ex), ex);
                    }
                    if (sourceTools.isAborted()) {
                        getLog().info("Stopped by user request.");
                        abortedByUser = true;
                        break loop;
                    }
                }

                // Compile only - no beans.xml or pom.xml changes yet.
                try {
                    getLog().info(agent.chat(buildCompilePrompt(projectPath)));
                } catch (Exception e) {
                    throw new MojoExecutionException(friendlyApiError(e), e);
                }
                anyChanges = true;
            } else {
                getLog().warn("Advisor did not emit JSON - falling back to text parsing. "
                        + "Ensure the advisor plugin is version 2.1-SNAPSHOT or later.");
                getLog().info("Advisor output:\n" + runResult.rawOutput);
                getLog().info("Asking AI agent to apply recommendations...");
                String prompt = "The Jakarta EE Upgrade Advisor produced the following output for the "
                        + "project at '" + projectPath + "':\n\n"
                        + runResult.rawOutput
                        + "\n\nFor each [ERROR] and [WARNING] entry above, identify the exact element it "
                        + "names (annotation, import, method call, field, or class) and the exact action "
                        + "it specifies (remove, replace, comment out). Then rewrite each affected file "
                        + "so the named element is changed in the way the advisor described - do not "
                        + "substitute a different element or a different action. After rewriting, "
                        + "call compile_maven_project with project path '" + projectPath + "' to verify. "
                        + "Report a summary listing each advisor entry and what you changed for it.";
                String result;
                try {
                    result = agent.chat(prompt);
                } catch (Exception e) {
                    throw new MojoExecutionException(friendlyApiError(e), e);
                }
                getLog().info(result);
                anyChanges = true;
            }

            if (sourceTools.isAborted()) {
                abortedByUser = true;
                break loop;
            }
            if (round == maxRounds) {
                getLog().warn("Reached maxRounds=" + maxRounds
                        + " - proceeding to final step (re-run if advisor still reports issues).");
            }
        }

        // Final step: update pom.xml, create beans.xml if needed, compile.
        // Runs after the advisor loop regardless of how it exited (but not on user abort).
        if (!abortedByUser) {
            getLog().info("");
            getLog().info("--- Final step ---");

            // pom.xml: already updated before the loop if PomUpdater succeeded.
            // Try again here only when the pre-loop update didn't happen (artifact not found,
            // or pom.xml update failed), so the AI gets a chance to handle it.
            boolean pomUpdated = pomUpdatedBeforeLoop;
            if (!pomUpdated) {
                try {
                    pomUpdated = PomUpdater.updateJakartaEeVersion(
                            projectBaseDir.toPath().resolve("pom.xml"), eeVer + ".0.0");
                    if (pomUpdated) {
                        getLog().info("pom.xml: updated Jakarta EE version to " + eeVer + ".0.0");
                    } else {
                        getLog().info("pom.xml: Jakarta EE dependency not found - AI will handle it.");
                    }
                } catch (Exception e) {
                    getLog().warn("pom.xml: programmatic update failed (" + e.getMessage()
                            + ") - AI will handle it.");
                }
            }

            // Try programmatic beans.xml creation before delegating to AI.
            List<AdvisorItem> remainingNoFile = new ArrayList<>(lastNoFile);
            Iterator<AdvisorItem> iter = remainingNoFile.iterator();
            while (iter.hasNext()) {
                AdvisorItem item = iter.next();
                if (BeansXmlCreator.BEANS_XML_SPEC.equals(item.spec)) {
                    try {
                        boolean created = BeansXmlCreator.create(projectBaseDir.toPath());
                        if (created) {
                            getLog().info("beans.xml: created at src/main/webapp/WEB-INF/beans.xml");
                        } else {
                            getLog().info("beans.xml: already exists - skipping.");
                        }
                        iter.remove();
                    } catch (Exception e) {
                        getLog().warn("beans.xml: programmatic creation failed (" + e.getMessage()
                                + ") - AI will handle it.");
                    }
                }
            }

            try {
                getLog().info(agent.chat(buildFinalPrompt(remainingNoFile, projectPath,
                        targetJakartaVersion, pomUpdated)));
            } catch (Exception e) {
                throw new MojoExecutionException(friendlyApiError(e), e);
            }
        } else {
            if (runLogger != null) runLogger.log("Stopped by user request.");
        }

        if (runLogger != null) runLogger.close();
    }

    // -------------------------------------------------------------------------
    // Advisor execution
    // -------------------------------------------------------------------------

    private static final class AdvisorRunResult {
        final List<AdvisorItem> items;
        final int totalBytes;
        final int versionCount;
        final String rawOutput;

        AdvisorRunResult(List<AdvisorItem> items, int totalBytes, int versionCount, String rawOutput) {
            this.items = items;
            this.totalBytes = totalBytes;
            this.versionCount = versionCount;
            this.rawOutput = rawOutput;
        }
    }

    private AdvisorRunResult runAdvisorMerged() {
        String goal = "fish.payara.advisor:advisor-maven-plugin:" + advisorVersion + ":advise";
        List<String> versions = "10".equals(targetJakartaVersion) ? List.of("10") : List.of("10", "11");
        Map<String, AdvisorItem> merged = new LinkedHashMap<>();
        StringBuilder rawConcat = new StringBuilder();
        int totalBytes = 0;
        for (String v : versions) {
            getLog().info("  Running advisor for Jakarta EE " + v + "...");
            String output = MavenInvoker.run(projectBaseDir.toPath(), goal,
                    "-Dformat=json",
                    "-DadviseVersion=" + v);
            String json = extractJsonObject(output);
            if (runLogger != null) runLogger.logAdvisorJson(v, json != null ? json : output);
            List<AdvisorItem> vItems = parseAdvisorJson(output);
            getLog().info("    EE " + v + ": " + vItems.size() + " item(s)");
            for (AdvisorItem it : vItems) {
                String key = it.file + ":" + it.line + ":" + it.expression;
                AdvisorItem existing = merged.get(key);
                merged.put(key, existing == null ? it : new AdvisorItem(existing, it));
            }
            rawConcat.append(output);
            totalBytes += output.length();
        }
        List<AdvisorItem> items = new ArrayList<>(merged.values());
        if (runLogger != null) runLogger.logMergedJson(toMergedJson(items));
        return new AdvisorRunResult(items, totalBytes, versions.size(), rawConcat.toString());
    }

    // -------------------------------------------------------------------------
    // AdvisorItem - parsed from the advisor's JSON output
    // -------------------------------------------------------------------------

    static final class AdvisorItem {
        final String severity;
        final String file;
        final String filePath;
        final String line;
        final String expression;
        final String expressionKind;
        final String spec;
        final String message;
        final String fix;

        AdvisorItem(JSONObject o) {
            this.severity       = o.optString("severity", "INFO");
            this.file           = o.optString("file", "");
            this.filePath       = o.optString("filePath", "");
            this.line           = o.optString("line", "");
            this.expression     = o.optString("expression", "");
            this.expressionKind = o.optString("expressionKind", "other");
            this.spec           = o.optString("spec", "");
            this.message        = o.optString("message", "");
            this.fix            = o.optString("fix", "");
        }

        /** Field-level merge: newer version wins for spec/severity; falls back to base for message/fix when newer has empty strings. */
        AdvisorItem(AdvisorItem base, AdvisorItem newer) {
            this.severity       = newer.severity.isEmpty()       ? base.severity       : newer.severity;
            this.file           = base.file;
            this.filePath       = base.filePath;
            this.line           = base.line;
            this.expression     = base.expression;
            this.expressionKind = newer.expressionKind.isEmpty() ? base.expressionKind : newer.expressionKind;
            this.spec           = newer.spec.isEmpty()           ? base.spec           : newer.spec;
            this.message        = newer.message.isEmpty()        ? base.message        : newer.message;
            this.fix            = newer.fix.isEmpty()            ? base.fix            : newer.fix;
        }
    }

    static List<AdvisorItem> parseAdvisorJson(String advisorOutput) {
        List<AdvisorItem> items = new ArrayList<>();
        String json = extractJsonObject(advisorOutput);
        if (json == null) return items;
        try {
            JSONObject root = new JSONObject(json);
            JSONArray arr = root.optJSONArray("items");
            if (arr == null) return items;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.optJSONObject(i);
                if (obj != null) items.add(new AdvisorItem(obj));
            }
        } catch (Exception ignored) {
        }
        return items;
    }

    static String extractJsonObject(String text) {
        int start = text.indexOf('{');
        if (start < 0) return null;
        int depth = 0;
        boolean inString = false, escape = false;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escape) { escape = false; continue; }
            if (inString) {
                if (c == '\\') escape = true;
                else if (c == '"') inString = false;
                continue;
            }
            if (c == '"') { inString = true; continue; }
            if (c == '{') depth++;
            else if (c == '}') { if (--depth == 0) return text.substring(start, i + 1); }
        }
        return null;
    }

    private static String toMergedJson(List<AdvisorItem> items) {
        StringBuilder sb = new StringBuilder("{\"items\":[");
        for (int i = 0; i < items.size(); i++) {
            AdvisorItem it = items.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"severity\":\"").append(jsonEsc(it.severity)).append('"')
              .append(",\"file\":\"").append(jsonEsc(it.file)).append('"')
              .append(",\"filePath\":\"").append(jsonEsc(it.filePath)).append('"')
              .append(",\"line\":\"").append(jsonEsc(it.line)).append('"')
              .append(",\"expression\":\"").append(jsonEsc(it.expression)).append('"')
              .append(",\"expressionKind\":\"").append(jsonEsc(it.expressionKind)).append('"')
              .append(",\"spec\":\"").append(jsonEsc(it.spec)).append('"')
              .append(",\"message\":\"").append(jsonEsc(it.message)).append('"')
              .append(",\"fix\":\"").append(jsonEsc(it.fix)).append("\"}");
        }
        return sb.append("]}").toString();
    }

    private static String jsonEsc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private static boolean hasRecommendations(String advisorOutput) {
        return advisorOutput.contains("Line of code:")
                || advisorOutput.contains("Expression:")
                || advisorOutput.contains("[ADVICE]")
                || advisorOutput.contains("Advice:");
    }

    // -------------------------------------------------------------------------
    // Prompt builders
    // -------------------------------------------------------------------------

    static String buildFilePrompt(List<AdvisorItem> fileItems, String filePath, String projectPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("Fix the following ").append(fileItems.size()).append(" advisor item(s) in:\n\n")
          .append("FILE: ").append(filePath).append('\n');
        for (AdvisorItem it : fileItems) {
            sb.append("  - [").append(it.severity).append("] line ").append(it.line).append(": ")
              .append(it.expressionKind).append(' ').append(it.expression).append('\n');
            sb.append("    spec : ").append(it.spec).append('\n');
            if (!it.message.isEmpty()) sb.append("    issue: ").append(it.message).append('\n');
            if (!it.fix.isEmpty())     sb.append("    fix  : ").append(it.fix).append('\n');
        }
        sb.append("\nRead the file, apply every fix above, and write the complete corrected version. ")
          .append("Do NOT call compile_maven_project - more files will follow. ")
          .append("Report a one-line summary of what you changed.");
        return sb.toString();
    }

    /** Per-round compile prompt - source edits only, no infrastructure changes. */
    static String buildCompilePrompt(String projectPath) {
        return "Call compile_maven_project with project path '" + projectPath + "'. "
                + "If it fails, re-read the offending file(s), fix the error(s), and compile again "
                + "until the build passes. Report a one-line summary of what was fixed.";
    }

    /**
     * Final-step prompt: handle any project-level advisor items (e.g. missing beans.xml),
     * update the Jakarta EE dependency version in pom.xml, then compile.
     * Called once after the advisor loop is done.
     */
    /**
     * @param pomAlreadyUpdated pass {@code true} when {@link PomUpdater} already
     *                          updated pom.xml - the pom.xml section is then omitted
     *                          from the prompt so the AI doesn't redo it.
     */
    static String buildFinalPrompt(List<AdvisorItem> projectItems, String projectPath,
                                   String targetEeVersion, boolean pomAlreadyUpdated) {
        StringBuilder sb = new StringBuilder();

        // 1. pom.xml version update - skipped when PomUpdater already handled it.
        if (!pomAlreadyUpdated) {
            String eeVersion = (targetEeVersion == null || targetEeVersion.isBlank()) ? "11" : targetEeVersion;
            sb.append("Update the Jakarta EE dependency version in pom.xml:\n")
              .append("  Read pom.xml, find the version of jakarta.platform:jakarta.jakartaee-api\n")
              .append("  (or the equivalent BOM such as jakarta.platform:jakarta.jakartaee-bom),\n")
              .append("  and set it to ").append(eeVersion).append(".0.0.\n")
              .append("  Use write_source_file to write the complete corrected pom.xml back.\n\n");
        }

        // 2. Project-level advisor items (e.g. missing beans.xml).
        boolean hasBeans = false;
        if (!projectItems.isEmpty()) {
            sb.append(pomAlreadyUpdated ? "Handle" : "Then handle")
              .append(" these project-level advisor items:\n");
            for (AdvisorItem it : projectItems) {
                sb.append("  - [").append(it.severity).append("] ").append(it.spec).append('\n');
                if (!it.message.isEmpty()) sb.append("    issue: ").append(it.message).append('\n');
                if (!it.fix.isEmpty())     sb.append("    fix  : ").append(it.fix).append('\n');
                if ("jakarta-cdi-file-not-found-beans-xml".equals(it.spec)) hasBeans = true;
            }
            sb.append('\n');
        }
        if (hasBeans) {
            sb.append("Create src/main/webapp/WEB-INF/beans.xml using write_source_file with ")
              .append("this exact XML as content:\n\n")
              .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
              .append("<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\"\n")
              .append("       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n")
              .append("       xsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee ")
              .append("https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd\"\n")
              .append("       version=\"4.0\" bean-discovery-mode=\"annotated\"/>\n\n");
        }

        // 3. Compile to verify everything together.
        String prefix = (pomAlreadyUpdated && projectItems.isEmpty()) ? "Call" : "Then call";
        sb.append(prefix).append(" compile_maven_project with project path '")
          .append(projectPath).append("'. ")
          .append("If it fails, re-read the offending file(s), fix the error(s), and compile again until the build passes. ")
          .append("Report a brief summary of every change made in this final step.");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Error formatting
    // -------------------------------------------------------------------------

    private static String friendlyApiError(Exception e) {
        String raw = e.getMessage();
        if (raw == null) return "AI request failed - no details available.";
        try {
            int start = raw.indexOf('{');
            if (start >= 0) {
                JSONObject root = new JSONObject(raw.substring(start));
                JSONObject error = root.optJSONObject("error");
                if (error != null) {
                    String msg = error.optString("message", null);
                    String code = error.optString("code", null);
                    int status = root.optInt("status", -1);
                    if ("invalid_api_key".equals(code) || (msg != null && msg.toLowerCase().contains("incorrect api key"))) {
                        return "Invalid API key - check PAYARA_AI_API_KEY (or -Dpayara.ai.api.key). "
                                + "You can find or create your key in the provider's dashboard.";
                    }
                    if (status == 429) return "Rate limit exceeded (HTTP 429) - wait a moment and retry, or check your usage quota.";
                    if (status == 403) return "Access denied (HTTP 403) - your API key may not have permission for the selected model.";
                    if (msg != null && !msg.isEmpty()) return "AI request failed: " + msg;
                }
            }
        } catch (Exception ignored) {
        }
        return "AI request failed: " + raw;
    }
}

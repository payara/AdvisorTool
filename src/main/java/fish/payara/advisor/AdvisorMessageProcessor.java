/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2023-2024 Payara Foundation and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import org.apache.maven.plugin.logging.Log;

public class AdvisorMessageProcessor {

    public void updateLogSeverityForMessages(List<AdvisorBean> advisorMethodBeanList) {
        advisorMethodBeanList.forEach(b -> {
            String logSeverity = b.getKeyPattern().contains("info") ? "info" : (
                    b.getKeyPattern().contains("warn") ? "warn" : (b.getKeyPattern().contains("error") ? "error" : "")
            );
            if(!logSeverity.isEmpty()) {
                b.setType(AdvisorType.valueOf(logSeverity.toUpperCase()));
                b.setKeyPattern(b.getKeyPattern().substring(0, b.getKeyPattern().indexOf(logSeverity) - 1));
            }
        });
    }

    public void addMessages(List<AdvisorBean> advisorMethodBeanList, String adviseVersion) {
        addMessages("config/jakarta" + adviseVersion + "/advisorMessages", advisorMethodBeanList, "message");
        addMessages("config/jakarta" + adviseVersion + "/advisorFix", advisorMethodBeanList, "fix");
    }

    protected void addMessages(String url, List<AdvisorBean> advisorMethodBeanList, String type) {
        advisorMethodBeanList.forEach(b -> {
            URI baseMessageFolder = null;
            try {
                baseMessageFolder = AdvisorToolMojo.class.getClassLoader().getResource(url).toURI();
                Path internalPath = null;
                if (baseMessageFolder.getScheme().equals("jar")) {
                    FileSystem fileSystem = FileSystems.getFileSystem(baseMessageFolder);
                    internalPath = fileSystem.getPath(url);
                } else {
                    internalPath = Paths.get(baseMessageFolder);
                }
                findMessage(internalPath, b.getKeyPattern(), type, b);
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void findMessage(Path internalPath, String keyPattern, String type, AdvisorBean b) throws IOException {
        String fileMessageName = null;
        String fileFix = null;
        String keyIssue = null;
        Properties messageProperties = new Properties();
        String subSpec = keyPattern.contains("-interface") ? "-interface" : (keyPattern.contains("-method") ? "-method" : (
                keyPattern.contains("-field") ? "-field" : (keyPattern.contains("-remove") ? "-remove" : (keyPattern.contains("-file") ? "-file": (
                        keyPattern.contains("-namespace") ? "-namespace" : "-tag")))));
        String spec = keyPattern.substring(0, keyPattern.indexOf(subSpec));
        if(type.equals("message")) {
            fileMessageName = spec + "-messages.properties";
        }
        if(type.equals("fix")) {
            fileFix = spec + "-fix-messages.properties";
        }

        if (keyPattern.contains("issue")) {
            keyIssue = spec + keyPattern.substring(keyPattern.indexOf("-issue"));
        }

        if (internalPath != null) {
            Stream<Path> walk = Files.walk(internalPath, 1);
            for (Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
                Path p = it.next();
                if (type.equals("message") &&
                        p.getFileName().toString().contains(fileMessageName)) {
                    messageProperties = readProperties(messageProperties, p);
                }
                if (type.equals("fix") && p.getFileName().toString().contains(fileFix)) {
                    messageProperties = readProperties(messageProperties, p);
                }
            }
        }
        AdvisorMessage advisorMessage = null;
        if(b.getAdvisorMessage() == null) {
            advisorMessage = new AdvisorMessage.AdvisorMessageBuilder().build();
        } else {
            advisorMessage = b.getAdvisorMessage();
        }

        if(type.equals("message")) {
            String message = keyIssue != null ?
                    messageProperties.getProperty(keyIssue) : messageProperties.getProperty(keyPattern);
            advisorMessage.setMessage(message);
        }

        if(type.equals("fix")) {
            String fix = keyIssue != null ?
                    messageProperties.getProperty(keyIssue) : messageProperties.getProperty(keyPattern);
            advisorMessage.setFix(fix);
        }

        if(b.getType() != null) {
            advisorMessage.setType(b.getType());
        }

        b.setAdvisorMessage(advisorMessage);
    }
    
    protected Properties readProperties(Properties messageProperties, Path p) throws IOException {
        try(InputStream stream = AdvisorMessageProcessor.class.getClassLoader().getResourceAsStream(p.toString())) {
            if(stream == null) {
                File f = p.toFile();
                FileInputStream fileInputStream = new FileInputStream(f);
                messageProperties.load(fileInputStream);
            } else {
                messageProperties.load(stream);
            }
        }
        return messageProperties;
    }

    public void printToConsole(List<AdvisorBean> advisorMethodBeanList, Log log) {
        log.info("Showing Advisories");
        log.info("***************");
        advisorMethodBeanList.forEach(b -> {
            if(b.getType() != null) {
                switch (b.getType()) {
                    case INFO:
                        log.info(b.toString());
                        break;
                    case WARN:
                        log.warn(b.toString());
                        break;
                    case ERROR:
                        log.error(b.toString());
                        break;
                    default:
                        break;
                }
            } else {
                log.info(b.toString());
            }
        });
    }

    /**
     * Emit advisor results as a JSON document. The output is a single object with
     * an {@code adviseVersion} field and an {@code items} array. Each item carries
     * the structured fields a downstream tool needs to address it precisely:
     * severity, file, line, expression, expressionKind, spec, message, fix.
     */
    public void printToJson(List<AdvisorBean> advisorMethodBeanList, String adviseVersion, Log log) {
        StringBuilder sb = new StringBuilder(256 + 128 * advisorMethodBeanList.size());
        sb.append("{\n");
        sb.append("  \"adviseVersion\": \"").append(jsonEscape(adviseVersion)).append("\",\n");
        sb.append("  \"items\": [");
        for (int i = 0; i < advisorMethodBeanList.size(); i++) {
            AdvisorBean b = advisorMethodBeanList.get(i);
            String severity = b.getType() != null ? b.getType().name() : "INFO";
            String file = b.getFile() != null ? b.getFile().getName() : "";
            String filePath = b.getFile() != null ? b.getFile().getAbsolutePath() : "";
            String line = b.getLine() != null ? b.getLine() : "";
            String expression = expressionOf(b);
            String expressionKind = expressionKindOf(b);
            String spec = b.getKeyPattern() != null ? b.getKeyPattern() : "";
            String message = b.getAdvisorMessage() != null && b.getAdvisorMessage().getMessage() != null
                    ? b.getAdvisorMessage().getMessage().trim() : "";
            String fix = b.getAdvisorMessage() != null && b.getAdvisorMessage().getFix() != null
                    ? b.getAdvisorMessage().getFix().trim() : "";

            sb.append(i == 0 ? "\n" : ",\n");
            sb.append("    {\n");
            sb.append("      \"severity\": \"").append(jsonEscape(severity)).append("\",\n");
            sb.append("      \"file\": \"").append(jsonEscape(file)).append("\",\n");
            sb.append("      \"filePath\": \"").append(jsonEscape(filePath)).append("\",\n");
            sb.append("      \"line\": \"").append(jsonEscape(line)).append("\",\n");
            sb.append("      \"expression\": \"").append(jsonEscape(expression)).append("\",\n");
            sb.append("      \"expressionKind\": \"").append(jsonEscape(expressionKind)).append("\",\n");
            sb.append("      \"spec\": \"").append(jsonEscape(spec)).append("\",\n");
            sb.append("      \"message\": \"").append(jsonEscape(message)).append("\",\n");
            sb.append("      \"fix\": \"").append(jsonEscape(fix)).append("\"\n");
            sb.append("    }");
        }
        sb.append(advisorMethodBeanList.isEmpty() ? "]\n" : "\n  ]\n");
        sb.append("}");
        // Plain System.out so the document is captured cleanly by any consumer
        // parsing stdout - the Maven [INFO] prefix would corrupt the JSON.
        System.out.println(sb);
    }

    private static String expressionOf(AdvisorBean b) {
        if (b.getMethodDeclaration() != null) return b.getMethodDeclaration();
        if (b.getAnnotationDeclaration() != null) return b.getAnnotationDeclaration();
        return b.getImportDeclaration() != null ? b.getImportDeclaration() : "";
    }

    private static String expressionKindOf(AdvisorBean b) {
        if (b.getMethodDeclaration() != null) return "method";
        if (b.getAnnotationDeclaration() != null) return "annotation";
        if (b.getImportDeclaration() != null) return "import";
        return "other";
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }
}

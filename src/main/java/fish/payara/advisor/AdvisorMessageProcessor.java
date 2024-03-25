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
        String subSpec = keyPattern.contains("interface") ? "interface" : (keyPattern.contains("method") ? "method" : (
                keyPattern.contains("remove") ? "remove" : (keyPattern.contains("file") ? "file": (
                        keyPattern.contains("namespace") ? "namespace" : "tag"))));
        String spec = keyPattern.substring(0, keyPattern.indexOf(subSpec));
        if(type.equals("message")) {
            fileMessageName = spec + "messages.properties";
        }
        if(type.equals("fix")) {
            fileFix = spec + "fix-messages.properties";
        }

        if (keyPattern.contains("issue")) {
            keyIssue = spec + keyPattern.substring(keyPattern.indexOf("issue"));
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
}

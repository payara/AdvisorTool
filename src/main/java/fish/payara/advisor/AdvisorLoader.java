/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2023-2025 Payara Foundation and/or its affiliates. All rights reserved.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.project.MavenProject;

public class AdvisorLoader {

    private static final Logger log = Logger.getLogger(AdvisorLoader.class.getName());

    public Properties loadPatterns(String version) throws URISyntaxException, IOException {
        //validate configurations
        if (AdvisorToolMojo.class.getClassLoader().getResource("config/jakarta" + version) == null) {
            log.severe(String.format("Not available configurations for the indicated version %s", version));
            return null;
        }
        URI uriBaseFolder = Objects.requireNonNull(AdvisorToolMojo.class.getClassLoader().getResource(
                "config/jakarta" + version + "/mappedPatterns")).toURI();
        Properties readPatterns = new Properties();
        Path internalPath = null;
        if (uriBaseFolder.getScheme().equals("jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(uriBaseFolder, Collections.<String, Object>emptyMap());
            internalPath = fileSystem.getPath("config/jakarta" + version + "/mappedPatterns");
        } else {
            internalPath = Paths.get(uriBaseFolder);
        }
        Stream<Path> walk = Files.walk(internalPath, 1);
        for (Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
            Path p = it.next();
            if (p.getFileName().toString().contains(".properties")) {
                try (InputStream stream = AdvisorToolMojo.class.getClassLoader().getResourceAsStream(p.toString())) {
                    if (stream == null) {
                        File f = p.toFile();
                        FileInputStream fileInputStream = new FileInputStream(f);
                        readPatterns.load(fileInputStream);
                    } else {
                        readPatterns.load(stream);
                    }
                }
            }
        }
        return readPatterns;
    }

    public List<File> loadSourceFiles(File baseDir) throws IOException {
        List<File> javaFiles = new ArrayList<>();
        if (baseDir != null) {
            javaFiles = Files.walk(Paths.get(baseDir.toURI()))
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.toString().contains(File.separator + "target" + File.separator))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
        return javaFiles;
    }

    public List<File> loadJSPandJSFFiles(File baseDir) throws IOException {
        List<File> jspFiles = new ArrayList<>();
        if (baseDir != null) {
            jspFiles = Files.walk(Paths.get(baseDir.toURI()))
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".jsp") || p.toString().endsWith(".xhtml"))
                    .filter(p -> !p.toString().contains(File.separator + "target" + File.separator))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
        return jspFiles;
    }

    public List<File> loadConfigFiles(File baseDir) throws IOException {
        List<File> configFiles = new ArrayList<>();
        if (baseDir != null) {
            configFiles = Files.walk(Paths.get(baseDir.toURI()))
                    .filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".xml")
                            || p.toString().endsWith(".properties"))
                    .filter(p -> !p.toString().contains(File.separator + "target" + File.separator))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
        return configFiles;
    }

}

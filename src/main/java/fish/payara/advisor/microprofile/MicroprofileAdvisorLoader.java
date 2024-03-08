/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2024 Payara Foundation and/or its affiliates. All rights reserved.
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
package fish.payara.advisor.microprofile;

import fish.payara.advisor.AdvisorLoader;
import fish.payara.advisor.AdvisorToolMojo;

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
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MicroprofileAdvisorLoader extends AdvisorLoader {

    private static final Logger log = Logger.getLogger(MicroprofileAdvisorLoader.class.getName());

    public Properties loadPatterns(String version) throws URISyntaxException, IOException {
        //validate configurations
        if(MicroprofileAdvisorToolMojo.class.getClassLoader().getResource("microprofile/v"+version) == null) {
            log.severe(String.format("Not available configurations for the indicated version %s", version));
            return null;
        }
        URI uriBaseFolder = Objects.requireNonNull(MicroprofileAdvisorToolMojo.class.getClassLoader().getResource(
                "microprofile/v" + version + "/mappedPatterns")).toURI();
        Properties readPatterns = new Properties();
        Path internalPath = null;
        if(uriBaseFolder.getScheme().equals("jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(uriBaseFolder, Collections.<String, Object>emptyMap());
            internalPath = fileSystem.getPath("microprofile/v" + version + "/mappedPatterns");
        } else {
            internalPath = Paths.get(uriBaseFolder);
        }
        Stream<Path> walk = Files.walk(internalPath, 1);
        for (Iterator<Path> it = walk.iterator(); it.hasNext();){
            Path path = it.next();
            if(path.getFileName().toString().contains(".properties")) {
                try(InputStream stream = MicroprofileAdvisorToolMojo.class.getClassLoader().getResourceAsStream(path.toString())) {
                    if(stream == null) {
                        File file = path.toFile();
                        FileInputStream fileInputStream = new FileInputStream(file);
                        readPatterns.load(fileInputStream);
                    } else {
                        readPatterns.load(stream);
                    }
                }
            }
        }
        return readPatterns;
    }
}

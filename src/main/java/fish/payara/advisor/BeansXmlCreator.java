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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Programmatically creates a Jakarta EE 10/11 {@code beans.xml} when the
 * Payara Upgrade Advisor reports {@code jakarta-cdi-file-not-found-beans-xml}.
 *
 * <p>The file is written to {@code src/main/webapp/WEB-INF/beans.xml} relative
 * to the project root. Parent directories are created if they do not exist.
 * If the file already exists and is non-empty it is left untouched.
 */
public class BeansXmlCreator {

    static final String BEANS_XML_SPEC = "jakarta-cdi-file-not-found-beans-xml";

    private static final String BEANS_XML_CONTENT =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\"\n"
            + "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "       xsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee"
            + " https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd\"\n"
            + "       version=\"4.0\" bean-discovery-mode=\"annotated\"/>\n";

    private BeansXmlCreator() {}

    /**
     * Creates {@code src/main/webapp/WEB-INF/beans.xml} under {@code projectRoot}.
     *
     * @param projectRoot absolute path to the Maven project root
     * @return {@code true} if the file was created;
     *         {@code false} if it already exists and is non-empty (no-op)
     * @throws IOException if the file cannot be created
     */
    public static boolean create(Path projectRoot) throws IOException {
        Path beansXml = projectRoot.resolve("src/main/webapp/WEB-INF/beans.xml");
        if (Files.exists(beansXml) && Files.size(beansXml) > 0) {
            return false;
        }
        Files.createDirectories(beansXml.getParent());
        Files.writeString(beansXml, BEANS_XML_CONTENT, StandardCharsets.UTF_8);
        return true;
    }

    /** Returns the canonical content written by {@link #create}. */
    static String content() {
        return BEANS_XML_CONTENT;
    }
}

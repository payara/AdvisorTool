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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Programmatically updates the Jakarta EE API version in a Maven pom.xml.
 *
 * <p>Uses DOM to locate the version value (direct literal or a {@code ${property}}
 * reference), then writes back using targeted string replacement so the original
 * file formatting, comments, and whitespace are fully preserved.
 *
 * <p>Returns {@code false} whenever the artifact cannot be found - the caller
 * should fall back to asking the AI agent to handle the update instead.
 */
public class PomUpdater {

    private static final List<String> JAKARTAEE_ARTIFACTS = List.of(
            "jakarta.jakartaee-api",
            "jakarta.jakartaee-bom",
            "jakarta.jakartaee-web-api",
            "jakarta.jakartaee-core-api"
    );

    private PomUpdater() {}

    /**
     * Attempts to set the Jakarta EE dependency version in the given pom.xml.
     *
     * @param pomXml     path to the pom.xml file
     * @param newVersion target version string, e.g. {@code "11.0.0"}
     * @return {@code true} if the version was found and the file was updated;
     *         {@code false} if the artifact was not found (caller should fall back to AI)
     * @throws Exception if the file cannot be read, parsed, or written
     */
    public static boolean updateJakartaEeVersion(Path pomXml, String newVersion) throws Exception {
        String content = Files.readString(pomXml, StandardCharsets.UTF_8);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        // Suppress external DTD/schema fetches - pom.xml doesn't need them.
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(pomXml.toFile());
        doc.getDocumentElement().normalize();

        String versionText = findJakartaEeVersionText(doc);
        if (versionText == null || versionText.isBlank()) {
            return false;
        }

        String updatedContent;
        if (versionText.startsWith("${") && versionText.endsWith("}")) {
            // Version is a property reference - update the property value.
            String propertyKey = versionText.substring(2, versionText.length() - 1);
            String oldValue = findPropertyValue(doc, propertyKey);
            if (oldValue == null) return false;
            updatedContent = replacePropertyValue(content, propertyKey, oldValue, newVersion);
        } else {
            // Direct version literal - replace it within the dependency block.
            updatedContent = replaceVersionInDependencyBlock(content, versionText, newVersion);
        }

        if (updatedContent == null || updatedContent.equals(content)) {
            return false;
        }

        Files.writeString(pomXml, updatedContent, StandardCharsets.UTF_8);
        return true;
    }

    // -------------------------------------------------------------------------
    // DOM helpers
    // -------------------------------------------------------------------------

    private static String findJakartaEeVersionText(Document doc) {
        NodeList deps = doc.getElementsByTagName("dependency");
        for (int i = 0; i < deps.getLength(); i++) {
            if (!(deps.item(i) instanceof Element dep)) continue;
            String groupId   = childText(dep, "groupId");
            String artifactId = childText(dep, "artifactId");
            if ("jakarta.platform".equals(groupId) && JAKARTAEE_ARTIFACTS.contains(artifactId)) {
                String version = childText(dep, "version");
                if (version != null && !version.isBlank()) return version.trim();
            }
        }
        return null;
    }

    private static String findPropertyValue(Document doc, String key) {
        NodeList propBlocks = doc.getElementsByTagName("properties");
        for (int i = 0; i < propBlocks.getLength(); i++) {
            if (!(propBlocks.item(i) instanceof Element props)) continue;
            NodeList children = props.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                if (children.item(j) instanceof Element e && key.equals(e.getTagName())) {
                    return e.getTextContent().trim();
                }
            }
        }
        return null;
    }

    private static String childText(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        return nl.getLength() == 0 ? null : nl.item(0).getTextContent();
    }

    // -------------------------------------------------------------------------
    // String-replacement helpers (preserve original formatting)
    // -------------------------------------------------------------------------

    /**
     * Replaces {@code <key>oldValue</key>} with {@code <key>newValue</key>}.
     * Property keys are unique in a valid pom.xml so a single-occurrence
     * replacement is safe.
     */
    private static String replacePropertyValue(String content, String key,
                                               String oldValue, String newValue) {
        String oldTag = "<" + key + ">" + oldValue + "</" + key + ">";
        String newTag = "<" + key + ">" + newValue + "</" + key + ">";
        int idx = content.indexOf(oldTag);
        if (idx < 0) return null;
        return content.substring(0, idx) + newTag + content.substring(idx + oldTag.length());
    }

    /**
     * Locates the {@code <groupId>jakarta.platform</groupId>} marker in the raw
     * content, then replaces {@code <version>oldVersion</version>} within the
     * surrounding 600-char window - enough to cover the enclosing
     * {@code <dependency>} block regardless of element ordering. Operating on the
     * raw string preserves all whitespace, indentation, and XML comments.
     */
    private static String replaceVersionInDependencyBlock(String content,
                                                          String oldVersion,
                                                          String newVersion) {
        String groupIdMarker = "<groupId>jakarta.platform</groupId>";
        int gidIdx = content.indexOf(groupIdMarker);
        if (gidIdx < 0) return null;

        int windowStart = Math.max(0, gidIdx - 200);
        int windowEnd   = Math.min(content.length(), gidIdx + 400);
        String window   = content.substring(windowStart, windowEnd);

        String oldTag = "<version>" + oldVersion + "</version>";
        String newTag = "<version>" + newVersion + "</version>";
        int vIdx = window.indexOf(oldTag);
        if (vIdx < 0) return null;

        String updated = window.substring(0, vIdx) + newTag
                + window.substring(vIdx + oldTag.length());
        return content.substring(0, windowStart) + updated + content.substring(windowEnd);
    }
}

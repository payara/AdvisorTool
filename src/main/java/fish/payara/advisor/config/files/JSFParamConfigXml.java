/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2025 Payara Foundation and/or its affiliates. All rights reserved.
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
package fish.payara.advisor.config.files;

import fish.payara.advisor.AdvisorBean;
import fish.payara.advisor.AdvisorType;
import fish.payara.advisor.Analyzer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class JSFParamConfigXml implements Analyzer<List<AdvisorBean>> {

    private List<String> keyPatterns = new ArrayList<>();

    public JSFParamConfigXml() {
        keyPatterns.add("jakarta-faces-remove-property-fss1-xml");
        keyPatterns.add("jakarta-faces-remove-property-fss2-xml");
    }

    public JSFParamConfigXml(List<String> keyPatterns) {
        this.keyPatterns = keyPatterns;
    }


    @Override
    public List<AdvisorBean> analise(File file) {
        List<AdvisorBean> advisors = new ArrayList<>();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = null;
        try {
            saxParser = factory.newSAXParser();
            JSFContextPropertiesHandler handler = new JSFContextPropertiesHandler();
            saxParser.parse(file, handler);
            ArrayList<JSFProperty> jsfProperties = handler.getJsfProperties();
            for (JSFProperty jsfProperty : jsfProperties) {
                int idKeyPattern = 0;
                if (jsfProperty.value().equals("jakarta.faces.FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME")) {
                    idKeyPattern = 1;
                }

                AdvisorBean advisorFileBean = new AdvisorBean.
                        AdvisorBeanBuilder(keyPatterns.get(idKeyPattern), jsfProperty.value()).
                        setFile(file).
                        setLine("0").
                        setType(AdvisorType.WARN).
                        setMethodDeclaration("Constant deprecated").build();
                advisors.add(advisorFileBean);
            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        return advisors;
    }

    static class JSFContextPropertiesHandler extends DefaultHandler {
        private final ArrayList<JSFProperty> jsfProperties = new ArrayList<>();
        private JSFProperty currentProperty = new JSFProperty(null, null);
        private boolean inTargetTag = false;
        private StringBuilder currentContent;

        public JSFContextPropertiesHandler() {

        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inTargetTag) {
                currentContent.append(new String(ch, start, length));
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("param-name".equals(qName)) {
                inTargetTag = true;
                currentContent = new StringBuilder();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("param-name".equals(qName)) {
                inTargetTag = false;
                currentProperty = new JSFProperty("param-name", currentContent.toString());
                jsfProperties.add(currentProperty);
            }
        }

        public ArrayList<JSFProperty> getJsfProperties() {
            return jsfProperties;
        }
    }

    record JSFProperty(String name, String value) {
    }
}

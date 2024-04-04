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

package fish.payara.advisor.config.files;

import fish.payara.advisor.AdvisorBean;
import fish.payara.advisor.AdvisorType;
import fish.payara.advisor.Analyzer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BeansXml implements Analyzer<List<AdvisorBean>> {

    private String keyPattern;

    public BeansXml() {
        this.keyPattern = "jakarta-cdi-file-empty-beans-xml";
    }

    public BeansXml(String keyPattern) {
        this.keyPattern = keyPattern;
    }

    public List<AdvisorBean> analise(File file) {
        List<AdvisorBean> advisors = new ArrayList<>();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = null;

        try {
            saxParser = factory.newSAXParser();
            BeansXml.BeanHandler handler = new BeansXml.BeanHandler();
            saxParser.parse(file, handler);
            ArrayList<BeansXml.Bean> beans = handler.getBeans();
            if (beans.isEmpty() && !handler.isAnnotated()) {
                AdvisorBean advisorFileBean = new AdvisorBean.
                        AdvisorBeanBuilder(keyPattern, "empty.beans.xml").
                        setFile(file).
                        setLine("0").
                        setType(AdvisorType.WARN).
                        setMethodDeclaration("empty beans.xml").build();
                advisors.add(advisorFileBean);
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        return advisors;
    }

    static class Bean {
        private final String name;
        private final String className;

        public Bean(String name, String className) {
            this.name = name;
            this.className = className;
        }

        public String getName() {
            return this.name;
        }

        public String getClassName() {
            return this.className;
        }
    }

    static class BeanHandler extends DefaultHandler {
        private final ArrayList<BeansXml.Bean> beans = new ArrayList<>();
        private BeansXml.Bean currentBean;

        private boolean isAnnotated = false;

        public BeanHandler() {
        }

        public ArrayList<BeansXml.Bean> getBeans() {
            return this.beans;
        }

        public boolean isAnnotated() {
            return this.isAnnotated;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if ("bean".equals(qName)) {
                String name = attributes.getValue("name");
                String className = attributes.getValue("class");
                this.currentBean = new BeansXml.Bean(name, className);
            } else if ("beans".equals(qName)) {
                String mode = attributes.getValue("bean-discovery-mode");
                if ("all".equals(mode)) {
                    this.isAnnotated = true;
                }
            }
        }

        public void endElement(String uri, String localName, String qName) {
            if ("bean".equals(qName)) {
                this.beans.add(this.currentBean);
                this.currentBean = null;
            }
        }
    }
}

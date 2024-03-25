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

import fish.payara.advisor.config.files.BeansXml;
import fish.payara.advisor.config.files.JaxWsProperties;
import fish.payara.advisor.config.files.JaxmProperties;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class AdvisorEvaluator {

    public List<AdvisorBean> adviseCode(Properties patterns, List<File> files) throws IOException {
        List<AdvisorBean> advisorsList = new ArrayList<>();
        AdvisorInterface[] advisorInterfaces = new AdvisorInterface[]{new AdvisorMethodCall(), new AdvisorClassImport()};
        for (File sourceFile : files) {
            patterns.forEach((k, v) -> {
                String value = (String) v;
                if (value.contains("#")) {
                    processMethodCall(value, (String) k, sourceFile, advisorsList);
                } else if(value.contains("@")) {
                    processAnnotation(value, (String) k, sourceFile, advisorsList);
                } else {
                    for (AdvisorInterface advisorInterface : advisorInterfaces) {
                        AdvisorBean advisorBean = null;
                        try {
                            advisorBean = advisorInterface.parseFile((String) k, (String) v, sourceFile);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        if (advisorBean != null && !advisorsList.contains(advisorBean)) {
                            advisorsList.add(advisorBean);
                        }
                    }
                }
            });
        }
        return advisorsList;
    }

    private void processMethodCall(String value, String key, File sourceFile, List<AdvisorBean> advisorsList) {
        String importNameSpace = value.substring(0, value.indexOf("#"));
        String methodCall = value.substring(value.indexOf("#") + 1);
        //search import
        AdvisorBean advisorBean = null;
        AdvisorClassImport acimp = new AdvisorClassImport();
        try {
            advisorBean = acimp.parseFile(key, importNameSpace, sourceFile);
            //check if method call
            if(advisorBean != null) {
                AdvisorMethodCall amc = new AdvisorMethodCall();
                String args = "";
                if (methodCall.contains("(") && methodCall.contains(")")) {
                    args = methodCall.substring(methodCall.indexOf('(') + 1, methodCall.indexOf(')'));
                }
                if (methodCall.contains("constructor")){
                    AdvisorConstructorCall acc = new AdvisorConstructorCall();
                    String constructorClass = importNameSpace.substring(importNameSpace.lastIndexOf(".") + 1);
                    if (args.length() > 0) {
                        if (args.indexOf(',') > -1) {
                            advisorBean = acc.parseFile(key, constructorClass, sourceFile, args.split(","));
                        } else {
                            advisorBean = acc.parseFile(key, constructorClass, sourceFile, args);
                        }
                    } else {
                        advisorBean = acc.parseFile(key, constructorClass, sourceFile);
                    }
                } else if(methodCall.contains("constant")) {
                    AdvisorFieldCall afc = new AdvisorFieldCall();
                    String constantName = methodCall.substring(9, methodCall.length());
                    advisorBean = afc.parseFile(key, constantName, sourceFile, args);
                } else if (methodCall.contains("(") && methodCall.contains(")")) {
                    if (args.indexOf(',') > -1) {
                        advisorBean = amc.parseFile(key, methodCall, sourceFile, args.split(","));
                    } else {
                        advisorBean = amc.parseFile(key, methodCall, sourceFile, args);
                    }
                } else if (methodCall.contains("->")) {
                    AdvisorMethodReturn amr = new AdvisorMethodReturn();
                    String returnType = methodCall.substring(methodCall.indexOf('-' + 2));
                    advisorBean = amr.parseFile(key, methodCall.split("->")[0], sourceFile, returnType);
                } else {
                    advisorBean = amc.parseFile(key, methodCall, sourceFile);
                }

                if (advisorBean != null && !advisorsList.contains(advisorBean)) {
                    advisorsList.add(advisorBean);
                } else {
                    //check if method declaration
                    AdvisorMethodDeclaration amd = new AdvisorMethodDeclaration();
                    advisorBean = amd.parseFile(key, methodCall, sourceFile);
                    if(advisorBean != null && !advisorsList.contains(advisorBean)) {
                        advisorsList.add(advisorBean);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void processAnnotation(String value, String key, File sourceFile, List<AdvisorBean> advisorsList) {
        String importAnnotationNameSpace = value.substring(0, value.indexOf("@"));
        String annotationPropertyDeclaration = value.substring(value.indexOf("@") + 1, value.length());
        //search import
        AdvisorBean advisorBean = null;
        AdvisorClassImport acimp = new AdvisorClassImport();
        try {
            advisorBean = acimp.parseFile(key, importAnnotationNameSpace, sourceFile);
            //check if annotation with property
            if (advisorBean != null) {
                AdvisorAnnotationWithProperty aacwp = new AdvisorAnnotationWithProperty();
                advisorBean = aacwp.parseFile(key, importAnnotationNameSpace, annotationPropertyDeclaration, sourceFile);
                if(advisorBean != null && !advisorsList.contains(advisorBean)) {
                    advisorsList.add(advisorBean);
                }
            }
        } catch(FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public void adviseJspandJSFFiles(Properties patterns, List<AdvisorBean> advisorBeans, List<File> files) {
        Set<Map.Entry> namespaceProperties = patterns.entrySet().stream()
                .filter(entry -> entry.getKey().toString().contains("namespace-upgrade"))
                .collect(Collectors.toSet());
        Set<Map.Entry> deprecatedTags = patterns.entrySet().stream()
                .filter(entry -> entry.getKey().toString().contains("tag-deprecated"))
                .collect(Collectors.toSet());
        for (File sourceFile : files) {
            if(sourceFile.exists() && sourceFile.isFile()) {
                try {
                    List<String> allLines = Files.readAllLines(Paths.get(sourceFile.toURI()));
                    namespaceProperties.stream().forEach(entry -> {
                        String valuePattern = (String) entry.getValue();
                        String key = (String) entry.getKey();
                        Optional<String> result = allLines.stream().filter(s -> s.contains(valuePattern)).findAny();
                        if (result.isPresent()) {
                            AdvisorBean advisorBean = new AdvisorBean.AdvisorBeanBuilder(key, valuePattern)
                                    .setFile(sourceFile)
                                    .setMethodDeclaration("namespace:" + valuePattern + " was replaced").build();
                            advisorBeans.add(advisorBean);
                        }
                    });
                    deprecatedTags.stream().forEach(entry -> {
                        String valuePattern = (String) entry.getValue();
                        String key = (String) entry.getKey();
                        Optional<String> result = valuePattern.contains("#") ?
                                allLines.stream().filter(s -> s.contains(valuePattern.split("#")[0]) &&
                                        s.contains(valuePattern.split("#")[1])).findAny()
                                : allLines.stream().filter(s -> s.contains(valuePattern)).findAny();
                        if (result.isPresent()) {
                            AdvisorBean advisorBean = new AdvisorBean.AdvisorBeanBuilder(key, valuePattern)
                                    .setFile(sourceFile)
                                    .setMethodDeclaration(valuePattern + " was deprecated").build();
                            advisorBeans.add(advisorBean);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void adviseConfigFiles(List<AdvisorBean> advisorBeans, List<File> files) {
        Analyzer<List<AdvisorBean>> beanAnalyzer;

        boolean beanXmlNotFound = true;
        for (File file : files) {
            if (file.isFile()) {
                if ("beans.xml".equals(file.getName())) {
                    beanAnalyzer = new BeansXml();
                    beanXmlNotFound = false;
                    List<AdvisorBean> advisorsFromAnalyzer = beanAnalyzer.analise(file);
                    if (advisorsFromAnalyzer.size() > 0) {
                        advisorBeans.addAll(advisorsFromAnalyzer);
                    }
                } else if ("jaxws.properties".equals(file.getName())) {
                    beanAnalyzer = new JaxWsProperties();
                    List<AdvisorBean> advisorsFromAnalyzer = beanAnalyzer.analise(file);
                    advisorBeans.addAll(advisorsFromAnalyzer);
                } else if ("jaxm.properties".equals(file.getName())) {
                    beanAnalyzer = new JaxmProperties();
                    List<AdvisorBean> advisorsFromAnalyzer = beanAnalyzer.analise(file);
                    if (!advisorBeans.containsAll(advisorsFromAnalyzer)) {
                        advisorBeans.addAll(advisorsFromAnalyzer);
                    }
                }
            }
        }
        if (beanXmlNotFound) {
            AdvisorBean advisorFileBean = new AdvisorBean.
                    AdvisorBeanBuilder("jakarta-cdi-file-not-found-beans-xml", "not.found.beans.xml").
                    setMethodDeclaration("not found beans.xml").build();
            advisorBeans.add(advisorFileBean);
        }
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2023 Payara Foundation and/or its affiliates. All rights reserved.
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import fish.payara.advisor.config.files.JaxmProperties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

@Mojo(name = "advice", defaultPhase = LifecyclePhase.VERIFY)
public class AdvisorToolMojo extends AbstractMojo {

    private static final Logger log = Logger.getLogger(AdvisorToolMojo.class.getName());
    
    private static final String ADVISE_VERSION = "adviseVersion";
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "advisor-plugin.adviseVersion", defaultValue = "10")
    private String adviseVersion;

    @Override
    public void execute() {
        Properties patterns = null;
        List<AdvisorBean> advisorBeans = Collections.emptyList();
        Properties properties = System.getProperties();
        if(properties.getProperty(ADVISE_VERSION) != null) {
            this.adviseVersion = properties.getProperty(ADVISE_VERSION);
        }
        try {
            if (adviseVersion != null && !adviseVersion.isEmpty()) {
                patterns = loadPatterns(adviseVersion);
            } else {
                log.severe("You need to indicate adviseVersion option");
                return;
            }

            List<File> files = readSourceFiles();
            if (!files.isEmpty()) {
                if (patterns != null && !patterns.isEmpty()) {
                    advisorBeans = inspectCode(patterns, files);
                }
            }
            
            files = readJSPandJSFFiles();
            if(!files.isEmpty()) {
                checkJspandJSFFiles(patterns, advisorBeans, files);
            }
            
            files = readConfigFiles();
            if (!files.isEmpty()) {
                this.checkConfigFiles(advisorBeans, files);
            }
            setLogSeverityForMessages(advisorBeans);
            //print messages
            addMessages(advisorBeans);
            printToConsole(advisorBeans);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkConfigFiles(List<AdvisorBean> advisorBeans, List<File> files) {
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
    
    private void checkJspandJSFFiles(Properties patterns, List<AdvisorBean> advisorBeans, List<File> files) {
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

    public Properties loadPatterns(String version) throws URISyntaxException, IOException {
        //validate configurations
        if(AdvisorToolMojo.class.getClassLoader().getResource("config/jakarta"+version) == null) {
            log.severe(String.format("Not available configurations for the indicated version %s", version));
            return null;
        }
        URI uriBaseFolder = Objects.requireNonNull(AdvisorToolMojo.class.getClassLoader().getResource(
                "config/jakarta" + version + "/mappedPatterns")).toURI();
        Properties readPatterns = new Properties();
        Path internalPath = null;
        if(uriBaseFolder.getScheme().equals("jar")) {
            FileSystem fileSystem = FileSystems.newFileSystem(uriBaseFolder, Collections.<String, Object>emptyMap());
            internalPath = fileSystem.getPath("config/jakarta"+version+"/mappedPatterns");
        } else {
            internalPath = Paths.get(uriBaseFolder);
        }
        Stream<Path> walk = Files.walk(internalPath, 1);
        for (Iterator<Path> it = walk.iterator(); it.hasNext();){
            Path p = it.next();
            if(p.getFileName().toString().contains(".properties")) {
                getLog().info("Read file Stream:"+p.toString());
                readPatterns.load(AdvisorToolMojo.class.getClassLoader().getResourceAsStream(p.toString()));
            }
        }
        return readPatterns;
    }

    public List<File> readSourceFiles() throws IOException {
        List<File>javaFiles = new ArrayList<>();
        if(project.getBasedir() != null) {
            javaFiles = Files.walk(Paths.get(project.getBasedir().toURI()))
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.toString().contains(File.separator + "target" + File.separator))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
        return javaFiles;
    }

    public List<File> readConfigFiles() throws IOException {
        List<File> configFiles = new ArrayList<>();
        if(project.getBasedir() != null) {
            configFiles = Files.walk(Paths.get(project.getBasedir().toURI()))
                    .filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".xml")
                                    || p.toString().endsWith(".properties"))
                    .filter(p -> !p.toString().contains(File.separator + "target" + File.separator))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
        return configFiles;
    }
    
    public List<File> readJSPandJSFFiles() throws IOException {
        List<File> jspFiles = new ArrayList<>();
        if(project.getBasedir() != null) {
            jspFiles = Files.walk(Paths.get(project.getBasedir().toURI()))
                    .filter(Files::isRegularFile)
                    .filter(p->p.toString().endsWith(".jsp") || p.toString().endsWith(".xhtml"))
                    .filter(p -> !p.toString().contains(File.separator + "target" + File.separator))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
        return jspFiles;
    }

    public List<AdvisorBean> inspectCode(Properties patterns, List<File> files) throws IOException {
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
    
    public void processMethodCall(String value, String key, File sourceFile, List<AdvisorBean> advisorsList) {
        String importNameSpace = value.substring(0, value.indexOf("#"));
        String methodCall = value.substring(value.indexOf("#") + 1, value.length());
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

    public void processAnnotation(String value, String key, File sourceFile, List<AdvisorBean> advisorsList) {
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

    public void setLogSeverityForMessages(List<AdvisorBean> advisorMethodBeanList) {
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
    
    public void addMessages(List<AdvisorBean> advisorMethodBeanList) {
        addMessages("config/jakarta" + adviseVersion + "/advisorMessages", advisorMethodBeanList, "message");
        addMessages("config/jakarta" + adviseVersion + "/advisorFix", advisorMethodBeanList, "fix");
    }

    public void addMessages(String url, List<AdvisorBean> advisorMethodBeanList, String type) {
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
    
    public void findMessage(Path internalPath, String keyPattern, String type, AdvisorBean b) throws IOException {
        String fileMessageName = null;
        String fileFix = null;
        String keyIssue = null;
        Properties messageProperties = new Properties();
        String subSpec = keyPattern.contains("method") ? "method" : (
            keyPattern.contains("remove") ? "remove" : (keyPattern.contains("file") ? "file": (
                    keyPattern.contains("namespace") ? "namespace" : "tag")));
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
                    messageProperties.load(AdvisorToolMojo.class.getClassLoader().getResourceAsStream(p.toString()));
                }
                if (type.equals("fix") && p.getFileName().toString().contains(fileFix)) {
                    messageProperties.load(AdvisorToolMojo.class.getClassLoader().getResourceAsStream(p.toString()));
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
    
    public void printToConsole(List<AdvisorBean> advisorMethodBeanList) {
        getLog().info("Showing Advices");
        getLog().info("***************");
        advisorMethodBeanList.forEach(b -> {
            if(b.getType() != null) {
                switch (b.getType()) {
                    case INFO:
                        getLog().info(b.toString());
                        break;
                    case WARN:
                        getLog().warn(b.toString());
                        break;
                    case ERROR:
                        getLog().error(b.toString());
                        break;
                    default:
                        break;
                }
            } else {
                getLog().info(b.toString());  
            }
        });
    }
    
    public void setAdviseVersion(String adviseVersion) {
        this.adviseVersion = adviseVersion;
    }

    public String getAdviseVersion() {
        return adviseVersion;
    }

}

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

import com.github.javaparser.ast.visitor.VoidVisitor;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdvisorMethodCallTest {

    @Test
    void createVoidVisitorFromMethodCall() {
        AdvisorMethodCall advisorMethodCall = new AdvisorMethodCall();
        VoidVisitor<List<AdvisorBean>> expectedVisitor = advisorMethodCall.createVoidVisitor(
                "jakarta-authentication-method-change-issue-87-case-1-info", 
                "security.auth.message.MessageInfo#getMap");
        assertNotNull(expectedVisitor);
    }
    
    @Test 
    void createVoidVisitorReturnNull() {
        AdvisorMethodCall advisorMethodCall = new AdvisorMethodCall();
        VoidVisitor<List<AdvisorBean>> expectedVisitor = advisorMethodCall.createVoidVisitor(
                "jakarta-authentication-method-change-issue-87-case-1-info",
                "security.auth.message.MessageInfo#getMap", "secondPattern");
        assertNull(expectedVisitor);
    }
    
    @Test
    void createBeanFromParsingFileWithMethodDeclaration() throws FileNotFoundException {
        Path resourcePath = Paths.get("src", "test", "resources", "TestAuthConfigFactory.java");
        AdvisorMethodCall advisorMethodCall = new AdvisorMethodCall();
        File resourceFile = resourcePath.toFile();
        assertNotNull(resourceFile);
        
        VoidVisitor<List<AdvisorBean>> expectedVisitor = advisorMethodCall.createVoidVisitor(
                "jakarta-authentication-method-change-issue-87-case-2-info",
                "security.auth.message.config.AuthConfigFactory#registerConfigProvider");
        assertNotNull(expectedVisitor);

        AdvisorBean expectedBean = advisorMethodCall.parseFile(
                "jakarta-authentication-method-change-issue-87-case-2-info",
                "registerConfigProvider", resourceFile);
        assertNotNull(expectedBean);
    }
    
    @Test
    void createBeanFromParsingFileWithMethodCallAndParameters() throws FileNotFoundException {
        Path resourcePath = Paths.get("src", "test", "resources", "ServletExample.java");
        AdvisorMethodCall advisorMethodCall = new AdvisorMethodCall();
        File resourceFile = resourcePath.toFile();
        assertNotNull(resourceFile);

        VoidVisitor<List<AdvisorBean>> expectedVisitor = advisorMethodCall.createVoidVisitor(
                "jakarta-servlet-method-removed-set-status-error",
                "servlet.http.HttpServletResponse#setStatus(int, String)");
        assertNotNull(expectedVisitor);

        AdvisorBean expectedBean = advisorMethodCall.parseFile(
                "jakarta-servlet-method-removed-set-status-error",
                "setStatus", resourceFile, "int, String".split(","));
        assertNotNull(expectedBean);
    }
    
    @Test 
    void createBeanFromParsingFileWithMethodCall() throws FileNotFoundException {
        Path resourcePath = Paths.get("src", "test", "resources", "CDIBean.java");
        AdvisorMethodCall advisorMethodCall = new AdvisorMethodCall();
        File resourceFile = resourcePath.toFile();
        assertNotNull(resourceFile);

        VoidVisitor<List<AdvisorBean>> expectedVisitor = advisorMethodCall.createVoidVisitor(
                "jakarta-authorization-method-change-issue-105-info",
                "security.jacc.PolicyContext#getContext");
        assertNotNull(expectedVisitor);

        AdvisorBean expectedBean = advisorMethodCall.parseFile(
                "jakarta-authorization-method-change-issue-105-info",
                "getContext", resourceFile);
        assertNotNull(expectedBean);
    }
    
    @Test 
    void createBeanFromParsingFileWithClassExpression() throws FileNotFoundException {
        Path resourcePath = Paths.get("src", "test", "resources", "JaxbClassTest.java");
        AdvisorMethodCall advisorMethodCall = new AdvisorMethodCall();
        File resourceFile = resourcePath.toFile();
        assertNotNull(resourceFile);

        VoidVisitor<List<AdvisorBean>> expectedVisitor = advisorMethodCall.createVoidVisitor(
                "jakarta-restful-ws-method-change-jaxbadapter-deprecated-warn",
                "ws.rs.core.Link#JaxbAdapter");
        assertNotNull(expectedVisitor);

        AdvisorBean expectedBean = advisorMethodCall.parseFile(
                "jakarta-restful-ws-method-change-jaxbadapter-deprecated-warn",
                "JaxbAdapter", resourceFile);
        assertNotNull(expectedBean);
    }
    @Test
    void createBeanFromParsingFileWithMethodCallWithParameters() throws FileNotFoundException {
        Path resourcePath = Paths.get("src", "test", "resources", "MyMethodExpression.java");
        AdvisorMethodCall advisorMethodCall = new AdvisorMethodCall();
        File resourceFile = resourcePath.toFile();
        assertNotNull(resourceFile);
        
        VoidVisitor<List<AdvisorBean>> expectedVisitor = advisorMethodCall.createVoidVisitor(
                "jakarta-server-pages-method-get-feature-descriptors-1-warn",
                "el.ArrayELResolver#getFeatureDescriptors(ELContext, Object)");
        assertNotNull(expectedVisitor);

        AdvisorBean expectedBean = advisorMethodCall.parseFile(
                "jakarta-server-pages-method-get-feature-descriptors-1-warn",
                "getFeatureDescriptors(ELContext, Object)", resourceFile, "ELContext, Object".split(","));
        assertNotNull(expectedBean);
    }
}
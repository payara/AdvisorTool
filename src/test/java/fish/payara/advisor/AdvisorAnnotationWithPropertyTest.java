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

class AdvisorAnnotationWithPropertyTest {

    @Test
    void createVoidVisitorCollectionWithSecondPattern() {
        AdvisorAnnotationWithProperty advisorAnnotationWithProperty = new AdvisorAnnotationWithProperty();
        VoidVisitor<List<AdvisorBean>> expectedVisitor = advisorAnnotationWithProperty
                .createVoidVisitor("jakarta-jsonb-method-change-deprecate-nillable-warn", 
                        "json.bind.annotation.JsonbProperty", "nillable");
        assertNotNull(expectedVisitor);
    }
    
    @Test
    void createVoidVisitorReturnNull() {
        AdvisorAnnotationWithProperty advisorAnnotationWithProperty = new AdvisorAnnotationWithProperty();
        VoidVisitor<List<AdvisorBean>> expectedVisitor = advisorAnnotationWithProperty
                .createVoidVisitor("jakarta-jsonb-method-change-deprecate-nillable-warn",
                        "json.bind.annotation.JsonbProperty", "nillable", "new pattern");
        assertNull(expectedVisitor);
    }
    
    @Test 
    void createBeanFromParsingFileWithNormalAnnotation() throws FileNotFoundException {
        Path resourcePath = Paths.get("src", "test", "resources", "JsonBeanTest.java");
        AdvisorAnnotationWithProperty advisorAnnotationWithProperty = new AdvisorAnnotationWithProperty();
        File resourceFile = resourcePath.toFile();
        assertNotNull(resourceFile);

        VoidVisitor<List<AdvisorBean>> expectedVisitor = advisorAnnotationWithProperty
                .createVoidVisitor("jakarta-jsonb-method-change-deprecate-nillable-warn",
                        "json.bind.annotation.JsonbProperty", "nillable");
        assertNotNull(expectedVisitor);

        AdvisorBean expectedBean = advisorAnnotationWithProperty.parseFile("jakarta-jsonb-method-change-deprecate-nillable-warn", 
                "json.bind.annotation.JsonbProperty", "nillable", resourceFile);
        assertNotNull(expectedBean);
    }
    
    @Test 
    void createBeanFromParsingFileWithSingleMemberAnnotation() throws FileNotFoundException {
        Path resourcePath = Paths.get("src", "test", "resources", "HelloResource.java");
        AdvisorAnnotationWithProperty advisorAnnotationWithProperty = new AdvisorAnnotationWithProperty();
        File resourceFile = resourcePath.toFile();
        assertNotNull(resourceFile);
        
        VoidVisitor<List<AdvisorBean>> expectedVisitor = advisorAnnotationWithProperty
                .createVoidVisitor("jakarta-restful-ws-method-change-context-deprecated-warn",
                        "ws.rs.core.Context", "Context");
        assertNotNull(expectedVisitor);
        
        AdvisorBean expectedBean = advisorAnnotationWithProperty.parseFile(
                "jakarta-restful-ws-method-change-context-deprecated-warn",
                "ws.rs.core.Context", "Context", resourceFile);
        assertNotNull(expectedBean);
    }
    
    @Test 
    void createBeanFromParsingFileWithMarkerAnnotation() throws FileNotFoundException {
        Path resourcePath = Paths.get("src", "test", "resources", "HelloResource.java");
        AdvisorAnnotationWithProperty advisorAnnotationWithProperty = new AdvisorAnnotationWithProperty();
        File resourceFile = resourcePath.toFile();
        assertNotNull(resourceFile);

        VoidVisitor<List<AdvisorBean>> expectedVisitor = advisorAnnotationWithProperty
                .createVoidVisitor("jakarta-restful-ws-method-change-consumes-multipart-info",
                        "ws.rs.Consumes", "MULTIPART_FORM_DATA");
        assertNotNull(expectedVisitor);

        AdvisorBean expectedBean = advisorAnnotationWithProperty.parseFile(
                "jakarta-restful-ws-method-change-consumes-multipart-info",
                "ws.rs.Consumes", "MULTIPART_FORM_DATA", resourceFile);
        assertNotNull(expectedBean);
    }
}
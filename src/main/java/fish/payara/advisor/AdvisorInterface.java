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

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public interface AdvisorInterface {

    VoidVisitor<List<AdvisorBean>> createVoidVisitor(String keyPattern, String valuePattern, String... params);

    VoidVisitor<List<AdvisorBean>> createVoidVisitor(String keyPattern, String valuePattern, String secondPattern);

    default AdvisorBean parseFile(String keyPattern, String valuePattern, File f, String... params) throws FileNotFoundException {
        List<AdvisorBean> advisorBeanList = new ArrayList<>();
        VoidVisitor<List<AdvisorBean>> collector = createVoidVisitor(keyPattern, valuePattern, params);
        CompilationUnit compilationUnit = StaticJavaParser.parse(f);
        collector.visit(compilationUnit, advisorBeanList);
        if (!advisorBeanList.isEmpty()) {
            AdvisorBean b = advisorBeanList.get(0);
            String importDeclaration = b.getImportDeclaration();
            if (importDeclaration != null && (valuePattern.contains("jakarta") || valuePattern.contains("javax"))) {
                return compareImports(importDeclaration, valuePattern, b, f);
            } else if (importDeclaration != null) {
                importDeclaration = removingPrefixFromNameSpace(importDeclaration);
                return compareImports(importDeclaration, valuePattern, b, f);
            } else if (importDeclaration == null) {
                b.setFile(f);
                return b;
            }
        }
        return null;
    }

    private String removingPrefixFromNameSpace(String importDeclaration) {
        if (importDeclaration.contains("javax")) {
            int position = importDeclaration.indexOf("javax");
            return importDeclaration.substring(position + 6);
        } else if (importDeclaration.contains("jakarta")) {
            int position = importDeclaration.indexOf("jakarta");
            return importDeclaration.substring(position + 8);
        }
        return importDeclaration;
    }

    private AdvisorBean compareImports(String importDeclaration, String valuePattern, AdvisorBean b, File f) {
        if (this instanceof AdvisorClassImport && !importDeclaration.equals(valuePattern)) {
            return null;
        }
        b.setFile(f);
        return b;
    }
}

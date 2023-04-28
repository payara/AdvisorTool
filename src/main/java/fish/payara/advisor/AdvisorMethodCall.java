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

import com.github.javaparser.Position;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdvisorMethodCall implements AdvisorInterface {
    
    @Override
    public AdvisorBean parseFile(String keyPattern, String valuePattern, File f) throws FileNotFoundException {
        List<AdvisorBean> advisorMethodBeanList = new ArrayList<>();
        VoidVisitor<List<AdvisorBean>> methodCallCollector = 
                new MethodCallCollector(keyPattern, valuePattern);
        CompilationUnit compilationUnit = StaticJavaParser.parse(f);
        methodCallCollector.visit(compilationUnit, advisorMethodBeanList);
        if(advisorMethodBeanList != null && advisorMethodBeanList.size() > 0){
            AdvisorBean b = advisorMethodBeanList.get(0);
            b.setFile(f);
            return b;
        }
        return null;
    }

    private static class MethodCallCollector extends VoidVisitorAdapter<List<AdvisorBean>> {
        
        private String keyPattern;
        private String valuePattern;
        
        public MethodCallCollector(String keyPattern, String valuePattern) {
            this.keyPattern = keyPattern;
            this.valuePattern = valuePattern;
        }
        
        public void visit(MethodCallExpr methodCall, List<AdvisorBean> collector) {
            super.visit(methodCall, collector);
            Optional<Position> p = methodCall.getBegin();
            if(methodCall.toString().contains(valuePattern)) {
                AdvisorBean advisorMethodBean = new AdvisorBean.
                        AdvisorBeanBuilder(keyPattern,valuePattern).
                        setLine(((p.isPresent()) ? ""+p.get().line : "")).
                        setMethodDeclaration(methodCall.asMethodCallExpr().toString()).build();
                collector.add(advisorMethodBean);
            }
        }
    }
    
}

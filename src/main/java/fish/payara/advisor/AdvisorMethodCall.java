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

import com.github.javaparser.Position;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;
import java.util.Optional;

public class AdvisorMethodCall implements AdvisorInterface {

    @Override
    public VoidVisitor<List<AdvisorBean>> createVoidVisitor(String keyPattern, String valuePattern, String... params) {
        return new MethodCallCollector(keyPattern, valuePattern, params);
    }

    @Override
    public VoidVisitor<List<AdvisorBean>> createVoidVisitor(String keyPattern, String valuePattern, String secondPattern) {
        return null;
    }


    private static class MethodCallCollector extends VoidVisitorAdapter<List<AdvisorBean>> {

        private final String keyPattern;
        private final String valuePattern;
        private final String[] params;

        public MethodCallCollector(String keyPattern, String valuePattern, String... params) {
            this.keyPattern = keyPattern;
            this.valuePattern = valuePattern;
            this.params = params;
        }

        @Override
        public void visit(MethodCallExpr methodCall, List<AdvisorBean> collector) {
            super.visit(methodCall, collector);
            Optional<Position> p = methodCall.getBegin();
            if (methodCall.toString().contains(valuePattern)) {
                AdvisorBean advisorMethodBean = new AdvisorBean.
                        AdvisorBeanBuilder(keyPattern, valuePattern).
                        setLine((p.map(position -> "" + position.line).orElse(""))).
                        setMethodDeclaration(methodCall.asMethodCallExpr().toString()).build();
                collector.add(advisorMethodBean);
            } else if (valuePattern.contains("(") && valuePattern.contains(")") &&
                    methodCall.toString().contains(valuePattern.substring(0, valuePattern.indexOf('('))) &&
                    isSameArgumentTypes(params, methodCall.getArguments())) {
                AdvisorBean advisorMethodBean = new AdvisorBean.
                        AdvisorBeanBuilder(keyPattern, valuePattern).
                        setLine((p.map(position -> "" + position.line).orElse(""))).
                        setMethodDeclaration(methodCall.asMethodCallExpr().toString()).build();
                collector.add(advisorMethodBean);
            }
        }

        @Override
        public void visit(ClassExpr classExpr, List<AdvisorBean> collector) {
            super.visit(classExpr, collector);
            Optional<Position> p = classExpr.getBegin();
            if(classExpr.isClassExpr() && classExpr.toString().contains(valuePattern)) {
                AdvisorBean advisorMethodBean = new AdvisorBean.AdvisorBeanBuilder(keyPattern, valuePattern).
                        setLine((p.map(position -> "" + position.line).orElse(""))).
                        setMethodDeclaration(classExpr.toString()).build();
                collector.add(advisorMethodBean);
            }
        }

        @Override
        public void visit(MethodDeclaration methodDeclaration, List<AdvisorBean> collector) {
            super.visit(methodDeclaration, collector);
            Optional<Position> p = methodDeclaration.getBegin();
            String methodName = null;
            if(valuePattern.contains("(")) {
                methodName = valuePattern.substring(0, valuePattern.indexOf("("));
            } else {
                methodName = valuePattern;
            }
            if(methodDeclaration.isMethodDeclaration() && (params != null && params.length > 0 
                    && compareParameters(params, methodDeclaration.getParameters()) 
                    && methodDeclaration.getName().asString().trim().contains(methodName.trim()))) {
                AdvisorBean advisorMethodBean = new AdvisorBean.AdvisorBeanBuilder(keyPattern, valuePattern).
                        setLine((p.map(position -> "" + position.line).orElse(""))).
                        setMethodDeclaration(methodDeclaration.getDeclarationAsString()).build();
                collector.add(advisorMethodBean);
            }
        }
    }

    protected static boolean isSameArgumentTypes(String[] params, NodeList<Expression> nodeList) {
        if (nodeList.size() != params.length) {
            return false;
        }
        for (int i = 0; i < nodeList.size(); i++) {
            Expression argument = nodeList.get(i);
            if (argument instanceof ObjectCreationExpr) {
                ClassOrInterfaceType type = ((ObjectCreationExpr) argument).getType();
                if (!type.getName().asString().equals(params[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    protected static boolean compareParameters(String[] params, NodeList<Parameter> nodeList) {
        if (nodeList.size() != params.length) {
            return false;
        }
        for (String param: params) {
            Optional<Parameter> optionalParameter = nodeList.stream().filter(n -> n.getType().toString().contains(param.trim())).findAny();
            if(!optionalParameter.isPresent()) {
                return false;
            }
        }
        return true;
    }
    
    
}

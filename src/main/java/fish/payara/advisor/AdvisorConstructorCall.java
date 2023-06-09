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
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.List;
import java.util.Optional;

public class AdvisorConstructorCall extends AdvisorMethodCall implements AdvisorInterface {

    @Override
    public VoidVisitor<List<AdvisorBean>> createVoidVisitor(String keyPattern, String valuePattern, String... params) {
        return new AdvisorConstructorCall.ConstructorCallCollector(keyPattern, valuePattern, params);
    }

    @Override
    public VoidVisitor<List<AdvisorBean>> createVoidVisitor(String keyPattern, String valuePattern, String secondPattern) {
        return null;
    }

    private static class ConstructorCallCollector extends VoidVisitorAdapter<List<AdvisorBean>> {
        private final String keyPattern;
        private final String valuePattern;
        private final String[] params;

        public ConstructorCallCollector(String keyPattern, String valuePattern, String... params) {
            this.keyPattern = keyPattern;
            this.valuePattern = valuePattern;
            this.params = params;
        }

        @Override
        public void visit(ObjectCreationExpr objectCreationExpr, List<AdvisorBean> advisorBeans) {
            super.visit(objectCreationExpr, advisorBeans);
            Optional<Position> p = objectCreationExpr.getBegin();
            if (objectCreationExpr.isObjectCreationExpr() &&
                    objectCreationExpr.toString().contains(valuePattern)) {
                if (params.length == 0) {
                    AdvisorBean advisorMethodBean = new AdvisorBean.
                            AdvisorBeanBuilder(keyPattern, valuePattern).
                            setLine((p.map(position -> "" + position.line).orElse(""))).
                            setMethodDeclaration(objectCreationExpr.toString()).build();
                    advisorBeans.add(advisorMethodBean);
                } else if (isSameArgumentTypes(params, objectCreationExpr.getArguments())) {
                    AdvisorBean advisorMethodBean = new AdvisorBean.
                            AdvisorBeanBuilder(keyPattern, valuePattern).
                            setLine((p.map(position -> "" + position.line).orElse(""))).
                            setMethodDeclaration(objectCreationExpr.toString()).build();
                    advisorBeans.add(advisorMethodBean);
                }
            }
        }

        @Override
        public void visit(ExplicitConstructorInvocationStmt explicitConstructorInvocationStmt,
                          List<AdvisorBean> advisorBeans) {
            super.visit(explicitConstructorInvocationStmt, advisorBeans);
            Optional<Position> p = explicitConstructorInvocationStmt.getBegin();
            if(explicitConstructorInvocationStmt.isExplicitConstructorInvocationStmt()) {
                if (params.length == 0) {
                    AdvisorBean advisorMethodBean = new AdvisorBean.
                            AdvisorBeanBuilder(keyPattern, valuePattern).
                            setLine((p.map(position -> "" + position.line).orElse(""))).
                            setMethodDeclaration(explicitConstructorInvocationStmt.toString()).build();
                    advisorBeans.add(advisorMethodBean);
                }
            }
        }
    }
}

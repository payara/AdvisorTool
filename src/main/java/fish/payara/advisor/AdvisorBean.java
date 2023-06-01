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

import java.io.File;
import java.util.Objects;

public class AdvisorBean {
    private File file;
    private String methodDeclaration;
    private String importDeclaration;
    
    private String annotationDeclaration;
    private String line;

    private String keyPattern;

    private String valuePattern;
    private AdvisorMessage advisorMessage;
    
    private AdvisorBean(AdvisorBeanBuilder advisorBeanBuilder) {
        this.file = advisorBeanBuilder.file;
        this.methodDeclaration = advisorBeanBuilder.methodDeclaration;
        this.importDeclaration = advisorBeanBuilder.importDeclaration;
        this.line = advisorBeanBuilder.line;
        this.keyPattern = advisorBeanBuilder.keyPattern;
        this.valuePattern = advisorBeanBuilder.valuePattern;
        this.advisorMessage = advisorBeanBuilder.advisorMessage;
        this.annotationDeclaration = advisorBeanBuilder.annotationDeclaration;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj instanceof AdvisorBean) {
            final AdvisorBean other = (AdvisorBean) obj;
            return this.file.equals(other.file) && this.line.equals(other.line)
                    && this.keyPattern.equals(other.keyPattern) && this.valuePattern.equals(other.valuePattern);
        }
        return false;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getMethodDeclaration() {
        return methodDeclaration;
    }

    public void setMethodDeclaration(String methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    public String getImportDeclaration() {
        return importDeclaration;
    }

    public void setImportDeclaration(String importDeclaration) {
        this.importDeclaration = importDeclaration;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getKeyPattern() {
        return keyPattern;
    }

    public void setKeyPattern(String keyPattern) {
        this.keyPattern = keyPattern;
    }

    public String getValuePattern() {
        return valuePattern;
    }

    public void setValuePattern(String valuePattern) {
        this.valuePattern = valuePattern;
    }

    public AdvisorMessage getAdvisorMessage() {
        return advisorMessage;
    }

    public void setAdvisorMessage(AdvisorMessage advisorMessage) {
        this.advisorMessage = advisorMessage;
    }

    public String getAnnotationDeclaration() {
        return annotationDeclaration;
    }

    public void setAnnotationDeclaration(String annotationDeclaration) {
        this.annotationDeclaration = annotationDeclaration;
    }

    @Override
    public String toString() {
        return "Line of code: " + (line == null ? "-" : line) + " | Expression: " + getExpression() + "\n"+
                "Source file: " + (file == null ? "-" : file.getName()) + "\n"+
                advisorMessage.toString();
    }

    private String getExpression() {
        if (methodDeclaration != null) {
            return methodDeclaration;
        }
        
        if(annotationDeclaration != null) {
            return annotationDeclaration;
        }

        return Objects.requireNonNullElse(importDeclaration, "");
    }

    public static class AdvisorBeanBuilder {
        private File file;
        private String methodDeclaration;
        private String importDeclaration;
        
        private String annotationDeclaration;
        private String line;

        final private String keyPattern;
        final private String valuePattern;

        private AdvisorMessage advisorMessage;

        public AdvisorBeanBuilder(String keyPattern, String valuePattern) {
            this.keyPattern = keyPattern;
            this.valuePattern = valuePattern;
        }
        
        public AdvisorBeanBuilder setFile(File file) {
            this.file = file;
            return this;
        }
        
        public AdvisorBeanBuilder setMethodDeclaration(String methodDeclaration) {
            this.methodDeclaration = methodDeclaration;
            return this;
        }

        public AdvisorBeanBuilder setImportDeclaration(String importDeclaration) {
            this.importDeclaration = importDeclaration;
            return this;
        }

        public AdvisorBeanBuilder setLine(String line) {
           this.line = line;
           return this;
        }
        
        public AdvisorBeanBuilder setAdvisorMessage(AdvisorMessage advisorMessage) {
            this.advisorMessage = advisorMessage;
            return this;
        }
        
        public AdvisorBeanBuilder setAnnotationDeclaration(String annotationDeclaration) {
            this.annotationDeclaration = annotationDeclaration;
            return this;
        }
        
        public AdvisorBean build() {
            return new AdvisorBean(this);
        }
    }
}


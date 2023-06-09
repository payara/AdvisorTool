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

public class AdvisorMessage {

    public static final String YELLOW = "\033[0;33m";
    public static final String GREEN = "\033[0;32m";
    public static final String RED = "\033[0;31m";
    public static final String BLUE = "\033[0;34m";
    public static final String RESET = "\033[0m";
    private String message;
    private String fix;
    
    private AdvisorType type;

    private AdvisorMessage(AdvisorMessageBuilder advisorMessageBuilder) {
        this.message = advisorMessageBuilder.message;
        this.fix = advisorMessageBuilder.fix;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFix() {
        return fix;
    }

    public void setFix(String fix) {
        this.fix = fix;
    }

    public AdvisorType getType() {
        return type;
    }

    public void setType(AdvisorType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        if(this.getType() != null) {
            switch (this.getType()) {
                case INFO:
                    return GREEN + message + RESET + "\n" + BLUE + fix + RESET;
                case WARN:
                    return YELLOW + message + RESET + "\n" + BLUE + fix + RESET;
                case ERROR:
                    return RED + message + RESET + "\n" + BLUE + fix + RESET;
            }
        }
        return "";
    }

    public static class AdvisorMessageBuilder {
        
        private String message;
        
        private String fix;
        public AdvisorMessageBuilder setMessage(String message) {
            this.message = message;
            return this;
        }
        
        public AdvisorMessageBuilder setFix(String fix) {
            this.fix = fix;
            return this;
        }
        
        public AdvisorMessage build(){
            return new AdvisorMessage(this);
        }
        
    }
}

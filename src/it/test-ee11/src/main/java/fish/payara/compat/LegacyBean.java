/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2025 Payara Foundation and/or its affiliates. All rights reserved.
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
package fish.payara.compat;

import jakarta.annotation.ManagedBean;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

//import java.util.Date;
import java.util.Calendar;

import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.security.SecurityManager;
import java.util.Date;

@ManagedBean(name = "legacyBean") // **ManagedBean was removed in EE 11**
public class LegacyBean {

    private Date someDate; // legacy java.util.Date
    private Calendar someCalendar; // legacy Calendar

    @Temporal(TemporalType.TIMESTAMP) // deprecated in Persistence 3.2 (EE 11)
    public Date getSomeDate() {
        return someDate;
    }

    public void setSomeDate(Date someDate) {
        this.someDate = someDate;
    }

    @Temporal(TemporalType.DATE) // deprecated
    public Calendar getSomeCalendar() {
        return someCalendar;
    }

    public void setSomeCalendar(Calendar someCalendar) {
        this.someCalendar = someCalendar;
    }

    public void doSomething() {
        // reference to SecurityManager (removed in EE 11)
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // legacy code path
            sm.checkPermission(new RuntimePermission("somePermission"));
        }
    }

    // A JPA entity embedded inside the same class for illustration:
    @Entity
    public static class LegacyEntity {
        @Id
        private Long id;

        @Version
        private Date version; // JPA 3.2 only allows java.time.Instant, LocalDateTime or java.sql.Timestamp for version per spec :contentReference[oaicite:5]{index=5}

        @Temporal(TemporalType.TIME) // deprecated TemporalType.TIME :contentReference[oaicite:6]{index=6}
        private Calendar timeField;

        // getters and setters...
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Date getVersion() { return version; }
        public void setVersion(Date version) { this.version = version; }

        public Calendar getTimeField() { return timeField; }
        public void setTimeField(Calendar timeField) { this.timeField = timeField; }
    }

    // Example of SOAP-based web service client – these APIs are removed in EE11 platform
    @WebServiceClient(name = "LegacyService", wsdlLocation = "http://example.com/legacy?wsdl")
    public static class LegacySoapClient {
        // ...
    }

    // Example JAXB (XML binding) use — JAXB API removed from EE 11 platform
    @XmlRootElement(name = "legacy")
    public static class LegacyXml {
        private String data;
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

}

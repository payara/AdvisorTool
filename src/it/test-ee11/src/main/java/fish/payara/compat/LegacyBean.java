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

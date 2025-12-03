package fish.payara.compat;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.el.ELResolver;
import jakarta.faces.application.ResourceHandler;
import jakarta.faces.application.ViewHandler;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.ws.rs.core.MediaType;
import jakarta.servlet.http.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.CacheControl;
import jakarta.persistence.Query;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Persistence;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.TypedQuery;
import jakarta.annotation.ManagedBean;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.el.ExpressionFactory;

/**
 * Compile-only compatibility helper that references APIs which are modified
 * or deprecated in Jakarta EE 11 so they are exercised while keeping the
 * project targeted at Jakarta EE 10 / JDK 11.
 *
 * All usage is guarded behind 'if (false)' so the code does not execute at
 * runtime but still compiles against the Jakarta EE 10 API in this project.
 */
public final class EE11Compatibility {

    // Reference deprecated JsonbProperty.nillable() element
    @JsonbProperty(nillable = true)
    private String jsonbNillableField;

    // Reference deprecated Temporal annotation usage
    @Temporal(TemporalType.DATE)
    private java.util.Date legacyDateField;

    private EE11Compatibility() {}

    @SuppressWarnings({"deprecation", "unused"})
    public static void touchDeprecatedApis() {
        if (false) {
            try {
                // BeanManager.getELResolver() deprecated in EE11
                BeanManager bm = null;
                ELResolver el = bm.getELResolver();

                // Faces deprecated constants
                String s1 = ResourceHandler.JSF_SCRIPT_LIBRARY_NAME;
                String s2 = ResourceHandler.JSF_SCRIPT_RESOURCE_NAME;

                // ViewHandler.DEFAULT_SUFFIX is deprecated
                String s3 = ViewHandler.DEFAULT_SUFFIX;

                // Deprecated Query overloads that take java.util.Date/Calendar + TemporalType
                Query q = null;
                q.setParameter(1, new java.util.Date(), TemporalType.DATE);
                TypedQuery<String> tq = null;
                tq.setParameter(1, new java.util.Date(), TemporalType.TIME);

                // CriteriaQuery.multiselect(Selection...) is not typesafe
                CriteriaBuilder cb = null;
                CriteriaQuery<?> cq = null;
                Selection<?>[] selections = new Selection<?>[0];
                cq.multiselect(selections);

                // Graph.addSubgraph(Attribute, Class) deprecated in favor of treated variants
                // Use reflection to avoid hard compile-time linkage to jakarta.persistence.Graph
                try {
                    Class<?> graphClass = Class.forName("jakarta.persistence.Graph");
                    Class<?> attributeClass = Class.forName("jakarta.persistence.metamodel.Attribute");
                    Class<?> mapAttrClass = Class.forName("jakarta.persistence.metamodel.MapAttribute");
                    // try to resolve methods if present
                    try {
                        java.lang.reflect.Method addSub = graphClass.getMethod("addSubgraph", attributeClass, Class.class);
                        java.lang.reflect.Method addKeySub = graphClass.getMethod("addKeySubgraph", mapAttrClass, Class.class);
                        // noop: resolving methods is enough for our compile-time coverage purposes
                        if (addSub != null && addKeySub != null) {
                            // no-op
                        }
                    } catch (NoSuchMethodException ex) {
                        // ignore - method not present on this API version
                    }
                } catch (ClassNotFoundException cnf) {
                    // ignore - the class may not be present on older Jakarta EE API versions
                }

                // JAX-RS deprecated media type constant
                MediaType mt = MediaType.APPLICATION_SVG_XML_TYPE;
                String mtStr = MediaType.APPLICATION_SVG_XML;

                // jakarta.annotation.ManagedBean usage: declare a dummy annotated inner class
                @ManagedBean
                class _ManagedBeanDummy {
                    // no-op; presence of the annotation exercises the API at compile time
                }

                // Try resolving JAXB/JAX-WS APIs reflectively (they may not be present)
                try {
                    // JAXB (Jakarta XML Bind) - jakarta.xml.bind.JAXBContext
                    Class<?> jaxbCtx = Class.forName("jakarta.xml.bind.JAXBContext");
                    java.lang.reflect.Method newInstance = null;
                    try {
                        newInstance = jaxbCtx.getMethod("newInstance", java.lang.Class.class);
                    } catch (NoSuchMethodException ex) {
                        // older/newer API shapes may differ; resolving is enough
                    }
                    // JAX-WS Service class
                    try {
                        Class<?> jaxwsService = Class.forName("jakarta.xml.ws.Service");
                        // attempt to resolve create or getPort methods
                        try {
                            jaxwsService.getMethod("create", java.net.URL.class, java.lang.Class.class);
                        } catch (NoSuchMethodException ex) {
                            // ignore
                        }
                    } catch (ClassNotFoundException ex) {
                        // JAX-WS not present on classpath - fine
                    }
                } catch (ClassNotFoundException cnf) {
                    // JAXB not on classpath - ignore
                }

                // Servlet cookie deprecated accessors
                Cookie c = null;
                c.getComment();
                c.getVersion();

                // Deprecated NewCookie constructors
                NewCookie nc = new NewCookie("name", "value");
                NewCookie nc2 = new NewCookie("name", "value", "path", "domain", 0, "comment", 0, new java.util.Date(), false, false);

                // Deprecated CacheControl.valueOf(String)
                CacheControl cc = CacheControl.valueOf("no-cache");

                // deprecated BeanManager methods
                BeanManager bm2 = null;
                bm2.wrapExpressionFactory((ExpressionFactory) null);

                // jakarta.validation deprecated API: addNode
                ConstraintValidatorContext.ConstraintViolationBuilder cbuilder = null;
                cbuilder.addNode("field");

                // Persistence.PERSISTENCE_PROVIDER field referenced (deprecated)
                String prov = Persistence.PERSISTENCE_PROVIDER;

                // HttpServlet legacy constant
                String legacy = jakarta.servlet.http.HttpServlet.LEGACY_DO_HEAD;

                // SecurityManager-related references: check for current security manager and call a check method
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    try {
                        // call checkPermission as a representative API
                        sm.checkPermission(new java.security.BasicPermission("example") {
                            private static final long serialVersionUID = 1L;
                            public boolean implies(java.security.Permission p) { return false; }
                        });
                    } catch (SecurityException se) {
                        // ignore - we're only exercising the API
                    }
                }

                // reflectively reference the FacesConfig.version() element (annotation element)
                java.lang.reflect.Method m = jakarta.faces.annotation.FacesConfig.class.getMethod("version");
                if (m != null) {
                    // noop
                }
            } catch (NoSuchMethodException ex) {
                // ignore - compile-time reference is the goal
            }
        }
    }
}

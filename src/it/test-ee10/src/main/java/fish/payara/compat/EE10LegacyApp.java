/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2026 Payara Foundation and/or its affiliates. All rights reserved.
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

// ===== CDI: javax.enterprise.inject.New removed in CDI 4.0 =====
import javax.enterprise.inject.New;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;

// ===== EJB: EJBContext deprecated/removed methods in EJB 4.0 =====
import javax.ejb.EJBContext;
import javax.ejb.SessionContext;

// ===== Jakarta Annotations: @ManagedBean deprecated in EE10 =====
import jakarta.annotation.ManagedBean;

// ===== Servlet 6.0: removed methods =====
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.SingleThreadModel;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpUtils;

// ===== Faces EL: removed in Faces 4.0 =====
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.PropertyResolver;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.MethodNotFoundException;
import javax.faces.el.EvaluationException;

// ===== Faces Bean: removed in Faces 4.0 =====
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.NoneScoped;
import javax.faces.bean.ViewScoped;

// ===== Faces Application: StateManager removed methods =====
import javax.faces.application.StateManager;

// ===== Faces Component: removed constants and adapters =====
import javax.faces.component.UIComponent;
import javax.faces.component.MethodBindingValueChangeListener;
import javax.faces.component.MethodBindingValidator;

// ===== Authentication: changed method signatures in EE10 =====
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.module.ClientAuthModule;
import javax.security.auth.message.module.ServerAuthModule;
import javax.security.auth.message.ClientAuth;
import javax.security.auth.message.ServerAuth;

// ===== Authorization: PolicyContext.getContext return type changed =====
import javax.security.jacc.PolicyContext;

// ===== JSON-P: createObjectBuilder(Map) type bound changed =====
import javax.json.Json;
import javax.json.JsonObjectBuilder;

// ===== JSON-B: @JsonbProperty nillable deprecated =====
import javax.json.bind.annotation.JsonbProperty;

// ===== JMS: repeatable annotations added =====
import javax.jms.JMSConnectionFactoryDefinition;
import javax.jms.JMSDestinationDefinition;

// ===== Persistence: AutoCloseable / UUID generation info patterns =====
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GenerationType;

// ===== JAX-RS: deprecated constructors and removed inner classes =====
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Link;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

// ===== SOAP Attachments: SOAPElementFactory removed =====
import javax.xml.soap.SOAPElementFactory;

// ===== XML Binding: Validator removed =====
import javax.xml.bind.Validator;

// ===== EL Resolvers: getFeatureDescriptors deprecated =====
import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.BeanNameELResolver;
import javax.el.CompositeELResolver;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ELContext;
import javax.el.LambdaExpression;

/**
 * Comprehensive Java EE 8 legacy application that exercises ALL Java-detectable
 * patterns from the jakarta10 advisor config (EE 8 to EE 10 migration).
 *
 * Compilation is skipped in this test module — the advisor performs text-based
 * scanning only. All code is guarded by "if (false)" to avoid execution.
 */
@ManagedBean(name = "ee10LegacyApp") // faces.bean.ManagedBean removed in Faces 4.0
public class EE10LegacyApp {

    // ===== JSON-B: @JsonbProperty nillable deprecated =====
    @JsonbProperty(nillable = true)
    private String nillableJsonField;

    // ===== JMS: repeatable annotation triggers =====
    @JMSConnectionFactoryDefinition(name = "java:/jms/myFactory",
        interfaceName = "javax.jms.ConnectionFactory",
        resourceAdapter = "activemq-rar")
    @JMSDestinationDefinition(name = "java:/jms/queue/myQueue",
        interfaceName = "javax.jms.Queue",
        resourceAdapter = "activemq-rar")
    public void jmsAnnotatedMethod() {}

    @SuppressWarnings({"deprecation", "unused"})
    public static void exerciseEE10Patterns() {
        if (false) {

            // ===== EJB 4.0: getCallerIdentity() removed (relied on java.security.Identity) =====
            EJBContext ejbContext = null;
            java.security.Identity callerIdentity = ejbContext.getCallerIdentity();

            // ===== EJB 4.0: getEnvironment() deprecated (use @Resource injection instead) =====
            java.util.Properties envProperties = ejbContext.getEnvironment();

            // ===== CDI 4.0: enterprise.inject.New removed =====
            New newQualifier = null; // usage of removed @New qualifier

            // ===== CDI 4.0: Bean.isNullable() removed =====
            Bean<?> bean = null;
            boolean nullable = bean.isNullable();

            // ===== CDI 4.0: BeanManager.fireEvent() signature changed =====
            BeanManager bm = null;
            bm.fireEvent(new Object());

            // ===== CDI 4.0: BeanManager.createInjectionTarget() signature changed =====
            AnnotatedType<?> at = null;
            InjectionTarget<?> injTarget = bm.createInjectionTarget(at);

            // ===== CDI 4.0: BeforeBeanDiscovery.addAnnotatedType(AnnotatedType) overload changed =====
            BeforeBeanDiscovery bbd = null;
            bbd.addAnnotatedType(at);

            // ===== Jakarta Annotations 2.1: @ManagedBean deprecated =====
            // Referenced via import jakarta.annotation.ManagedBean above

            // ===== Servlet 6.0: ServletContext removed methods =====
            ServletContext sc = null;
            Servlet foundServlet = sc.getServlet("myServlet");
            java.util.Enumeration<Servlet> allServlets = sc.getServlets();
            java.util.Enumeration<String> servletNames = sc.getServletNames();
            sc.log(new Exception("legacy log"), "legacy message");

            // ===== Servlet 6.0: ServletRequest.getRealPath removed =====
            HttpServletRequest req = null;
            String realPath = req.getRealPath("/index.html");
            boolean fromUrl = req.isRequestedSessionIdFromUrl();

            HttpServletRequestWrapper reqWrapper = null;
            String realPath2 = reqWrapper.getRealPath("/page.jsp");
            boolean fromUrl2 = reqWrapper.isRequestedSessionIdFromUrl();

            // ===== Servlet 6.0: HttpServletResponse removed methods =====
            HttpServletResponse resp = null;
            String encodedUrl = resp.encodeUrl("/path");
            String encodedRedirectUrl = resp.encodeRedirectUrl("/redirect");
            resp.setStatus(200, "OK");

            HttpServletResponseWrapper respWrapper = null;
            String encodedUrl2 = respWrapper.encodeUrl("/path2");
            String encodedRedirectUrl2 = respWrapper.encodeRedirectUrl("/redirect2");
            respWrapper.setStatus(404, "Not Found");

            // ===== Servlet 6.0: HttpSession removed methods =====
            HttpSession session = null;
            HttpSessionContext sessionCtx = session.getSessionContext();
            Object sessionValue = session.getValue("myKey");
            String[] valueNames = session.getValueNames();
            session.putValue("myKey", "myValue");
            session.removeValue("myKey");

            // ===== Servlet 6.0: removed classes =====
            SingleThreadModel stm = null; // servlet.SingleThreadModel removed
            HttpSessionContext hsc = null; // servlet.http.HttpSessionContext removed
            HttpUtils hu = null;          // servlet.http.HttpUtils removed

            // ===== Servlet 6.0: UnavailableException removed constructors =====
            Servlet targetServlet = null;
            UnavailableException ue1 = new UnavailableException(targetServlet, "unavailable");
            UnavailableException ue2 = new UnavailableException(60, targetServlet, "unavailable");
            Servlet unavailServlet = ue1.getServlet();

            // ===== JASPIC/Authentication 3.0: changed method signatures =====
            MessageInfo mi = null;
            java.util.Map rawMap = mi.getMap();

            AuthConfigFactory acf = null;
            acf.getFactorySecurityPermission();
            acf.setFactorySecurityPermission(null);
            acf.providerRegistrationSecurityPermission();

            ClientAuthConfig cac = null;
            Object cAuthCtx = cac.getAuthContext("id", null, java.util.Collections.emptyMap());

            ServerAuthConfig sac = null;
            Object sAuthCtx = sac.getAuthContext("id", null, java.util.Collections.emptyMap());

            // ===== JACC 2.1: PolicyContext.getContext return type changed =====
            Object pctx = PolicyContext.getContext("javax.servlet.http.HttpServletRequest");

            // ===== JSON-P 2.1: createObjectBuilder(Map) type bound changed =====
            java.util.Map<String, Object> jsonMap = new java.util.HashMap<>();
            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder(jsonMap);

            // ===== JAX-RS 3.1: deprecated Cookie/NewCookie constructors =====
            Cookie jaxrsCookie = new Cookie("name", "value");
            NewCookie jaxrsNewCookie = new NewCookie("name", "value");
            Link.JaxbLink jaxbLink = new Link.JaxbLink();
            Link.JaxbAdapter jaxbAdapter = new Link.JaxbAdapter();

            // ===== Faces 4.0: removed EL classes =====
            MethodBinding mb = null;
            ValueBinding vb = null;
            VariableResolver vr = null;
            PropertyResolver pr = null;
            PropertyNotFoundException pnfe = null;
            MethodNotFoundException mnfe = null;
            EvaluationException ee = null;
            ReferenceSyntaxException rse = null;

            // ===== Faces 4.0: removed bean scope classes =====
            // Triggered by imports above - ApplicationScoped, NoneScoped, ViewScoped, SessionScoped, RequestScoped
            Object appScopedObj = null; // javax.faces.bean.ApplicationScoped removed
            Object reqScopedObj = null; // javax.faces.bean.RequestScoped removed
            Object sesScopedObj = null; // javax.faces.bean.SessionScoped removed
            Object noneScopedObj = null; // javax.faces.bean.NoneScoped removed
            Object viewScopedObj = null; // javax.faces.bean.ViewScoped removed

            // ===== Faces 4.0: StateManager removed methods =====
            StateManager sm = null;
            StateManager.SerializedView sv = sm.saveSerializedView(null);
            StateManager.SerializedView sv2 = sm.saveView(null);
            Object treeStruct = sm.getTreeStructureToSave(null);
            Object compState = sm.getComponentStateToSave(null);
            sm.writeState(null, sv);
            Object restoredView = sm.restoreView(null, null, null);
            Object restoredTree = sm.restoreTreeStructure(null, null, null);
            sm.restoreComponentState(null, null, null, null);

            // ===== Faces 4.0: UIComponent removed constants =====
            String currentComp = UIComponent.CURRENT_COMPONENT;
            String currentComposite = UIComponent.CURRENT_COMPOSITE_COMPONENT;
            String honorAttrs = UIComponent.HONOR_CURRENT_COMPONENT_ATTRIBUTES_PARAM_NAME;

            // ===== Faces 4.0: removed MethodBinding adapters =====
            MethodBindingValueChangeListener mbvcl = null;
            MethodBindingValidator mbv = null;

            // ===== JSP/EL: getFeatureDescriptors deprecated in EL 5.0 =====
            ELContext elCtx = null;
            ArrayELResolver arrRes = new ArrayELResolver();
            arrRes.getFeatureDescriptors(elCtx, null);

            BeanELResolver beanRes = new BeanELResolver();
            beanRes.getFeatureDescriptors(elCtx, null);

            BeanNameELResolver beanNameRes = null;
            beanNameRes.getFeatureDescriptors(elCtx, null);

            CompositeELResolver compRes = new CompositeELResolver();
            compRes.getFeatureDescriptors(elCtx, null);

            ListELResolver listRes = new ListELResolver();
            listRes.getFeatureDescriptors(elCtx, null);

            MapELResolver mapRes = new MapELResolver();
            mapRes.getFeatureDescriptors(elCtx, null);

            // ===== EL 5.0: LambdaExpression.isParmetersProvided() removed (typo is in spec) =====
            LambdaExpression lambda = null;
            lambda.isParmetersProvided();

            // ===== Persistence 3.1: AutoCloseable / UUID generation info patterns =====
            EntityManager em = null;
            EntityManagerFactory emf = null;
            GenerationType genType = GenerationType.UUID;

            // ===== SOAP Attachments: SOAPElementFactory removed =====
            SOAPElementFactory soapFactory = null;

            // ===== XML Binding: Validator removed =====
            Validator xmlValidator = null;
        }
    }
}

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

// ===== Authentication 3.1: SecurityPermission methods removed =====
import jakarta.security.auth.message.config.AuthConfigFactory;

// ===== Concurrency 3.1: prefer jakarta.enterprise.concurrent over ejb.Schedule =====
import jakarta.ejb.Schedule;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;

// ===== Expression Language 6.0: ELResolver.getFeatureDescriptors removed =====
import jakarta.el.ELResolver;
import jakarta.el.ELContext;

// ===== Server Pages EL resolver getFeatureDescriptors removed =====
import jakarta.el.ArrayELResolver;
import jakarta.el.BeanELResolver;
import jakarta.el.BeanNameELResolver;
import jakarta.el.CompositeELResolver;
import jakarta.el.ListELResolver;
import jakarta.el.MapELResolver;

// ===== Server Pages: JspException.getRootCause and ErrorData constructor removed =====
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.ErrorData;

// ===== WebSocket 2.2: SendResult(Throwable) constructor deprecated =====
import jakarta.websocket.SendResult;

// ===== Servlet 6.1: Cookie/SessionCookieConfig deprecated methods =====
import jakarta.servlet.http.Cookie;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.http.PushBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

// ===== RESTful WS 4.0: Cookie/NewCookie constructors deprecated, JaxbLink/JaxbAdapter removed =====
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;

// ===== Authorization 3.0: Policy, PolicyContext, JaccConfigurationFactory =====
import jakarta.security.jacc.PolicyContext;
import java.security.Policy;

// ===== Faces 4.1: removed events, ActionSource2, deprecated constants =====
import jakarta.faces.event.PostConstructCustomScopeEvent;
import jakarta.faces.event.PreDestroyCustomScopeEvent;
import jakarta.faces.component.ActionSource2;
import jakarta.faces.view.ActionSource2AttachedObjectHandler;
import jakarta.faces.view.ActionSource2AttachedObjectTarget;

// ===== Persistence 3.2: additional deprecations/removals =====
import jakarta.persistence.EntityGraph;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.MapAttribute;

/**
 * Comprehensive Java EE 10 legacy application that exercises ALL Java-detectable
 * patterns from the jakarta11 advisor config (EE 10 to EE 11 migration)
 * not already covered by EE11Compatibility.java or LegacyBean.java.
 *
 * Compilation is skipped in this test module — the advisor performs text-based
 * scanning only. All code is guarded by "if (false)" to avoid execution.
 */
public class EE11LegacyApp {

    // ===== Persistence 3.2: @PersistenceContext may cause CDI ambiguous dependency =====
    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings({"deprecation", "unused"})
    public static void exerciseEE11Patterns() {
        if (false) {

            // ===== Authentication 3.1: getFactorySecurityPermission removed =====
            AuthConfigFactory acf = AuthConfigFactory.getFactory();
            acf.getFactorySecurityPermission();
            acf.setFactorySecurityPermission(null);
            acf.providerRegistrationSecurityPermission();

            // ===== Concurrency 3.1: prefer ContextService/ManagedExecutor over @ejb.Schedule =====
            // These are info-level patterns flagging that newer alternatives exist
            ContextService cs = null;         // prefer @Inject ContextService
            ManagedExecutorService mes = null; // prefer @Inject ManagedExecutorService
            ManagedScheduledExecutorService mses = null; // prefer @Inject ManagedScheduledExecutorService
            ManagedThreadFactory mtf = null;  // prefer @Inject ManagedThreadFactory
            Asynchronous async = null;        // jakarta.enterprise.concurrent.Asynchronous available
            Schedule schedule = null;         // consider migrating from @ejb.Schedule

            // ===== Expression Language 6.0: ELResolver.getFeatureDescriptors removed =====
            ELResolver elResolver = null;
            ELContext elContext = null;
            elResolver.getFeatureDescriptors(elContext, null);

            // ===== Server Pages EL resolvers: getFeatureDescriptors removed =====
            ArrayELResolver arrRes = new ArrayELResolver();
            arrRes.getFeatureDescriptors(elContext, null);

            BeanELResolver beanRes = new BeanELResolver();
            beanRes.getFeatureDescriptors(elContext, null);

            BeanNameELResolver beanNameRes = null;
            beanNameRes.getFeatureDescriptors(elContext, null);

            CompositeELResolver compRes = new CompositeELResolver();
            compRes.getFeatureDescriptors(elContext, null);

            ListELResolver listRes = new ListELResolver();
            listRes.getFeatureDescriptors(elContext, null);

            MapELResolver mapRes = new MapELResolver();
            mapRes.getFeatureDescriptors(elContext, null);

            // ===== Server Pages: JspException.getRootCause() removed =====
            JspException jspEx = new JspException("message");
            Throwable rootCause = jspEx.getRootCause();

            // ===== Server Pages: ErrorData constructor removed =====
            ErrorData errorData = new ErrorData(new Throwable(), 500, "/error", "fish.payara.MyServlet");

            // ===== WebSocket 2.2: SendResult(Throwable) constructor deprecated =====
            Throwable wsError = new RuntimeException("websocket error");
            SendResult sendResult = new SendResult(wsError);

            // ===== Servlet 6.1: Cookie.setComment() deprecated for removal =====
            Cookie cookie = new Cookie("name", "value");
            cookie.setComment("legacy comment");
            cookie.setVersion(0);

            // ===== Servlet 6.1: SessionCookieConfig.getComment/setComment deprecated for removal =====
            SessionCookieConfig scc = null;
            String sccComment = scc.getComment();
            scc.setComment("session-cookie-comment");

            // ===== Servlet 6.1: PushBuilder deprecated in favor of 103 Early Hints =====
            HttpServletRequest req = null;
            PushBuilder pb = req.newPushBuilder();

            HttpServletRequestWrapper reqWrapper = null;
            PushBuilder pb2 = reqWrapper.newPushBuilder();

            PushBuilder pushBuilder = null; // reference to jakarta.servlet.http.PushBuilder

            // ===== RESTful WS 4.0: Cookie() no-arg constructor deprecated =====
            jakarta.ws.rs.core.Cookie noArgCookie = new jakarta.ws.rs.core.Cookie("name", "value");

            // ===== RESTful WS 4.0: Cookie(String,String,String,String) deprecated =====
            jakarta.ws.rs.core.Cookie fullCookie = new jakarta.ws.rs.core.Cookie("name", "value", "/path", "example.com");

            // ===== RESTful WS 4.0: Cookie(String,String,String,String,int) deprecated =====
            jakarta.ws.rs.core.Cookie versionedCookie = new jakarta.ws.rs.core.Cookie("name", "value", "/path", "example.com", 1);

            // ===== RESTful WS 4.0: NewCookie additional deprecated constructors =====
            NewCookie nc3 = new NewCookie("name", "value", "/path", "domain", 0, "comment", 3600, false);
            NewCookie nc4 = new NewCookie("name", "value", "/path", "domain", "comment", 3600, false);
            NewCookie nc5 = new NewCookie("name", "value", "/path", "domain", "comment", 3600, false, true);

            // ===== RESTful WS 4.0: Link.JaxbLink and JaxbAdapter constructors removed =====
            Link.JaxbLink jaxbLink = new Link.JaxbLink();
            Link.JaxbAdapter jaxbAdapter = new Link.JaxbAdapter();

            // ===== RESTful WS 4.0: CacheControl.valueOf(String) deprecated =====
            CacheControl cc = CacheControl.valueOf("no-cache, no-store");

            // ===== RESTful WS 4.0: MediaType SVG XML constants deprecated =====
            String svgXml = MediaType.APPLICATION_SVG_XML;
            MediaType svgXmlType = MediaType.APPLICATION_SVG_XML_TYPE;

            // ===== Authorization 3.0: System.getSecurityManager removed =====
            SecurityManager sm = System.getSecurityManager();

            // ===== Authorization 3.0: PolicyContext.getContext return type changed =====
            Object pctx = PolicyContext.getContext("jakarta.servlet.http.HttpServletRequest");

            // ===== Authorization 3.0: java.security.Policy removed =====
            Policy policy = Policy.getPolicy();

            // ===== Authorization 3.0: Payara JaccConfigurationFactory removed =====
            // Reference the known removed factory class
            // fish.payara.jacc.JaccConfigurationFactory is removed in EE 11
            Object jaccFactory = null; // fish.payara.jacc.JaccConfigurationFactory

            // ===== Faces 4.1: PostConstructCustomScopeEvent deprecated =====
            PostConstructCustomScopeEvent postConstruct = null;

            // ===== Faces 4.1: PreDestroyCustomScopeEvent deprecated =====
            PreDestroyCustomScopeEvent preDestroy = null;

            // ===== Faces 4.1: ActionSource2 deprecated =====
            ActionSource2 actionSource2 = null;

            // ===== Faces 4.1: ActionSource2AttachedObjectHandler deprecated =====
            ActionSource2AttachedObjectHandler as2Handler = null;

            // ===== Faces 4.1: ActionSource2AttachedObjectTarget deprecated =====
            ActionSource2AttachedObjectTarget as2Target = null;

            // ===== Persistence 3.2: PersistenceUnitTransactionType deprecated for removal =====
            PersistenceUnitTransactionType txType = PersistenceUnitTransactionType.JTA;

            // ===== Persistence 3.2: Persistence() constructor deprecated for removal =====
            Persistence persistenceInstance = new Persistence();

            // ===== Persistence 3.2: Persistence.providers field deprecated for removal =====
            java.util.List<?> providers = Persistence.providers;

            // ===== Persistence 3.2: EntityGraph.addSubclassSubgraph deprecated for removal =====
            EntityGraph<?> graph = null;
            graph.addSubclassSubgraph(Object.class);

            // ===== Persistence 3.2: EntityGraph.addSubgraph(Attribute, Class) deprecated for removal =====
            Attribute<?, ?> attr = null;
            graph.addSubgraph(attr, Object.class);

            // ===== Persistence 3.2: EntityGraph.addKeySubgraph(MapAttribute, Class) deprecated for removal =====
            MapAttribute<?, ?, ?> mapAttr = null;
            graph.addKeySubgraph(mapAttr, Object.class);

            // ===== Persistence 3.2: EntityGraph.addKeySubgraph(MapAttribute) deprecated for removal =====
            graph.addKeySubgraph(mapAttr);

            // ===== Persistence 3.2: Query.setParameter(String, Date, TemporalType) deprecated =====
            Query query = null;
            query.setParameter("namedParam", new java.util.Date(), jakarta.persistence.TemporalType.DATE);

            // ===== Persistence 3.2: TypedQuery.setParameter(String, Date, TemporalType) deprecated =====
            TypedQuery<String> typedQuery = null;
            typedQuery.setParameter("namedParam", new java.util.Date(), jakarta.persistence.TemporalType.TIMESTAMP);

            // ===== Authorization 3.0: System.getSecurityManager() already covered above =====
        }
    }
}

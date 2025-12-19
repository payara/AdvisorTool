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
package fish.payara.advisor.test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import javax.annotation.security.RolesAllowed;
import javax.annotation.security.PermitAll;
import javax.annotation.security.DeclareRoles;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import javax.el.ELProcessor;

import javax.inject.Inject;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;

import javax.json.Json;
import javax.json.JsonObject;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.transaction.Transactional;

import javax.websocket.OnMessage;
import javax.websocket.server.ServerEndpoint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import java.io.IOException;
import java.io.StringWriter;

/**
 * A purposely oversized Jakarta EE 8 example using many APIs
 * that are removed or renamed in Jakarta EE 10+.
 */
@Singleton
@Startup
@Remote(ExampleDeprecatedAPI.RemoteApi.class)
@DeclareRoles({"admin", "user"})
@Path("/legacy")
@ServerEndpoint("/ws/legacy")
public class ExampleDeprecatedAPI extends HttpServlet {

    // ===============================
    // EJB / Resources
    // ===============================

    @Resource
    private TimerService timerService;

    @EJB
    private LegacyEJB legacyEjb;

    @PersistenceContext
    private EntityManager em;

    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "mail/Session")
    private Session mailSession;

    @Inject
    private ELProcessor elProcessor;

    // ===============================
    // Lifecycle
    // ===============================

    @PostConstruct
    private void init() {
        timerService.createTimer(5000, "legacy timeout");
    }

    @PreDestroy
    private void shutdown() {
        System.out.println("Shutting down legacy component");
    }

    // ===============================
    // EJB Timeout – removed in EE 10
    // ===============================
    @Timeout
    public void onTimeout(Timer timer) {
        System.out.println("Timer triggered: " + timer.getInfo());
    }

    // ===============================
    // JMS 1.x – moved to jakarta.jms.*
    // ===============================
    public void sendJmsMessage() {
        try (JMSContext ctx = connectionFactory.createContext()) {
            Queue q = ctx.createQueue("queue/test");
            ctx.createProducer().send(q, "Legacy message");
        }
    }

    // ===============================
    // JSON-P (javax.json → jakarta.json)
    // ===============================
    public JsonObject buildJson() {
        return Json.createObjectBuilder()
                .add("status", "deprecated")
                .build();
    }

    // ===============================
    // JAXB (javax.xml.bind removed from JDK)
    // ===============================
    public String marshalXml(Object obj) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(obj.getClass());
        Marshaller m = ctx.createMarshaller();
        StringWriter writer = new StringWriter();
        m.marshal(obj, writer);
        return writer.toString();
    }

    // ===============================
    // JavaMail (javax.mail → jakarta.mail)
    // ===============================
    public void sendMail() throws Exception {
        MimeMessage msg = new MimeMessage(mailSession);
        msg.setSubject("Legacy Email");
        msg.setText("This API moved in Jakarta EE 10.");
        System.out.println("Email prepared (not actually sent).");
    }

    // ===============================
    // JAX-RS – old javax.ws.rs.* package
    // ===============================
    @GET
    @Path("/hello")
    @PermitAll
    public String restEndpoint() {
        return "Hello from EE8 legacy!";
    }

    // ===============================
    // WebSocket – javax.websocket.*
    // ===============================
    @OnMessage
    public String onWebSocketMessage(String msg) {
        return "Echo: " + msg;
    }

    // ===============================
    // Servlet – javax.servlet.*
    // ===============================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        ServletContext ctx = req.getServletContext();
        ctx.log("Legacy servlet invoked");
        resp.getWriter().println("Servlet using javax.* APIs");
    }

    // ===============================
    // Transaction (javax.transaction)
    // ===============================
    @Transactional
    public void doDatabaseWork() {
        legacyEjb.perform();
    }

    // ===============================
    // Inner EJB – for more javax.* API usage
    // ===============================
    @Remote
    public interface RemoteApi {
        void call();
    }

    @Singleton
    public static class LegacyEJB {
        public void perform() {
            System.out.println("Legacy EJB method executed");
        }
    }
}

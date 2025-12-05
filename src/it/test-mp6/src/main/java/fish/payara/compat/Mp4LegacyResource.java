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
package com.example.mp4sample;

// ------------------------------
// Legacy javax.* imports (MP6 requires jakarta.*)
// ------------------------------
import javax.inject.Inject;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

// ------------------------------
// Removed in MicroProfile 6: OpenTracing
// ------------------------------
import org.eclipse.microprofile.opentracing.Traced;

// ------------------------------
// MicroProfile Config
// (Still exists in MP6, but scanning unresolved @ConfigProperty is useful)
// ------------------------------
import org.eclipse.microprofile.config.inject.ConfigProperty;

// ------------------------------
// Metrics: All annotations that change in MP6 (Metrics 5)
// ------------------------------
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Metered;
//import org.eclipse.microprofile.metrics.annotation.ConcurrentGauge;
//import org.eclipse.microprofile.metrics.MetricUnits;

@ApplicationScoped
@Path("/mp4-legacy")
public class Mp4LegacyResource {

    // MicroProfile Config – unresolved or optional property
    @Inject
    @ConfigProperty(name = "my.legacy.property")
    String legacyProperty;

    // Counted annotation — behavior changes in MicroProfile Metrics 5 (MP6)
    @GET
    @Path("/count")
    @Counted(
        name = "legacy_count",
        description = "Example Counted metric in MP4"
    )
    public String count() {
        return "count called: " + legacyProperty;
    }

    // Timed annotation — unit semantics changed in Metrics 5
    @GET
    @Path("/time")
    @Timed(
        name = "legacy_timer",
        description = "Example Timed metric",
        unit = MetricUnits.MILLISECONDS // some units deprecated/changed
    )
    public String time() {
        return "timed call";
    }

    // Gauge annotation — changed behavior & requirements in Metrics 5
    @Gauge(
        name = "legacy_gauge",
        unit = MetricUnits.NONE,
        description = "Example Gauge metric"
    )
    public int gaugeValue() {
        return 42;
    }

    // Metered — removed / replaced with different concepts in Metrics 5
    @GET
    @Path("/meter")
    @Metered(
        name = "legacy_meter",
        description = "Metered metric example"
    )
    public String meter() {
        return "metered call";
    }

    // ConcurrentGauge — removed from Metrics 5 (not supported in MP6)
    /*@GET
    @Path("/concurrent")
    //@ConcurrentGauge(
        name = "legacy_concurrent_gauge",
        description = "Concurrent gauge example"
    )
    public String concurrent() {
        return "concurrent gauge call";
    }*/

    // ------------------------------
    // OpenTracing — Removed in MP 6
    // ------------------------------
    @GET
    @Path("/trace")
    @Traced(operationName = "legacyTracingOperation")
    public String trace() {
        return "tracing call";
    }
}

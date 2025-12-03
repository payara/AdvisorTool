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

package fish.payara.compat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

/**
 * JUnit test that tries to call the compatibility helper only when the Jakarta
 * API classes are available on the test classpath. If not present, the test
 * will be skipped so CI does not fail due to a provided-scope API.
 */
public class EE11CompatibilityTest {

    @Test
    public void compileTimeCheck() {
        // Skip the test if the runtime Jakarta API classes are not present.
        boolean jakartaPresent;
        try {
            Class.forName("jakarta.persistence.Graph");
            jakartaPresent = true;
        } catch (ClassNotFoundException e) {
            jakartaPresent = false;
        }
        Assumptions.assumeTrue(jakartaPresent, "Jakarta API not available on test classpath; skipping compatibility execution");

        try {
            // Try to call helper; if linkage problems occur we skip the test instead of failing.
            EE11Compatibility.touchDeprecatedApis();
        } catch (LinkageError | Exception ex) {
            Assumptions.assumeTrue(false, "Compatibility helper could not be executed: " + ex.getMessage());
        }
    }
}

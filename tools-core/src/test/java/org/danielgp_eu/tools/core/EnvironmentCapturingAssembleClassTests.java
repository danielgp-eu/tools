package org.danielgp_eu.tools.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testing for EnvironmentCapturingAssembleClass
 */
class EnvironmentCapturingAssembleClassTests {

    @Test
    @DisplayName("Simple test to check if environment details gathering results returns a valid JSON")
    void testPackageCurrentEnvironmentDetailsIntoJson() {
        final String handled = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoJson();
        final boolean isJsonValid = JsonOperationsClass.isJsonValid(handled);
        assertTrue(isJsonValid, String.format("JSON produced by environment gathering logic does not seem to be valid... %s", handled));
    }

    @Test
    @DisplayName("Check if environment details gathering results is not null")
    void testPackageCurrentEnvironmentDetailsIntoListOfProperties() {
        final List<Properties> handled = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoListOfProperties();
        assertNotNull(handled, String.format("Environment gathering logic should not be null... %s", handled));
    }

    /**
     * Constructor
     */
    public EnvironmentCapturingAssembleClassTests() {
        // intentionally blank
    }

}

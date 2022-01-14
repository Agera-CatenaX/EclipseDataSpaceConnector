package org.eclipse.dataspaceconnector.system.runtime;

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

public class OtelRuntime {
    public static void main(String[] args) {
        AutoConfiguredOpenTelemetrySdk.initialize();

        BaseRuntime.main(args);
    }
}

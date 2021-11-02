package org.microsoft.extension.prs;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

/**
 * This class contains endpoints added to the consumer, so that a PRS client can request parts tree through consumer.
 * This class should not contain PRS business logic. The business logic of PRS should be inside the PRS application.
 */

@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Path("/")
public class PRSApiController {

    // Monitor is a logger.
    private final Monitor monitor;

    public PRSApiController(Monitor monitor) {
        this.monitor = monitor;
    }

    @GET
    @Path("health")
    public String checkHealth() {
        monitor.info("Received a health request");
        return "Consumer is healthy.";
    }
}

package org.eclipse.dataspaceconnector.extensions.api;

import org.eclipse.dataspaceconnector.spi.policy.PolicyRegistry;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowManager;

import java.util.Set;

public class PrsApiExtension implements ServiceExtension {

    // TODO: is it needed?
    @Override
    public Set<String> requires() {
        return Set.of("edc:webservice", PolicyRegistry.FEATURE);
    }

    @Override
    public void initialize(ServiceExtensionContext context) {

        var dataFlowMgr = context.getService(DataFlowManager.class);
        var flowController = new PrsApiFlowController(context.getMonitor(), new PrsApiCaller(), new FileSystemDataWriter(context.getMonitor()));
        dataFlowMgr.register(flowController);
        context.getMonitor().info("PRS API extension initialized!");
    }

}

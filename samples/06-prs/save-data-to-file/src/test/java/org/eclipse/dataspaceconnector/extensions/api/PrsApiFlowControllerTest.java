package org.eclipse.dataspaceconnector.extensions.api;

import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowInitiateResponse;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PrsApiFlowControllerTest {

    private PrsApiFlowController controller;
    private DataReader dataReader;

    @BeforeEach
    void setUp() {
        ServiceExtensionContext context = niceMock(ServiceExtensionContext.class);
        expect(context.getMonitor()).andReturn(niceMock(Monitor.class));

        replay(context);
        dataReader = niceMock(DataReader.class);
        DataWriter dataWriter = niceMock(DataWriter.class);
        controller = new PrsApiFlowController(context.getMonitor(), dataReader, dataWriter);
    }

    @Test
    void verifyDataReaderWasCalled() {
        //given
        expect(dataReader.read(anyObject(DataAddress.class))).andReturn("").times(1);

        var dataRequest = DataRequest.Builder.newInstance()
                .id("YS3DD78N4X7055320")
                .protocol("ids-rest")
                .dataDestination(DataAddress.Builder.newInstance()
                        .type("File")
                        .property("path", "some/path")
                        .build())
                .dataEntry(DataEntry.Builder.newInstance()
                        .policyId("use-eu")
                        .id("prs-api")
                        .catalogEntry(new DummyCatalogEntry()).build())
                .managedResources(false)
                .build();

        //when
        var response = controller.initiateFlow(dataRequest);

        //then
        assertEquals(response, DataFlowInitiateResponse.OK);

    }

    public static class DummyCatalogEntry implements org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataCatalogEntry {
        @Override
        public DataAddress getAddress() {
            return DataAddress.Builder.newInstance()
                    .type("test-source-type")
                    .property("vin", "YS3DD78N4X7055320")
                    .property("view", "AS_BUILD")
                    .property("path", "PARTS_TREE_BY_VIN")
                    .build();
        }
    }
}
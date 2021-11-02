package org.eclipse.dataspaceconnector.extensions.api;

import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowInitiateResponse;
import org.eclipse.dataspaceconnector.spi.types.TypeManager;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PrsApiFlowControllerTest {

    private PrsApiFlowController controller;
    private DataReader dataReader;
    private DataWriter dataWriter;

    @BeforeEach
    void setUp() {
        ServiceExtensionContext context = niceMock(ServiceExtensionContext.class);
        expect(context.getMonitor()).andReturn(niceMock(Monitor.class));

        replay(context);
        TypeManager typeManager = niceMock(TypeManager.class);
        dataReader = niceMock(DataReader.class);
        dataWriter = niceMock(DataWriter.class);
        controller = new PrsApiFlowController(context.getMonitor(), dataReader, dataWriter);
    }

    @Test
    void verifyDataReaderWasCalled() throws Exception {
        //given
        expect(dataReader.read(anyObject(DataAddress.class))).andReturn("").times(1);

        var dataRequest = DataRequest.Builder.newInstance()
                .dataDestination(DataAddress.Builder.newInstance().build())
                .dataEntry(DataEntry.Builder.newInstance()
                        .catalogEntry(new DummyCatalogEntry()).build())
                .build();
        //when
        var response = controller.initiateFlow(dataRequest);

        //then
        assertEquals(response, DataFlowInitiateResponse.OK);

    }

    public static class DummyCatalogEntry implements org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataCatalogEntry {
        @Override
        public DataAddress getAddress() {
            return DataAddress.Builder.newInstance().type("test-source-type").build();
        }
    }
}
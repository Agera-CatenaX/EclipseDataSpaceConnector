package org.eclipse.dataspaceconnector.extensions.api;

import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowController;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowInitiateResponse;
import org.eclipse.dataspaceconnector.spi.transfer.response.ResponseStatus;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.jetbrains.annotations.NotNull;

public class PrsApiFlowController implements DataFlowController {
    private final Monitor monitor;
    private DataReader dataReader;
    private DataWriter dataWriter;

    // TODO: check if TypeManager is needed
    public PrsApiFlowController(Monitor monitor, DataReader apiCaller, DataWriter dataWriter) {
        this.monitor = monitor;
        this.dataReader = apiCaller;
        this.dataWriter = dataWriter;
    }

    @Override
    public boolean canHandle(DataRequest dataRequest) {
        return dataRequest.getDataDestination().getType().equalsIgnoreCase("file");
    }

    @Override
    public @NotNull DataFlowInitiateResponse initiateFlow(DataRequest dataRequest) {
        var source = dataRequest.getDataEntry().getCatalogEntry().getAddress();
        var destination = dataRequest.getDataDestination();

        String response;
        try {
            response = dataReader.read(source);
            monitor.info(response);
        } catch (Exception e) {
            String message = "Error when reading the data: " + e.getMessage();
            monitor.severe(message);
            return new DataFlowInitiateResponse(ResponseStatus.FATAL_ERROR, message);
        }

        try {
            dataWriter.write(destination, response);
        } catch (Exception e) {
            String message = "Error when writing the data: " + e.getMessage();
            monitor.severe(message);
            return new DataFlowInitiateResponse(ResponseStatus.FATAL_ERROR, message);
        }

        return DataFlowInitiateResponse.OK;
    }

}
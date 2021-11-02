package org.eclipse.dataspaceconnector.extensions.api;

import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataAddress;

public interface DataReader {
    String read(DataAddress address);
}

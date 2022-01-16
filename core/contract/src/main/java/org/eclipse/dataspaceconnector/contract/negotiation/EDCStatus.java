package org.eclipse.dataspaceconnector.contract.negotiation;

import java.util.concurrent.atomic.AtomicLong;

public class EDCStatus implements EDCStatusMBean {

    private final AtomicLong negotiationsSaved = new AtomicLong();

    @Override
    public Long getNegotiationsSaved() {
        return negotiationsSaved.get();
    }

    public void incrementNegotiationsSaved() {
        negotiationsSaved.incrementAndGet();
    }
}

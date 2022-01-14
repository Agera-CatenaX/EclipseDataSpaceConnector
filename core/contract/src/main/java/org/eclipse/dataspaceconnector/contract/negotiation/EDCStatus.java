package org.eclipse.dataspaceconnector.contract.negotiation;

public class EDCStatus implements EDCStatusMBean {

    private long negotiationsSaved;

    @Override
    public Long getNegotiationsSaved() {
        return negotiationsSaved;
    }

    public void incrementNegotiationsSaved() {
        negotiationsSaved++;
    }
}

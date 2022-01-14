/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       Fraunhofer Institute for Software and Systems Engineering - extended method implementation
 *
 */
package org.eclipse.dataspaceconnector.contract.negotiation;

import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.agreement.ContractAgreement;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ContractNegotiationStoreMetricsDecorator implements ContractNegotiationStore {

    private final ContractNegotiationStore delegate;
    private final EDCStatusMBean edcStatusMBean;

    public ContractNegotiationStoreMetricsDecorator(ContractNegotiationStore delegate, EDCStatusMBean edcStatusMBean) {
        this.edcStatusMBean = edcStatusMBean;
        this.delegate = delegate;
    }

    @Override
    @Nullable
    public ContractNegotiation find(String negotiationId) {
        return delegate.find(negotiationId);
    }

    @Override
    @Nullable
    public ContractNegotiation findForCorrelationId(String correlationId) {
        return delegate.findForCorrelationId(correlationId);
    }

    @Override
    public @Nullable ContractAgreement findContractAgreement(String contractId) {
        return delegate.findContractAgreement(contractId);
    }

    @Override
    public void save(ContractNegotiation negotiation) {
        delegate.save(negotiation);
        ((EDCStatus)edcStatusMBean).incrementNegotiationsSaved();
    }

    @Override
    public void delete(String negotiationId) {
        delegate.delete(negotiationId);
    }

    @Override
    public @NotNull List<ContractNegotiation> nextForState(int state, int max) {
        return delegate.nextForState(state, max);
    }
}

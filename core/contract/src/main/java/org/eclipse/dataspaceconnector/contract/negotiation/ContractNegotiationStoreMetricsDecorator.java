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

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.agreement.ContractAgreement;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiation;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiationStates;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

public class ContractNegotiationStoreMetricsDecorator implements ContractNegotiationStore {
    private final static OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
    private final static MeterProvider meterProvider = openTelemetry.getMeterProvider();

    private final Meter meter = meterProvider.meterBuilder("edc")
            .build();
    private final LongCounter saveCounter = meter
            .counterBuilder("negotiationsSaved")
            .setDescription("Negotiation repository save operations")
            .setUnit("1")
            .build();
    private final ContractNegotiationStore delegate;

    public ContractNegotiationStoreMetricsDecorator(ContractNegotiationStore delegate) {
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
        var state1 = getStateName(negotiation);
        delegate.save(negotiation);
        var state2 = getStateName(negotiation);
        System.out.println("Adding 1 to " + saveCounter);
        saveCounter.add(1, Attributes.of(
                stringKey("InitialState"), state1,
                stringKey("FinalState"), state2
        ));
    }

    private String getStateName(ContractNegotiation negotiation) {
        return ContractNegotiationStates.from(negotiation.getState()).toString();
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

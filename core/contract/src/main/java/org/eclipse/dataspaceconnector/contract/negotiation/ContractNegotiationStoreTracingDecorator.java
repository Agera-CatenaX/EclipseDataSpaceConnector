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
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.extension.annotations.WithSpan;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.agreement.ContractAgreement;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiation;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiationStates;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ContractNegotiationStoreTracingDecorator implements ContractNegotiationStore {
    private Monitor monitor;
    private final ContractNegotiationStore delegate;
    private final OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
    private final static ContractNegotiationTraceContextMapper traceContextMapper = new ContractNegotiationTraceContextMapper();

    public ContractNegotiationStoreTracingDecorator(ContractNegotiationStore delegate, Monitor monitor) {
        this.delegate = delegate;
        this.monitor = monitor;
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

    @WithSpan(value = "saving negotiation")
    @Override
    public void save(ContractNegotiation negotiation) {
        Span span = Span.current();
        span.setAttribute("negotiationState", getStateName(negotiation));

        monitor.debug("Injecting trace context into contract negotiation.");
        openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), negotiation, traceContextMapper);
        monitor.debug("Trace context: " + negotiation.getTraceContextString());

        monitor.debug("Saving negotiation in state " + getStateName(negotiation));
        delegate.save(negotiation);

        span.addEvent("Saved", Attributes.builder().put("negotiationState", getStateName(negotiation)).build());
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

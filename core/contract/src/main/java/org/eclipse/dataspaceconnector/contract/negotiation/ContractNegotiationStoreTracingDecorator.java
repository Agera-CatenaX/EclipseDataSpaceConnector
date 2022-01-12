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
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.extension.annotations.WithSpan;
import org.eclipse.dataspaceconnector.spi.contract.negotiation.store.ContractNegotiationStore;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.agreement.ContractAgreement;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiation;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.negotiation.ContractNegotiationStates;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.opentelemetry.api.trace.StatusCode.ERROR;

public class ContractNegotiationStoreTracingDecorator implements ContractNegotiationStore {
    private final ContractNegotiationStore delegate;
    private final OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
    private final Tracer tracer = openTelemetry.getTracer("edc");
    private final static ContractNegotiationTraceContextMapper traceContextMapper = new ContractNegotiationTraceContextMapper();

    public ContractNegotiationStoreTracingDecorator(ContractNegotiationStore delegate) {
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

    @WithSpan(value = "saving negotiation")
    @Override
    public void save(ContractNegotiation negotiation) {
        Span span = Span.current();
        span.setAttribute("negotiationState", getStateName(negotiation));
        openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), negotiation, traceContextMapper);
        delegate.save(negotiation);
        span.addEvent("Saved", Attributes.builder().put("State", getStateName(negotiation)).build());
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

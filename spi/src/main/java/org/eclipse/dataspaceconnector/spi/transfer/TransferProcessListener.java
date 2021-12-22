/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.dataspaceconnector.spi.transfer;

import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcessStates;

/**
 * Interface implemented by listeners registered to observe transfer process
 * state changes via {@link TransferProcessObservable#registerListener(TransferProcessListener)}.
 * <p>
 * Note that the listener may be called more than once, in case the application
 * restarts, and a persistent transfer store implementation is used.
 */
public interface TransferProcessListener {
    /**
     * Called after a {@link TransferProcess} has moved to state
     * {@link TransferProcessStates#INITIAL INITIAL}.
     *
     * @param process the transfer process whose state has changed.
     */
    default void created(TransferProcess process) {
    }

    /**
     * Called after a {@link TransferProcess} has moved to state
     * {@link TransferProcessStates#PROVISIONING PROVISIONING}.
     *
     * @param process the transfer process whose state has changed.
     */
    default void provisioning(TransferProcess process) {
    }

    /**
     * Called after a {@link TransferProcess} has moved to state
     * {@link TransferProcessStates#PROVISIONED PROVISIONED}.
     *
     * @param process the transfer process whose state has changed.
     */
    default void provisioned(TransferProcess process) {
    }

    /**
     * Called after a {@link TransferProcess} has moved to state
     * {@link TransferProcessStates#IN_PROGRESS IN_PROGRESS}.
     *
     * @param process the transfer process whose state has changed.
     */
    default void inProgress(TransferProcess process) {
    }

    /**
     * Called after a {@link TransferProcess} has moved to state
     * {@link TransferProcessStates#COMPLETED COMPLETED}.
     *
     * @param process the transfer process whose state has changed.
     */
    default void completed(TransferProcess process) {
    }

    /**
     * Called after a {@link TransferProcess} has moved to state
     * {@link TransferProcessStates#DEPROVISIONING DEPROVISIONING}.
     *
     * @param process the transfer process whose state has changed.
     */
    default void deprovisioning(TransferProcess process) {
    }

    /**
     * Called after a {@link TransferProcess} has moved to state
     * {@link TransferProcessStates#DEPROVISIONED DEPROVISIONED}.
     *
     * @param process the transfer process whose state has changed.
     */
    default void deprovisioned(TransferProcess process) {
    }

    /**
     * Called after a {@link TransferProcess} has moved to state
     * {@link TransferProcessStates#ENDED ENDED}.
     *
     * @param process the transfer process whose state has changed.
     */
    default void ended(TransferProcess process) {
    }

    /**
     * Called after a {@link TransferProcess} has moved to state
     * {@link TransferProcessStates#ERROR ERROR}.
     *
     * @param process the transfer process whose state has changed.
     */
    default void error(TransferProcess process) {
    }

    /**
     * Called after a {@link TransferProcess} has moved to state
     * {@link TransferProcessStates#REQUESTED REQUESTED}.
     *
     * @param process the transfer process whose state has changed.
     */
    default void requested(TransferProcess process) {
    }

    /**
     * Called after a {@link TransferProcess} has moved to state
     * {@link TransferProcessStates#REQUESTED_ACK REQUESTED_ACK}.
     *
     * @param process the transfer process whose state has changed.
     */
    default void requestAck(TransferProcess process) {
    }

    /**
     * Called after a {@link TransferProcess} has moved to state
     * {@link TransferProcessStates#DEPROVISIONING_REQ DEPROVISIONING_REQ}.
     *
     * @param process the transfer process whose state has changed.
     */
    default void deprovisioningRequested(TransferProcess process) {
    }
}

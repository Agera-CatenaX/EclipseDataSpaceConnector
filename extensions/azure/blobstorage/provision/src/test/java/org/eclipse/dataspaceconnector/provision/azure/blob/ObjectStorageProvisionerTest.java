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

package org.eclipse.dataspaceconnector.provision.azure.blob;

import com.azure.storage.blob.models.BlobStorageException;
import net.jodah.failsafe.RetryPolicy;
import org.eclipse.dataspaceconnector.common.azure.BlobStoreApi;
import org.eclipse.dataspaceconnector.provision.azure.AzureSasToken;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.transfer.response.ResponseStatus;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.ProvisionedResource;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.ResourceDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ObjectStorageProvisionerTest {

    private final BlobStoreApi blobStoreApiMock = mock(BlobStoreApi.class);
    private ObjectStorageProvisioner provisioner;

    @BeforeEach
    void setup() {
        RetryPolicy<Object> retryPolicy = new RetryPolicy<>().withMaxRetries(0);
        provisioner = new ObjectStorageProvisioner(retryPolicy, mock(Monitor.class), blobStoreApiMock);
    }

    @Test
    void canProvision() {
        assertThat(provisioner.canProvision(new ObjectStorageResourceDefinition())).isTrue();
        assertThat(provisioner.canProvision(new ResourceDefinition() {
        })).isFalse();
    }

    @Test
    void canDeprovision() {
        assertThat(provisioner.canDeprovision(new ObjectContainerProvisionedResource())).isTrue();
        assertThat(provisioner.canDeprovision(new ProvisionedResource() {
        })).isFalse();
    }

    @Test
    void deprovision_should_not_do_anything() {
        var result = provisioner.deprovision(new ObjectContainerProvisionedResource());

        assertThat(result).succeedsWithin(1, SECONDS);
    }

    @Test
    void provision_success() {
        var resourceDef = resourceDefinition().transferProcessId("tpId").build();
        String accountName = resourceDef.getAccountName();
        String containerName = resourceDef.getContainerName();
        when(blobStoreApiMock.exists(anyString(), anyString())).thenReturn(false);
        when(blobStoreApiMock.createContainerSasToken(eq(accountName), eq(containerName), eq("w"), any())).thenReturn("some-sas");

        var response = provisioner.provision(resourceDef).join();

        assertThat(response.getResource()).isInstanceOfSatisfying(ObjectContainerProvisionedResource.class, resource -> {
            assertThat(resource.getTransferProcessId()).isEqualTo("tpId");
        });
        assertThat(response.getSecretToken()).isInstanceOfSatisfying(AzureSasToken.class, secretToken -> {
            assertThat(secretToken.getSas()).isEqualTo("?some-sas");
        });
        verify(blobStoreApiMock).exists(anyString(), anyString());
        verify(blobStoreApiMock).createContainer(accountName, containerName);
    }

    @Test
    void provision_container_already_exists() {
        var resourceDef = resourceDefinition().transferProcessId("tpId").build();
        String accountName = resourceDef.getAccountName();
        String containerName = resourceDef.getContainerName();
        when(blobStoreApiMock.exists(accountName, containerName)).thenReturn(true);
        when(blobStoreApiMock.createContainerSasToken(eq(accountName), eq(containerName), eq("w"), any())).thenReturn("some-sas");

        var response = provisioner.provision(resourceDef).join();

        assertThat(response.getResource()).isInstanceOfSatisfying(ObjectContainerProvisionedResource.class, resource -> {
            assertThat(resource.getTransferProcessId()).isEqualTo("tpId");
        });
        assertThat(response.getSecretToken()).isInstanceOfSatisfying(AzureSasToken.class, secretToken -> {
            assertThat(secretToken.getSas()).isEqualTo("?some-sas");
        });
        verify(blobStoreApiMock).exists(anyString(), anyString());
        verify(blobStoreApiMock).createContainerSasToken(eq(accountName), eq(containerName), eq("w"), any());
    }

    @Test
    void provision_no_key_found_in_vault() {
        var resourceDefinition = resourceDefinition().build();
        when(blobStoreApiMock.exists(any(), anyString()))
                .thenThrow(new IllegalArgumentException("No Object Storage credential found in vault"));

        assertThatThrownBy(() -> provisioner.provision(resourceDefinition).join()).hasCauseInstanceOf(IllegalArgumentException.class);
        verify(blobStoreApiMock).exists(any(), any());
    }

    @Test
    void provision_key_not_authorized() {
        var resourceDef = resourceDefinition().build();
        when(blobStoreApiMock.exists(anyString(), anyString())).thenReturn(false);
        doThrow(new BlobStorageException("not authorized", null, null))
                .when(blobStoreApiMock).createContainer(resourceDef.getAccountName(), resourceDef.getContainerName());

        assertThatThrownBy(() -> provisioner.provision(resourceDef).join()).hasCauseInstanceOf(BlobStorageException.class);
        verify(blobStoreApiMock).exists(anyString(), anyString());
    }

    private ObjectStorageResourceDefinition.Builder resourceDefinition() {
        return ObjectStorageResourceDefinition.Builder
                .newInstance()
                .accountName("test-account-name")
                .containerName("test-container-name")
                .transferProcessId("test-process-id")
                .id("test-id");
    }
}

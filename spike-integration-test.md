# Integration Test Spike Results

A spike was performed to evaluate state of Integration Testing in EDC repo. Few improvements and suggestion are identified, this document will list these items.

## Running Integration Tests locally

- [Current](./docs/integration-testing.md) documentation can be improvement further to add details regarding what infrastructure setup we need in order to run IT tests locally. For example to run Azure CosmosDB related IT test an Azure cosmos DB cloud instance needs to created and configured in tests, another example is to run Azure blob storage related IT tests a docker container(mcr.microsoft.com/azure-storage/azurite) needs to be running.

- Test setup configuration needs to be externalized and should be reused across tests. For example account name of Azure CosmosDB instance is hard coded in [Test code](https://github.com/Agera-CatenaX/EclipseDataSpaceConnector/blob/2d62acc473608ab06464a8cf59919d8a260b98dc/extensions/azure/transfer-process-store-cosmos/src/test/java/org/eclipse/dataspaceconnector/transfer/store/cosmos/CosmosTransferProcessStoreIntegrationTest.java#L65) hence if we setup a CosmosDB account then test code needs to be updated in order to use it.

- Possible solutions to run IT tests locally

  - A docker-compose file can be created which can run required containers locally. See recently added [sample file](./docker-compose.yml). Azure cosmos db emulator for [linux docker container](https://docs.microsoft.com/en-us/azure/cosmos-db/linux-emulator?tabs=ssl-netstd21) is also available in preview now.
  - [Testcontainer](https://www.testcontainers.org/) based approach can also be used. This approach works very well with JUnit and creates temporary docker containers to run tests and then automatically destroy them. Azure cosmos-db is also available as a [module](https://www.testcontainers.org/modules/azure/). Attention needs to be paid on a testcontainer startup time, evaluate the approach if any testcontainer takes very long to startup which can increase test suite duration.
  -A [spike implementation](https://github.com/Agera-CatenaX/EclipseDataSpaceConnector/blob/946ef867796020910a93a3bf3d364ead72f6f063/extensions/azure/transfer-process-store-cosmos/src/test/java/org/eclipse/dataspaceconnector/transfer/store/cosmos/CosmosTransferProcessStoreIntegrationTest.java#L89) is done to run `CosmosTransferProcessStoreIntegrationTest` test using CosmosDB emulator test container.
  - [Generic test](https://www.testcontainers.org/features/creating_container/) containers can also be created based on a docker images. This approach can be used if a module is not already available within testcontainers.org module repository. It also provides us an opportunity to do sharing/contribution of test containers e.g. creating a testcontainer module for azure storage based on mcr.microsoft.com/azure-storage/azurite docker image.
  - Even though the above mentioned approaches resolves local test setup for some IT tests but still it is possible that for some test components we need to use a cloud based infrastructure only.

## Consistency of Integration Tests Github flow

Some of the IT tests runs using containerized service instance while other runs containers using command line options. To keep the approach consistent we have [updated integration tests github flow](https://github.com/Agera-CatenaX/EclipseDataSpaceConnector/blob/946ef867796020910a93a3bf3d364ead72f6f063/.github/workflows/integrationtests.yaml#L94) to run using containerized service instance for all.

## Integration Tests coverage for EDC samples

EDC [samples](https://github.com/Agera-CatenaX/EclipseDataSpaceConnector/tree/upstream-main/samples) doesn't have any integration tests. This provides us an opportunity to add integration tests to samples so that we can ensure that samples are working as expected.

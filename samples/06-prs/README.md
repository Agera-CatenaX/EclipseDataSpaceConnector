# Run connectors

## Build the project and run connectors

Change PRS_URL in PrsApiCaller class to point to PRS API.

This version uses locally published PRS Client (net.catenax.prs:prs-client:0.0.1-SNAPSHOT). Make sure that PRS Client is published in you local maven repository.

```bash
./gradlew clean build
# provider
java -Dedc.fs.config=samples/06-prs/provider/config.properties -jar samples/06-prs/provider/build/libs/provider.jar
# consumer
java -jar samples/06-prs/api/build/libs/basic-connector.jar
````


## Sample call:

```bash 
curl -X GET http://localhost:8181/api/vins/[VIN]/partsTree?view=AS_BUILT&connectorAddress=[url_provider_connector]&destination=[destination_path]
```




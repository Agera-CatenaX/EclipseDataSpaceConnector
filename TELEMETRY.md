# Telemetry spike

The sample `04-file-transfer` was adapted to demonstrate automatic and manual collection of traces using the Open Telemetry library, together with either the Open Telemetry agent or the Application Insights agent.

The Application Insights agent is compatible with Open Telemetry library code.

Usage:

```bash
./gradlew samples:04-file-transfer:consumer:build
./gradlew samples:04-file-transfer:provider:build
docker-compose up
```

The docker-compose file spins multiple containers to demonstrate multiple telemetry backends:
- Application Insights
- Jaeger (at [http://localhost:16686](http://localhost:16686))
- Zipkin (at [http://localhost:9411](http://localhost:9411))

It also starts a container to fire a cURL request to initiate a contract negotiation process on the consumer connector. This causes EDC to send an HTTP request from the consumer to the provider connector, followed by another message from the provider to the consumer connector. See [the sample README file](samples/04-file-transfer//README.md) for more information about the negotiation process.

Features shown:
- [Configuration-based](https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md) exporter to Azure Application Insights, Jaeger and Zipkin.
- [Automatic instrumentation](https://opentelemetry.io/docs/instrumentation/java/automatic_instrumentation/) and trace propagation for Jersey (incoming) and OkHttp (outgoing) HTTP calls.
- [Manual instrumentation](https://opentelemetry.io/docs/instrumentation/java/manual_instrumentation/) for capturing custom spans and events.
- [Custom code for context propagation](https://opentelemetry.io/docs/instrumentation/java/manual_instrumentation/#context-propagation) by capturing the W3C Trace Context HTTP headers in the EDC in-memory store for asynchronous requests.

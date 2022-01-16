# OpenTelemetry Metrics spike

The sample `04-file-transfer` was adapted to demonstrate collection of metrics using the OpenTelemetry library. Metrics can be exposed through a Prometheus-compatible web endpoint. We also attempted to have metrics automatically collected by the Application Insights agent.

Note that Metrics are in Alpha version in OpenTelemetry.

The result of the spike:

- Custom metrics are collected when using the latest (January 2022) OpenTelemetry agent. 
- OpenTelemetry Metrics are apparently not yet collected by the Application Insights agent (even with the recent `3.2.5-BETA` release). Indeed, the [Application Insights agent docs](https://docs.microsoft.com/en-us/azure/azure-monitor/app/java-in-process-agent#supported-custom-telemetry) do not mention support for OpenTelemetry Metrics.

## Usage

### Prerequisites

Download [applicationinsights-agent-3.2.4.jar](https://docs.microsoft.com/en-us/azure/azure-monitor/app/java-in-process-agent#download-the-jar-file) and place it in the project root folder.

To use also Application Insights as a telemetry backend you have to provide `APPLICATIONINSIGHTS_CONNECTION_STRING` property. Copy the content of [`.env.example`](./.env.example) into a newly created `.env` file and fill in the Application Insights connection string.

Download [opentelemetry-javaagent.jar](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.10.0/opentelemetry-javaagent.jar) v1.10.0 and place it in the project root folder, after renaming it to `opentelemetry-javaagent-v1.10.0.jar`.

### Run the demo

```bash
./gradlew clean
./gradlew samples:04-file-transfer:consumer:build
./gradlew samples:04-file-transfer:provider:build
docker-compose up
```

The docker-compose file spins multiple containers to demonstrate multiple metrics:
- Azure Monitor [Application Insights](https://docs.microsoft.com/azure/azure-monitor/app/app-insights-overview) cloud-native Application Performance Management (APM) service
- [Prometheus](https://prometheus.io/) open-source monitoring system (at [http://localhost:9090](http://localhost:9090)) with OpenTelemetry agent

It also starts containers to fire cURL requests to repeatedly initiate a contract negotiation process on the consumer connector. This causes EDC to send an HTTP request from the consumer to the provider connector, followed by another message from the provider to the consumer connector. See [the sample README file](samples/04-file-transfer//README.md) for more information about the negotiation process.

### Verify the metrics

#### Application Insights

Metrics are not captured as far as we could tell.

#### Prometheus with Agent

Go to [http://localhost:9090](http://localhost:9090) and browse metrics.

Example: [query number of save operations per second as measured over the last minute](http://localhost:9090/graph?g0.expr=rate(negotiationsSaved_total%5B1m%5D)&g0.tab=0&g0.stacked=0&g0.show_exemplars=0&g0.range_input=15m).

![Prometheus metric](.attachments/prometheus.png)

We can access the Consumer Connector metrics endpoint at [http://localhost:9464/metrics](http://localhost:9464/metrics):

```sh
> curl http://localhost:9464/metrics
# HELP runtime_jvm_gc_time_total Time spent in a given JVM garbage collector in milliseconds.
# TYPE runtime_jvm_gc_time_total counter
runtime_jvm_gc_time_total{gc="G1 Young Generation",} 344.0 1642358676173
runtime_jvm_gc_time_total{gc="G1 Old Generation",} 0.0 1642358676173
# HELP negotiationsSaved_total Negotiation repository save operations
# TYPE negotiationsSaved_total counter
negotiationsSaved_total{FinalState="REQUESTED",InitialState="REQUESTED",} 17.0 1642360009221
negotiationsSaved_total{FinalState="REQUESTING",InitialState="UNSAVED",} 18.0 1642360009221
```

The metrics contain system metrics as well as our custom metrics.

We can similarly access the Provider Connector metrics endpoint at [http://localhost:9465/metrics](http://localhost:9465/metrics).

### About the code

The agent automatically configures the OpenTelemetry SDK based on environment variables. We can use the SDK to create meters:

```java
OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
MeterProvider meterProvider = openTelemetry.getMeterProvider();

LongCounter saveCounter = meter
  .counterBuilder("negotiationsSaved")
  .setDescription("Negotiation repository save operations")
  .setUnit("1")
  .build();
```

And modify meter values, including optional attributes:

```java
saveCounter.add(1, Attributes.of(
  stringKey("InitialState"), state1,
  stringKey("FinalState"), state2
));
```

In [the Prometheus server configuration file](prometheus/prometheus.yml), we configure the server to scrape the Prometheus endpoints.

```yaml
scrape_configs:
  - job_name: services
    static_configs:
      - targets:
          - 'consumerO:9464'
      ...
```

## Features shown in the spike

- [OpenTelemetry Metrics](https://opentelemetry.io/docs/instrumentation/java/manual_instrumentation/#metrics-alpha-only)
- [OpenTelemetry Prometheus exporter](https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#prometheus-exporter)

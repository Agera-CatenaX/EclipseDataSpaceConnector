# OpenTelemetry Metrics spike

The sample `04-file-transfer` was adapted to demonstrate collection of metrics using the OpenTelemetry library. Metrics can be exposed through a Prometheus-compatible web endpoint. We also attempted to have metrics automatically collected by the Application Insights agent.

Note that Metrics are in Alpha version in OpenTelemetry.

The result of the spike:

- Custom metrics are collected when using the latest (January 2022) OpenTelemetry agent. 
- OpenTelemetry Metrics are not yet collected by the Application Insights agent. Indeed, the Application Insights agent docs do not mention support for OpenTelemetry Metrics.

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

It also starts containers to fire cURL requests to initiate a contract negotiation process on the consumer connector. This causes EDC to send an HTTP request from the consumer to the provider connector, followed by another message from the provider to the consumer connector. See [the sample README file](samples/04-file-transfer//README.md) for more information about the negotiation process.

### Verify the metrics

#### Application Insights

Metrics are not captured as far as we could tell.

#### Prometheus with Agent

Go to [http://localhost:9090](http://localhost:9090).

We can access the Consumer Connector metrics endpoint at [http://localhost:9464/metrics](http://localhost:9464/metrics):

```sh
> curl http://localhost:9464/metrics
# HELP runtime_jvm_gc_time_total Time spent in a given JVM garbage collector in milliseconds.
# TYPE runtime_jvm_gc_time_total counter
runtime_jvm_gc_time_total{gc="G1 Young Generation",} 344.0 1642358676173
runtime_jvm_gc_time_total{gc="G1 Old Generation",} 0.0 1642358676173
# HELP saveOT_total Repository save operations
# TYPE saveOT_total counter
saveOT_total{FinalState="REQUESTED",InitialState="REQUESTED",} 1.0 1642358676173
saveOT_total{FinalState="REQUESTING",InitialState="UNSAVED",} 1.0 1642358676173
saveOT_total{FinalState="CONFIRMED",InitialState="CONFIRMED",} 1.0 1642358676173
```

The metrics contain system metrics as well as our custom metrics.

We can similarly access the Provider Connector metrics endpoint at [http://localhost:9465/metrics](http://localhost:9465/metrics).

### About the code

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

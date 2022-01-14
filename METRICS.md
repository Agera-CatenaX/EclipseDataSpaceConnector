# OpenTelemetry Metrics spike

The sample `04-file-transfer` was adapted to demonstrate collection of metrics using the OpenTelemetry library. Metrics are either exposed through a Prometheus-compatible web endpoint, or automatically collected by the Application Insights agent.

Note that Metrics are in Alpha version in OpenTelemetry.

## Usage

### Prerequisites

Download [applicationinsights-agent-3.2.4.jar](https://docs.microsoft.com/en-us/azure/azure-monitor/app/java-in-process-agent#download-the-jar-file) and place it in the project root folder.

To use also Application Insights as a telemetry backend you have to provide `APPLICATIONINSIGHTS_CONNECTION_STRING` property. Copy the content of [`.env.example`](./.env.example) into a newly created `.env` file and fill in the Application Insights connection string.

### Run the demo

```bash
./gradlew clean
./gradlew samples:04-file-transfer:consumer:build
./gradlew samples:04-file-transfer:provider:build
docker-compose up
```

The docker-compose file spins multiple containers to demonstrate multiple metrics:
- Azure Monitor [Application Insights](https://docs.microsoft.com/azure/azure-monitor/app/app-insights-overview) cloud-native Application Performance Management (APM) service
- [Prometheus](https://prometheus.io/) open-source monitoring system (at [http://localhost:9090](http://localhost:9090))

It also starts containers to fire cURL requests to initiate a contract negotiation process on the consumer connector. This causes EDC to send an HTTP request from the consumer to the provider connector, followed by another message from the provider to the consumer connector. See [the sample README file](samples/04-file-transfer//README.md) for more information about the negotiation process.

### Verify the distributed traces

#### Application Insights

Monitor the traces in [Application map](https://docs.microsoft.com/en-us/azure/azure-monitor/app/app-map?tabs=net) or in [transaction diagnostic](https://docs.microsoft.com/en-us/azure/azure-monitor/app/transaction-diagnostics) component.

<TODO>

#### Prometheus

Go to [http://localhost:9090](http://localhost:9090).

<TODO>

### About the code

<TODO>

We can access the Consumer Connector metrics endpoint at [http://localhost:9464/metrics](http://localhost:9464/metrics):

```sh
> curl http://localhost:9464/metrics
<TODO>
```

We can similarly access the Provider Connector metrics endpoint at [http://localhost:9465/metrics](http://localhost:9465/metrics).

In [the Prometheus server configuration file](prometheus/prometheus.yml), we configure the server to scrape those endpoints.

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

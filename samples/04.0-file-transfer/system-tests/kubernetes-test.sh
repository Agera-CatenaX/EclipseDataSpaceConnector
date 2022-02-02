#/bin/sh

set -euxo pipefail

dir=$(dirname $0)

# Build and install Consumer and Provider connectors

for target in consumer provider; do
  docker build -t $target --build-arg JAR=samples/04.0-file-transfer/$target/build/libs/$target.jar -f launchers/generic/Dockerfile .
  helm upgrade --install -f $dir/values-$target.yaml $target charts/dataspace-connector
done

# Wait for pods to be live

for target in consumer provider; do
  kubectl wait --for=condition=available deployment $target-dataspace-connector --timeout=120s
done

# Resolve NodePort address for Consumer

nodeIP=$(kubectl get nodes --namespace default -o jsonpath="{.items[0].status.addresses[0].address}")
consumerPort=$(kubectl get --namespace default -o jsonpath="{.spec.ports[0].nodePort}" services consumer-dataspace-connector)

# Perform negotiation and file transfer. See sample README.md file for more details.

export CHECK_FILE=0
export EDC_SAMPLES_04_ASSET_PATH=$(mktemp) # irrelevant
export EDC_CONSUMER_CONNECTOR_HOST="http://$nodeIP:$consumerPort"
export EDC_SAMPLES_04_CONSUMER_ASSET_PATH=/tmp/destination-file-$RANDOM
export EDC_PROVIDER_CONNECTOR_HOST=http://provider-dataspace-connector
export RUN_INTEGRATION_TEST=true

./gradlew :samples:integration-tests:test

kubectl exec deployment/provider-dataspace-connector -- wc -l $EDC_SAMPLES_04_CONSUMER_ASSET_PATH
echo "Test succeeded."

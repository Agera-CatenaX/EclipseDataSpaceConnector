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
consumerUrl="http://$nodeIP:$consumerPort"

# Perform negotiation and file transfer. See sample README.md file for more details.

requestId=$(curl -f -X POST -H "Content-Type: application/json" -d @samples/04.0-file-transfer/contractoffer.json "$consumerUrl/api/negotiation?connectorAddress=http://provider-dataspace-connector/api/ids/multipart")
sleep 15
negotiationId=$(curl -f -X GET -H 'X-Api-Key: password' "$consumerUrl/api/control/negotiation/$requestId")
contractId=$(jq -r .contractAgreement.id <<< $negotiationId)
destfile=/tmp/destination-file-$RANDOM
curl -f -X POST "$consumerUrl/api/file/test-document?connectorAddress=http://provider-dataspace-connector/api/ids/multipart&destination=$destfile&contractId=$contractId"
sleep 15
kubectl exec deployment/provider-dataspace-connector -- wc -l $destfile
echo "Test succeeded."

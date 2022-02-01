#/bin/sh

set -euxo pipefail

# minikube start
# eval $(minikube docker-env)
# samples/04.0-file-transfer/system-tests/kubernetes-test.sh

dir=$(dirname $0)


for s in consumer provider; do
  docker build -t $s --build-arg JAR=samples/04.0-file-transfer/$s/build/libs/$s.jar -f launchers/generic/Dockerfile .
  helm upgrade --install -f $dir/values-$s.yaml $s charts/dataspace-connector
done

for s in consumer provider; do
  kubectl wait --for=condition=available deployment $s-dataspace-connector --timeout=240s
done

nodeIP=$(kubectl get nodes --namespace default -o jsonpath="{.items[0].status.addresses[0].address}")
consumerPort=$(kubectl get --namespace default -o jsonpath="{.spec.ports[0].nodePort}" services consumer-dataspace-connector)
consumerUrl="http://$nodeIP:$consumerPort"

requestId=$(curl -f -X POST -H "Content-Type: application/json" -d @samples/04.0-file-transfer/contractoffer.json "$consumerUrl/api/negotiation?connectorAddress=http://provider-dataspace-connector/api/ids/multipart")
sleep 15
negotiationId=$(curl -f -X GET -H 'X-Api-Key: password' "$consumerUrl/api/control/negotiation/$requestId")
contractId=$(jq -r .contractAgreement.id <<< $negotiationId)
destfile=/tmp/destination-file-$RANDOM
curl -f -X POST "$consumerUrl/api/file/test-document?connectorAddress=http://provider-dataspace-connector/api/ids/multipart&destination=$destfile&contractId=$contractId"
sleep 15
kubectl exec -it deployment/provider-dataspace-connector -- wc -l $destfile
echo "Test succeeded."

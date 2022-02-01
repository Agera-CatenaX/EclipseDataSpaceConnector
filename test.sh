#/bin/sh

set -euxo pipefail

# eval $(minikube docker-env)

for s in consumer provider; do
  docker build -t $s --build-arg JAR=samples/04.0-file-transfer/$s/build/libs/$s.jar -f ./samples/04.0-file-transfer/Dockerfile .
  helm delete $s || true
  helm install -f values-$s.yaml $s charts/dataspace-connector
done

for s in consumer provider; do
  kubectl wait --for=condition=available deployment $s-dataspace-connector --timeout=120s
done

nodeIP=$(kubectl get nodes --namespace default -o jsonpath="{.items[0].status.addresses[0].address}")
consumerPort=$(kubectl get --namespace default -o jsonpath="{.spec.ports[0].nodePort}" services consumer-dataspace-connector)
consumerUrl="http://$nodeIP:$consumerPort"
providerPort=$(kubectl get --namespace default -o jsonpath="{.spec.ports[0].nodePort}" services provider-dataspace-connector)
providerUrl="http://$nodeIP:$providerPort"

requestId=$(curl -f -X POST -H "Content-Type: application/json" -d @samples/04.0-file-transfer/contractoffer.json "$consumerUrl/api/negotiation?connectorAddress=http://provider-dataspace-connector/api/ids/multipart")
sleep 15
negotiationId=$(curl -f -X GET -H 'X-Api-Key: password' "$consumerUrl/api/control/negotiation/$requestId")
contractId=$(jq -r .contractAgreement.id <<< $negotiationId)
destfile=/tmp/$RANDOM
curl -f -X POST "$consumerUrl/api/file/test-document?connectorAddress=http://provider-dataspace-connector/api/ids/multipart&destination=$destfile&contractId=$contractId"
sleep 15
kubectl exec -it deployment/provider-dataspace-connector -- wc -l $destfile

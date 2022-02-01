#/bin/sh

if [ -z "$1" ]
then
  echo "Usage: $0 BASE_URL [CONTAINER_REGISTRY]"
  echo "  BASE_URL: Base URL for API calls, e.g. http://localhost"
  echo "  CONTAINER_REGISTRY (optional): Registry to push Docker images to"
  exit 1
fi

set -euxo pipefail

baseUrl=$1
registry=${2:-}

for s in consumer provider; do
  docker build -t $registry/$s --build-arg JAR=samples/04.0-file-transfer/$s/build/libs/$s.jar -f ./samples/04.0-file-transfer/Dockerfile .
  if [ -n "$registry" ]
  then
    docker push $registry/$s
  fi
  helm delete $s || true
  helm install -f values-$s.yaml $s charts/dataspace-connector
done

for s in consumer provider; do
  kubectl wait --for=condition=available deployment $s-dataspace-connector --timeout=120s
done

requestId=$(curl -f -X POST -H "Content-Type: application/json" -d @samples/04.0-file-transfer/contractoffer.json "$baseUrl/consumer/api/negotiation?connectorAddress=http:///provider-dataspace-connector/api/ids/multipart")
sleep 15
negotiationId=$(curl -f -X GET -H 'X-Api-Key: password' "$baseUrl/consumer/api/control/negotiation/$requestId")
contractId=$(jq -r .contractAgreement.id <<< $negotiationId)
destfile=/tmp/$RANDOM
curl -f -X POST "$baseUrl/consumer/api/file/test-document?connectorAddress=http://provider-dataspace-connector/api/ids/multipart&destination=$destfile&contractId=$contractId"
sleep 15
kubectl exec -it svc/provider-dataspace-connector -- wc -l $destfile

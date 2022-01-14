CONNECTOR_NAME=$1 # Should be `consumer` or `provider`
REGISTRY=$2 # Container registry
CONNECTOR_ADDRESS=$3
docker build --build-arg CONNECTOR_NAME=$CONNECTOR_NAME --build-arg CONNECTOR_ADDRESS=${CONNECTOR_ADDRESS} -f samples/04-file-transfer/Dockerfile . -t ${REGISTRY}/${CONNECTOR_NAME}:latest
docker push ${REGISTRY}/${CONNECTOR_NAME}:latest

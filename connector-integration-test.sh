#!/bin/bash

set -euo pipefail
set -x

provider=$1
consumer=$2

curl -O https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh
chmod +x wait-for-it.sh
./wait-for-it.sh -t 240 $provider
./wait-for-it.sh -t 240 $consumer

traceId=$(openssl rand -hex 16)

while true; do
  curl -X POST -H "Content-Type: application/json" -d @/samples/04-file-transfer/contractoffer.json -H "traceparent: 00-$traceId-d99d251a8caecd06-01" "http://$consumer/api/negotiation?connectorAddress=http://$provider/api/ids/multipart"
  sleep 1
done

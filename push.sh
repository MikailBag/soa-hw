#!/usr/bin/env bash
set -euxo pipefail

# it is too hard to correctly version images
SERVER_IMAGE=cr.yandex/crpepnp6t24n31l71vv9/soa-2
CLIENT_IMAGE=cr.yandex/crpepnp6t24n31l71vv9/soa-2-client

./gradlew build
( cd client && go build . )

docker build -f ./Dockerfile ./build/distributions/ -t ${SERVER_IMAGE}
( cd client && docker build . -t ${CLIENT_IMAGE} )

docker push ${SERVER_IMAGE}
docker push ${CLIENT_IMAGE}

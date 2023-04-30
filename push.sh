#!/usr/bin/env bash
set -euxo pipefail

# it is too hard to correctly version images
IMAGE=cr.yandex/crpepnp6t24n31l71vv9/soa-1:latest

./gradlew build
docker build -f ./Dockerfile ./build/distributions/ -t ${IMAGE}
docker push ${IMAGE}
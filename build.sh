#!/usr/bin/env bash

REGISTRY='repo.lab.fiware.org'
IMAGE='ids-stream-cmm-machine'

docker build --no-cache -t ${REGISTRY}/${IMAGE} .
docker push ${REGISTRY}/${IMAGE}

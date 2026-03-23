#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TODO_DIR="$SCRIPT_DIR/todo"
DOCKERFILE_DIR="$SCRIPT_DIR/../TD1/todo"

kubectl apply -k "$TODO_DIR"

kubectl rollout status deployment/registry

docker build -t 127.0.0.1:30500/todo "$DOCKERFILE_DIR"
docker push 127.0.0.1:30500/todo

kubectl rollout restart deployment/todo
kubectl rollout status deployment/todo

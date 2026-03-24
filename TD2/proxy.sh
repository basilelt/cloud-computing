#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

k3d cluster create --registry-config reg.yml --agents=2 -p "80:80@server:0" -p "30500:30500@agent:0"

kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/alternative.yaml
kubectl -n kubernetes-dashboard create serviceaccount admin-user

kubectl apply -f script.yml

kubectl -n kubernetes-dashboard create clusterrolebinding --clusterrole cluster-admin --serviceaccount kubernetes-dashboard:admin-user admin-user

echo ""
kubectl -n kubernetes-dashboard get secret admin-user-token -o go-template="{{.data.token | base64decode}}"
echo ""
echo ""
echo "Dashboard URL: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/kubernetes-dashboard:80/proxy/#/workloads?namespace=default"
echo ""

kubectl proxy \
  --port=8001 \
  --address=0.0.0.0 \
  --accept-hosts='^home$,^192\.168\.27\.65$,^localhost$,^127\.0\.0\.1$'

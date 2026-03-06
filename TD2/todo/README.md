# TD2 Todo on k3d with Local Registry

This folder contains the Kubernetes manifests for the `todo` app, MySQL, and a local Docker registry exposed through NodePort.

## What Was Fixed

1. `registry` Service definition
- Problem: `type: NodePort` was combined with `clusterIP: None` (headless), which is invalid.
- Fix: removed `clusterIP: None` from `registry-deployment.yaml`.

2. Initial `docker push` / `curl` EOF on registry
- Problem: the registry pod was still starting (`ContainerCreating`), so the Service had no endpoints yet.
- Fix: wait until registry pod is `Running` and endpoint exists before pushing.

3. `todo` `ErrImagePull` with `registry:5000/todo:latest`
- Problem: image pull is done by node containerd, not pod DNS. Node runtime could not resolve/reach `registry:5000`.
- Fix: configure k3s registry mirror so `registry:5000` resolves to the k3d load balancer NodePort endpoint.

4. TODO startup before MySQL was ready
- Problem: if TODO starts before DB is reachable, it falls back to in-memory storage.
- Fix: add an `initContainer` with `toschneck/wait-for-it` that blocks until `mysql:3306` accepts connections.

5. External HTTP access through Ingress
- Fix: add `todo-ingress.yaml` with a `/` path rule and no hostname constraint.

Current image, scaling and session behavior in `todo-deployment.yaml`:
- `registry:5000/todo:latest`
- `replicas: 3`
- `AUTH: "true"`
- init container waits for `mysql:3306` before app container starts

Current sticky-session behavior in `todo-ingress.yaml`:
- Traefik sticky cookie enabled
- cookie name: `todo_route` (separate from app cookie `sid`)

## YAML for Registry Runtime Config (k3d)

Use `k3d-registry-config.yaml` when creating the cluster:

```bash
k3d cluster create k3s-default \
  --agents 2 \
  -p "80:80@loadbalancer" \
  -p "30500:30500@loadbalancer" \
  --registry-config TD2/todo/k3d-registry-config.yaml
```

Important:
- `todo-deployment.yaml` pulls `registry:5000/todo:latest`.
- If you create the cluster with `TD2/reg.yml` (used in `proxy.sh` comments), add the `registry:5000` mirror there too, or use `TD2/todo/k3d-registry-config.yaml` directly.

If your cluster already exists, apply the config and restart k3d nodes:

```bash
for n in k3d-k3s-default-server-0 k3d-k3s-default-agent-0 k3d-k3s-default-agent-1; do
  docker cp TD2/todo/k3d-registry-config.yaml ${n}:/etc/rancher/k3s/registries.yaml
done
docker restart k3d-k3s-default-server-0 k3d-k3s-default-agent-0 k3d-k3s-default-agent-1
```

## YAML for Kubernetes Apply

Apply everything in this folder with:

```bash
kubectl apply -k TD2/todo
```

`kustomization.yaml` includes:
- mysql secret
- mysql PV/PVC + deployment/service
- registry PV/PVC + deployment/service
- todo deployment/service
- todo ingress (`/` path)

## End-to-End Flow

1. Deploy manifests:

```bash
kubectl apply -k TD2/todo
```

2. Build and push app image to registry:

```bash
docker build -t todo:latest TD1/todo
docker tag todo:latest 127.0.0.1:30500/todo:latest
docker push 127.0.0.1:30500/todo:latest
```

3. Verify image pull and MySQL wait from Kubernetes:

```bash
kubectl describe pod -l app=todo
kubectl get pod -l app=todo
```

Look for:
- `Successfully pulled image "registry:5000/todo:latest"` in events
- `Init Containers:` section with `wait-for-mysql` completed successfully
- `READY` is `1/1` and `STATUS` is `Running`

Note: after a fix, `kubectl describe` can still show older failed pull events. Check current state with `kubectl get pod -l app=todo`.

4. Verify app is reachable from inside cluster:

```bash
kubectl get svc todo
SVC_IP=$(kubectl get svc todo -o jsonpath='{.spec.clusterIP}')
docker exec k3d-k3s-default-server-0 sh -lc "wget -qO- http://$SVC_IP:8080 | head -n 5"
```

5. Verify app is reachable from outside via Ingress:

```bash
kubectl get ingress todo
curl -L http://127.0.0.1/
```

## Scaling and Sticky Sessions (No Hazelcast)

This setup intentionally uses sticky sessions only (no Hazelcast session sharing).

### Deploy and verify 3 replicas

```bash
kubectl apply -k TD2/todo
kubectl rollout status deployment/todo --timeout=180s
kubectl get pods -l app=todo
```

Expected result:
- `3` pods with label `app=todo`
- all pods `Running` and `READY 1/1`

### Validate sticky session behavior

1. Open the app and log in (`/auth` flow).
2. In browser dev tools, confirm both cookies exist:
- `sid` (application session cookie)
- `todo_route` (Traefik sticky routing cookie)
3. Refresh `/list` multiple times and confirm backend stays stable:
- check `meta[name="hostname"]` in page source/dev tools
- hostname should remain the same while the target pod is alive

### Known limitation without Hazelcast

- If the target pod restarts or is deleted, the in-memory session is lost.
- Replaying requests with old cookies redirects back to `/auth`.

Example validation:

```bash
kubectl get pods -l app=todo -o wide
kubectl delete pod <pod-name>
```

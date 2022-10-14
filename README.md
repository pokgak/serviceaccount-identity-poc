# Using k8s ServiceAccount for service-to-service authentication

PoC of using k8s ServiceAccount token to authenticate between services.

## How it works?

![Authentication Diagram](./service-account-auth.png)

## Running locally using Rancher Desktop

Note: `--namespace k8s.io` needed when using the built image locally in Rancher desktop and make sure to set container `imagePullPolicy: Never` or not the pod won't be able to pull the image.

### Client

```
cd client
nerdctl build --namespace k8s.io -t local/sa-identity-client .
k apply -f infra
```

### Server

```
cd server
nerdctl build --namespace k8s.io -t local/sa-identity-server .
k apply -f infra
```
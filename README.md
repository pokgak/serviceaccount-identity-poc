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
kubectl apply -f infra

kubectl get -nclient pod
kubectl exec -it <pod-name> -- sh

# inside the container
> wget -qSO- app.client:8080/refreshToken
> wget -qSO- app.server:8081/sendRequestToServer
```

### Server

```
cd server
nerdctl build --namespace k8s.io -t local/sa-identity-server .
k apply -f infra

kubectl get -nserver pod
kubectl exec -it <pod-name> -- sh

# inside the container
> wget -qSO- app.client:8080/refreshToken
```

### Rebuilding and deploying latest version of the app

Since we're using the `latest` tag instead of specific commit version for the image tag, we have to terminate the current pod so that the new pod will be using the latest image from our local registry:

```
kubectl rollout -n {client|server} deploy/app
```

This will restart the Deployment and starts a new pod.

### Example Response from TokenReview API

The following snippet is a part of the full TokenReview object returned by the TokenReview API after we sent it the token from the client. I extracted the `status` field from the object.

The `tokenReviewStatus.user.username` contains the namespace and name of the ServiceAccount used by the client. 
```json
{
    "tokenReviewStatus": {
        "audiences": [
            "server"
        ],
        "authenticated": true,
        "error": null,
        "user": {
            "extra": {
                "authentication.kubernetes.io/pod-name": [
                    "app-84cb7b495f-scvbk"
                ],
                "authentication.kubernetes.io/pod-uid": [
                    "82a48992-ab8c-4c68-8816-1e05d52be912"
                ]
            },
            "groups": [
                "system:serviceaccounts",
                "system:serviceaccounts:client",
                "system:authenticated"
            ],
            "uid": "fda6b989-3c38-4113-9483-f2a9e8c12edb",
            "username": "system:serviceaccount:client:client"
        }
    }
}
```

## Resources

- [Explaination how to use service account tokens for service-to-service authentication](https://learnk8s.io/microservices-authentication-kubernetes)
- [How AWS uses this feature to build the IRSA for EKS](https://learnk8s.io/authentication-kubernetes#workload-identities-in-kubernetes-how-aws-integrates-iam-with-kubernetes)
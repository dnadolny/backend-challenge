# Ada Backend Challenge #

## Specifications

See the [original specifications](https://github.com/AdaSupport/backend-challenge/blob/33e3ea5435957b7614818c209a6935dac82d7628/README.md), rooughly: an HTTP API that saves messages grouped by a `conversation_id` and can list them.

Additional requirements:
 * Dockerize the application
 * Create a Kubernetes deployment for the application and instructions on how to deploy and access on DockerDesktop-Kubernetes or MiniKube

## Build & Run

Requires:
 * [sbt](https://www.scala-sbt.org/download.html) (you can download the tarball and add `bin` to your path, or even invoke it directly with `/path/to/sbt`)
 * [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)
 * [minikube](https://kubernetes.io/docs/setup/minikube/)

Setup:
```sh
minikube start #once
eval $(minikube docker-env) #once per terminal
```

To build and run:

```sh
sbt assembly
docker build -t backend-challenge .
kubectl apply -k .
```
Get the url to hit:
```sh
minikube service ada-challenge-backend --url
```

### Alternative to sbt

A dockerized build is available in `dockerized-build/Dockerfile`, replace `Dockerfile` with that to use it. It avoids the need to download sbt, but the price is a much slower build process.


## Try it out

```sh
ADA_URL="$(minikube service ada-challenge-backend --url)"

curl "$ADA_URL/conversations/12345"
# should get {"id":"12345","messages":[]}

#save a new message
curl -d '{"sender": "the_sender", "message": "some_message", "conversation_id": "12345"}' -H "Content-Type: application/json" -X POST "$ADA_URL/messages/"

curl "$ADA_URL/conversations/12345"
# now we get {"id":"12345","messages":[{"sender":"the_sender","message":"some_message","created":"2019-04-19T12:44:45.268Z"}]} 
```


## Operating

* `kubectl get pods --selector=app=ada-challenge` - list pods, there is a readiness check (and liveness check for the app) so once it shows ready it should be able to serve requests
* `kubectl logs --selector=app=ada-challenge,tier=backend` - show log for backend server
* `kubectl delete deploy/ada-challenge-backend` - kill backend server so you can rebuild and run `kubectl apply -k .` again


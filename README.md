Kafka Orders AI
========================================================

Deploy Quarkus projects on OpenShift
--------------------------------------------------------

### Prerequisites

The Quarkus applications relies on a pre-existing Kafka server installation.

The actual Kafka bootstrap address has to be configured here: [k8s/base/configmap.yaml]()

### Deployment

Create project:
```sh
oc new-project kafka-orders-ai
```

Set shared configuration and pvc:
```sh
oc apply -k k8s
```


Deploy the order producer:
```sh
./mvnw -f order-producer package -DskipTests -Dquarkus.kubernetes.deploy=true
```

Deploy the order query:

```sh
./mvnw -f order-query package -DskipTests -Dquarkus.kubernetes.deploy=true
```

### Remove all projects artifacts from OpenShift

```sh
oc delete all -l app.kubernetes.io/part-of=kafka-orders-ai
```

LibreChat Deployment
--------------------------------------------------------

Create project:
```sh
oc new-project librechat
```

Clone [LibreChat repo](https://github.com/danny-avila/LibreChat)

Copy the [Container file](librechat/Containerfile) in the cloned repository.

From the LibreChat folder, build the image locally:

```sh
podman build --tag "librechat:local" --file Containerfile
```

Push on the registry and set the local lookup policy:

```sh
set REGISTRY (oc registry info)
podman login -u (oc whoami) -p (oc whoami --show-token) $REGISTRY
podman tag localhost/librechat:local $REGISTRY/librechat/librechat:latest
podman push $REGISTRY/librechat/librechat:latest
oc patch imagestream librechat --type merge -p '{"spec":{"lookupPolicy":{"local":true}}}'
```

Librechat comes with a set of models from [Model as a Service](https://maas.apps.prod.rhoai.rh-aiservices-bu.com/).
You have to provide your own API KEYS to enable those models:

1. Create the file `librechat/.env`

2. Add you own keys:

    ```
    LLAMA4_API_KEY=abc..........................123
    LLAMA_API_KEY=abc..........................123
    QWEN_API_KEY=abc..........................123
    MISTRAL_API_KEY=abc..........................123
    ```

Deploy manifests via Kustomizer:

```sh
oc apply -k librechat
```
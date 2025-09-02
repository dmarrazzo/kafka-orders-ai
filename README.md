Kafka Orders AI
========================================================

Run locally
--------------------------------------------------------

### Run Quarkus projects in dev mode

Run the order producer:
```sh
./mvnw -f order-producer quarkus:dev
```

In different terminal, run the order query:

```sh
./mvnw -f order-query  quarkus:dev -Ddebug=5006 -Dquarkus.http.port=8090
```

### Run LibreChat

[LibreChat](https://www.librechat.ai/) is an open-source, self-hosted chat platform designed to provide a ChatGPT-like experience with enhanced privacy, flexibility, and extensibility. It allows users to interact with various AI models and services, including OpenAI, Google, and custom endpoints, all within a customizable web interface. LibreChat supports features such as multi-user authentication, social logins, conversation management, and integration with external tools and APIs.

Here the instuctions to build and run a local container:

1. Clone librechat project:

   ```sh
   git clone https://github.com/danny-avila/LibreChat.git
   cd LibreChat
   ```

2. This project has been tested with `v0.8.3-rc3`

   ```sh
   git checkout v0.8.0-rc3
   ```

3. Build image using the `Containerfile` provided in this project:

   ```sh
   podman build -t librechat:v0.8.0-rc3 -f ${PATH_TO_KAFKA_ORDERS_AI}/librechat/Containerfile .
   ```

4. Configure LibreChat

   - Navigate to the `/librechat` directory in this project

     ```sh
     cd ${PATH_TO_KAFKA_ORDERS_AI}/librechat
     ``` 

   - Create a `librechat-env.yaml` file to store your API token(s). For example:

     ```yaml
     apiVersion: v1
     kind: ConfigMap
     metadata:
       name: librechat-env
     data:
       MISTRAL_API_KEY: a...........................z
     ```

     > **TIP:** You can generate your API key by signing up for Model as a Service (MaaS) at: https://maas.apps.prod.rhoai.rh-aiservices-bu.com/.
     LibreChat also supports other models, such as OpenAI—simply provide the relevant API key in your configuration.

   - Make sure that the configuration file (`librechat.yaml`) include the address of the local **Quarkus MCP Server**.

     ```yaml
     mcpServers:
       kafka:
         url: http://host.docker.internal:8090/mcp/sse
         timeout: 60000 
     ```

5. Launch the LibreChat pod:
  
   ```sh
   podman kube play --configmap librechat-env.yaml podman-kube-play.yaml
   ```

### Build the Order Agent in LibreChat UI

1. Access the LibreChat user interface by navigating to [http://localhost:3080/]() in your web browser.

2. Follow the Sign-up procedure to create your user.

3. Sign-in using the just created credentials.


Create an agent, in my test I used the following agent instructions:

```
you are an helpful assistant that offer information about order. When you don't have enough information use tools provided to extract information. When the result contains more than 2 items, format the response in table.
```

Make sure to add the **Kafka tools**.

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
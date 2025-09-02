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

2. Complete the sign-up process to create your LibreChat user account.

3. Log in with your new credentials and accept the terms and conditions when prompted.

4. In the right panel of the LibreChat UI, configure your agent as follows:

   - **Name:** `Kafka Agent`
   - **Model:** `mistral-small-24b-w8a8`
   - **Instructions**:
     ```
     you will provide order information using available tools. When the result contains more than 2 items, format the response in table.
     ```

   - Click on `Add Tools` to open the tools selection dialog. Locate the `orders_tool` tile, click the `Add +` button, and then close the dialog.

### Chat with the agent

1. In the top navigation bar, click on the default model (`gpt-4o-mini`) and select `My agent > Kafka agent` from the dropdown.

   ![agent selection](/images/agent-selection.png)

2. Before starting a conversation, inject some new orders by visiting [http://localhost:8080/](). For best results, try adding 15 orders in 3 separate rounds, waiting a few seconds between each batch.

3. Now you can chat with your agent. Here are some example prompts you can try:

   - `show last 10 orders`
   - `show last 4 orders and compute the average amount`
   - `show orders aggregated by client over the last 10 minutes`
   - `how many orders come from Stark industries in the last 10 minutes`

> **Note:** Due to the inherent unpredictability of large language models (LLMs), responses may occasionally be inaccurate or incomplete.

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

Update the configuration file (`librechat.yaml`) to match the service address of the **Quarkus MCP Server**, e.g.:

```yaml
mcpServers:
  kafka:
    url: http://order-query.kafka-orders-ai.svc/mcp/sse
    timeout: 60000 
```

Deploy manifests via Kustomizer:

```sh
oc apply -k librechat
```
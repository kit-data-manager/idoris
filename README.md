# IDORIS

[![Build with Gradle](https://github.com/maximiliani/idoris/actions/workflows/gradle.yml/badge.svg)](https://github.com/maximiliani/idoris/actions/workflows/gradle.yml)

IDORIS is an **Integrated Data Type and Operations Registry with Inheritance System**.

## Installation of Neo4j

IDORIS relies on the Neo4j graph database.
The following command will start a Neo4j instance with the necessary plugins and configuration.
If you want demo data to be loaded, execute this command in this directory.
All data will be stored in the `neo4j-data` directory.
The password for the Neo4j user `neo4j` is `superSecret`.
You can access the Neo4j browser at http://localhost:7474.

```
docker run \
-p 7474:7474 -p 7687:7687 \
--name neo4j-apoc-gds-idoris \
--volume=$(pwd)/neo4j-data:/data \
-e NEO4J_AUTH=neo4j/superSecret \
-e NEO4J_DEBUG=true \
-e NEO4J_ACCEPT_LICENSE_AGREEMENT=yes \
-e NEO4J_apoc_export_file_enabled=true \
-e NEO4J_apoc_import_file_enabled=true \
-e NEO4J_apoc_import_file_use__neo4j__config=true \
-e NEO4J_PLUGINS=\[\"apoc\",\"graph-data-science\"\,\"bloom\"] \
-e NEO4J_dbms_security_procedures_unrestricted=apoc.\\\*,gds.\\\* \
neo4j:5.22
```

## Running IDORIS

You need to have JDK 21 installed on your system.

For macOS, you can install it using Homebrew: ```brew install openjdk@21```.

For Fedora, you can install it using DNF: ```sudo dnf install java-21```.

Configure the [application.properties](src/main/resources/application.properties) file to contain the Neo4j API
credentials.

```
spring.application.name=idoris
logging.level.root=INFO
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=superSecret
# Base path for all REST endpoints
server.servlet.context-path=/api
server.port=8095
idoris.validation-level=info
idoris.validation-policy=strict
```

When Neo4j is running, start IDORIS with the following command:

```
./gradlew bootRun
```

You can access the IDORIS API at http://localhost:8095/api.

## Architecture

IDORIS is a Spring Modulith. Modules communicate via well-defined APIs and domain events. Each module constitutes a
Domain Aggregate and encapsulates:

- Model classes (entities and value objects)
- DAO repositories (persistence layer)
- Services (business logic)
- REST controllers (web/API adapter)

Module boundaries must be respected:

- Access other modules only through their exported APIs; do not reference internal classes directly.
- Domain events published by a module (e.g., from repositories/services) are defined and contained within that module;
  other modules may subscribe.
- The design facilitates adding alternative interfaces such as GraphQL, DOIP, and gRPC, and defining validation rules
  via the rules modules.

### API Documentation

IDORIS provides comprehensive API documentation using OpenAPI/Swagger. You can access the API documentation
at http://localhost:8095/swagger-ui.html when the application is running.

All endpoints support HATEOAS (Hypermedia as the Engine of Application State) and return HAL (Hypertext Application
Language) responses, making the API self-discoverable.

## DTO-first API and Links

IDORIS uses a DTO-first public API:

- Controllers accept and return DTOs only (no entities leak out of modules).
- Each DTO includes only user-defined fields and may include:
    - internalId: the internal database identifier (intended for internal use).
- Responses include HATEOAS links on DTOs to discover relationship operations (e.g., link/unlink endpoints for
  relationships like attributes, inheritsFrom, inputs/outputs) and a "pid" link relation to /pid/{pid} for stable
  references.

PID endpoints are accessible under /pid and allow redirecting to entities and viewing tombstones for deleted ones.

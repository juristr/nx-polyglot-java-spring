# Spring Boot build-time OpenAPI spike

`./gradlew build` writes `build/openapi/openapi.json` from Spring MVC mappings and Java records. The application has `GET /products`, `GET /products/{id}` and `GET /orders`, with hard-coded data and `Product` / `Order` schemas.

## Prerequisites and commands

Install [mise](https://mise.jdx.dev/) and run the following once from this repo:

```sh
mise trust
mise install
```

`mise.toml` pins Temurin 21.0.10+7.0.LTS. [Mise's Java integration](https://mise.jdx.dev/lang/java.html) sets `JAVA_HOME` and selects Java on `PATH`. With mise activated in your shell, run the commands below directly. The committed wrapper downloads Gradle 8.14.3 and verifies its distribution SHA-256. Maven Central and the Gradle distribution host must be reachable on the first build.

```sh
./gradlew build
./gradlew generateOpenApi
./gradlew clean generateOpenApi
```

Spring Boot is pinned to 3.5.16 and springdoc to 2.8.17. `gradle.lockfile` locks resolved application and generator dependencies. Update it intentionally with `./gradlew build --write-locks` when changing dependencies.

For scripts or terminals without mise shell activation, use:

```sh
mise exec -- ./gradlew build
mise exec -- python3 scripts/verify.py
```

On this machine, zsh already activates mise. Re-enter this directory or open a new terminal if an existing terminal still reports no Java runtime.

`./gradlew bootRun` starts the sample HTTP application when you want to try the endpoints manually. Spec generation does not require it.

## Generation

The dedicated `openapi` source set contains one JUnit test. `generateOpenApi` is a Gradle `Test` task that loads the Spring context with `WebEnvironment.MOCK`, requests `/v3/api-docs` through MockMvc, checks the contract and writes UTF-8 JSON. `check` depends on this task, so normal builds emit the spec. Ordinary `test` does not run the writer.

The test asserts a `MockServletContext` and rejects a `WebServerApplicationContext`. No embedded application server opens a listening port. Gradle still uses its normal daemon and test-worker IPC.

Generation fixes the locale to en-US and timezone to UTC. springdoc sorts object keys, pretty-prints the document, and uses an explicit relative server URL `/` and fixed API version. There are no databases, credentials or startup network calls. The `openapi` profile is active but needs no resource file for this app.

The task declares the spec file as an output alongside the test reports. Gradle's `Test` task tracks the compiled classes and runtime classpath. `org.gradle.caching=true` enables the local build cache. Generated files stay ignored by Git.

## Acceptance experiments

Python 3 and Git are needed only for the verification script:

```sh
python3 scripts/verify.py
```

The script clones committed HEAD into a temporary directory. It builds without the build cache, checks the endpoints and schemas, expects an unchanged build to be `UP-TO-DATE`, deletes the spec and checks `FROM-CACHE` restoration, compares uncached output bytes, renames `Product.name` to `title`, then checks a comment-only edit against the original output. It never changes source files in this checkout.

## Recorded results

Validated on 2026-09-24 with macOS arm64, Temurin 21.0.10+7 and Gradle 8.14.3.

| Command or experiment | Result |
| --- | --- |
| `./gradlew clean build --write-locks` | Passed; emitted all three paths and both schemas. |
| `python3 scripts/verify.py` | Passed all experiments in a fresh local clone. |
| Clone: `./gradlew build --no-build-cache` | Passed with real generation and no embedded web server context. |
| Clone: `./gradlew build` | `generateOpenApi UP-TO-DATE`. |
| Clone: force generation, delete JSON, `./gradlew generateOpenApi` | `generateOpenApi FROM-CACHE`; identical restored bytes. |
| Clone: `./gradlew generateOpenApi --rerun-tasks --no-build-cache` | Byte-identical output. |
| Clone: rename `Product.name` to `title`, `./gradlew generateOpenApi` | Generator executed; schema contains `title`, no `name`. |
| Clone: original DTO plus comment, force uncached generation | Output matches original bytes. |

After adding `mise.toml`, `env -u JAVA_HOME mise exec -- ./gradlew clean build --no-build-cache` also passed. This verifies setup without the manual `JAVA_HOME` override used for the original experiments.

Baseline spec SHA-256: `1fc8e1e24802420f9cc48a2215b3d8ff021f35bceebfb218c65024488f59dfaf`.

## Changes from the research template

The MockMvc/source-set mechanism works as written. This standalone build adds the Boot dependency BOM, Java 21 toolchain, compiler parameter metadata, dependency locking and a checksummed wrapper. The OpenAPI bean fixes server URL and version. Assertions cover all endpoints, nested DTO references and the absence of an embedded server context.

The template's relative `openapi.output` path is retained so the test system property does not include a checkout-specific absolute path. If PLS-183 changes the build directory, update the writer property and declared output together.

## PLS-183 follow-ups

Nx initialization and multi-module splitting were skipped to keep this spike focused on the Gradle producer. Released `@nx/gradle` target inference, outputs, Nx cache restoration, test atomization and DTO changes in dependency modules still need verification in PLS-183. This repo proves Gradle caching only.

A full Spring context still runs application startup code. Added databases, security, listeners or environment-dependent customizers require a deterministic generation profile. Key sorting does not normalize array order. Review contract details such as nullability, required fields, enums and dates before adding client generation.

Version compatibility references: [springdoc Boot 3 documentation](https://springdoc.org/v2/) and [Boot 3.5 requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html).

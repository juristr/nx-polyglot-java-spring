# Nx polyglot: Spring Boot + TanStack Start

A full-stack example workspace: a modular Spring Boot API (Gradle) and a TanStack Start storefront in one Nx monorepo. The storefront's TypeScript types are generated from the API's OpenAPI spec, and the Nx task graph keeps both sides in sync. Rename a field on a Java record and the storefront stops typechecking.

![Storefront](docs/storefront.png)

## Layout

| Path                       | Nx project            | What it is                                                                          |
| -------------------------- | --------------------- | ----------------------------------------------------------------------------------- |
| `libs/catalog-data`        | `catalog-data`        | Data layer. `Product` record and a hard-coded `ProductRepository`.                  |
| `libs/orders`              | `orders`              | Business layer. `OrderService` prices orders from the catalog and builds a summary. |
| `apps/products-api`        | `products-api`        | Spring Boot app: `/products`, `/products/{id}`, `/orders`, `/orders/summary`.       |
| `libs/shared/api-types`    | `api-types`           | TypeScript types generated from the API's OpenAPI spec with `@hey-api/openapi-ts`.  |
| `libs/storefront/products` | `storefront-products` | React components for the product list (`ProductList`, `ProductCard`).               |
| `libs/storefront/orders`   | `storefront-orders`   | React components for orders (`OrderTable`, `OrderSummary`).                         |
| `apps/storefront`          | `storefront`          | TanStack Start app. Its route loads the data and renders the two feature libs.      |
| `apps/storefront-e2e`      | `storefront-e2e`      | Playwright tests against the built storefront and the running API.                  |

The three Java modules are one Gradle build (`settings.gradle.kts` at the root). `@nx/gradle` turns every Gradle project into an Nx project and every Gradle task into an Nx target, with the dependencies between them. The TypeScript side is a pnpm workspace.

```
catalog-data <── orders <── products-api <·· api-types <── storefront-products <──┐
                                         (implicit)    <── storefront-orders   <──┴── storefront <·· storefront-e2e
                                                                                                  (implicit)
```

The two feature libs import only the generated types from `api-types`, never each other.

## Prerequisites

- [mise](https://mise.jdx.dev/) for Java. `mise.toml` pins Temurin 21. Run `mise trust && mise install` once. With mise activated in your shell, `java` and `./gradlew` pick it up automatically. Otherwise prefix commands with `mise exec --`.
- Node.js 24 and pnpm 11 (`packageManager` in `package.json`, so `corepack enable` works).
- The Gradle wrapper (8.14.3) is committed. The first build downloads Gradle and Maven dependencies.

```sh
pnpm install
```

## Commands

```sh
# Run the storefront (http://localhost:3000) and the Spring Boot API (http://localhost:8080)
pnpm nx dev storefront

# Typecheck everything. Builds the Java modules, emits the spec, generates types, then runs tsc.
pnpm nx run-many -t typecheck

# Build and test everything
pnpm nx run-many -t build test

# Individual steps
pnpm nx run products-api:generateOpenApi   # writes apps/products-api/build/openapi/openapi.json
pnpm nx codegen api-types                  # writes libs/shared/api-types/src/generated
pnpm nx serve products-api                 # Spring Boot on :8080
pnpm nx build storefront                   # vite build into apps/storefront/dist
pnpm nx preview storefront                 # built storefront on :4173, plus the API on :8080
pnpm nx e2e storefront-e2e                 # all Playwright specs in one task (Chromium)

# Explore
pnpm nx graph
pnpm nx show project products-api
```

`API_URL` overrides where the storefront's server functions reach the API (default `http://localhost:8080`).

## How the type sync works

1. **Spec at build time, no server.** `products-api:generateOpenApi` is a Gradle `Test` task in its own `openapi` source set. It boots the Spring context with a mock servlet environment, requests `/v3/api-docs` through MockMvc and writes `build/openapi/openapi.json`. No port is opened. Output is deterministic (fixed locale and timezone, sorted keys, fixed server URL and version), and Gradle's `check` depends on it, so `./gradlew build` emits the spec too. An `OpenApiCustomizer` marks every record component as required. Without it, the generated TypeScript makes every field optional.
2. **Nx picks the task up.** `@nx/gradle` infers `generateOpenApi` as a cacheable target, including the declared `build/openapi/openapi.json` output.
3. **Types from the spec.** `api-types` has a `codegen` script that runs `openapi-ts` on the spec. Its Nx config in `libs/shared/api-types/package.json`:

   ```json
   "implicitDependencies": ["products-api"],
   "targets": {
     "codegen": {
       "dependsOn": ["^generateOpenApi"],
       "inputs": [
         { "dependentTasksOutputFiles": "**/build/openapi/openapi.json" },
         "{projectRoot}/package.json",
         "sharedGlobals"
       ],
       "outputs": ["{projectRoot}/src/generated"],
       "cache": true
     }
   }
   ```

   `implicitDependencies` is the edge that makes `nx affected` see a Java change as affecting the storefront. The generated code is gitignored, so there's no import Nx could read instead. `codegen` hashes the spec file, not the Java sources, so a Java change that doesn't change the contract reuses the cached types.

4. **Consumers pull codegen in.** `nx.json` target defaults:

   ```json
   "typecheck": {
     "dependsOn": ["...", "codegen"],
     "inputs": ["...", { "dependentTasksOutputFiles": "**/*.ts", "transitive": true }]
   },
   "build": { "dependsOn": ["...", "^codegen"] },
   "dev":   { "dependsOn": ["...", "^codegen"] }
   ```

   The storefront imports only from `@shop/api-types`. It fetches through a TanStack Start server function (`apps/storefront/src/lib/shop-api.ts`), so the browser never calls the API directly and no CORS setup is needed.

## Verifying it

First recorded on 2026-09-25 with Nx 23.2.0. Re-run on 2026-11-05 after migrating to Nx 24.0.4, in a fresh clone with an empty cache (macOS arm64, Temurin 21.0.10, Gradle 8.14.3, pnpm 11.0.6). Every row passed both times.

| Experiment                                                                     | Result                                                                                                                  |
| ------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------- |
| `nx run-many -t typecheck` from a clean state                                  | Runs Java compile, `generateOpenApi`, `codegen`, then both typechecks in order. 18 tasks.                               |
| Same command again                                                             | 18/18 from cache.                                                                                                       |
| Delete `build/openapi/openapi.json`, run `nx run products-api:generateOpenApi` | Restored from the Nx cache.                                                                                             |
| Rename `Product.name` to `title` in `libs/catalog-data`                        | Spec and types regenerate. `storefront:typecheck` fails: `Property 'name' does not exist on type 'Product'` (2 errors). |
| Edit only the Javadoc on `Product`                                             | Java compile and `generateOpenApi` rerun. `codegen`, `api-types:typecheck` and `storefront:typecheck` come from cache.  |
| `nx run-many -t build test`, three times after `./gradlew clean`               | Passes each time.                                                                                                       |
| `nx dev storefront`                                                            | Starts Spring Boot on :8080 and Vite on :3000. Page renders products, summary and orders from the API.                  |
| `nx run-many -t build test typecheck` with an empty cache                      | 43 tasks pass. A rerun is 43/43 from cache.                                                                             |
| `nx affected` with a Java change, a storefront-only change, and no change      | All Java and TS projects; only `storefront`; nothing.                                                                   |

## End-to-end tests

`apps/storefront-e2e` uses [`@nx/playwright`](https://nx.dev/docs/technologies/test-tools/playwright/introduction). Run `pnpm exec playwright install chromium` once.

The `webServer` in `playwright.config.mts` is `nx run storefront:preview` with `reuseExistingServer: true`. Because the command is a plain `nx run <project>:<target>`, Nx runs the server as a task before the tests, so one task serves the whole stack: `storefront:preview` builds the storefront and depends on `products-api:serve`. Both targets set `readyWhen` with a URL, so the tests wait until the page and the API answer.

The plugin splits the suite into one task per spec file (`e2e-ci--src/orders.spec.ts` and so on), which CI distributes across the workers. Locally, `e2e-ci` only runs on Nx Cloud workers. Use `nx e2e storefront-e2e`, or name the split targets with `nx run-many -p storefront-e2e -t e2e-ci--src/orders.spec.ts`.

Each spec says which projects it exercises with a comment on its first line, so its cached result is reused when nothing it tests changed:

| Spec               | Directive                                              | Reruns when                                      |
| ------------------ | ------------------------------------------------------ | ------------------------------------------------ |
| `products.spec.ts` | `// @nx-depends-on: storefront-products, products-api` | the products lib, the API or what they depend on |
| `orders.spec.ts`   | `// @nx-depends-on: storefront-orders, products-api`   | the orders lib, the API or what they depend on   |
| `summary.spec.ts`  | `// @nx-depends-on: storefront-orders, products-api`   | the orders lib, the API or what they depend on   |
| `smoke.spec.ts`    | none                                                   | anything in the storefront                       |

The directive replaces the spec's dependency on the whole app with the listed projects and drops the server task's inputs. The app itself is never listed: that would put every feature back in the hash. App shell changes are covered by the unscoped smoke spec. Check a spec's inputs with:

```sh
pnpm nx show target inputs storefront-e2e:e2e-ci--src/orders.spec.ts \
  --check libs/storefront/orders/src/order-table.tsx libs/storefront/products/src/product-card.tsx
```

A change only in `libs/storefront/orders` reruns `orders.spec.ts`, `summary.spec.ts` and `smoke.spec.ts`, and replays `products.spec.ts` from the cache.

On your machine, each split task also hashes every file in `storefront-e2e`, so editing one spec reruns all of them. In CI, [Ultracache](https://nx.dev/docs/features/ci-features/remote-cache#ultracache) (turned on in the Nx Cloud workspace settings) records which files each spec actually read. It then keeps only those in the hash, plus the projects listed in the directive.

## CI

CI runs on [Nx Cloud CI](https://nx.dev/docs/features/ci-features#run-ci-on-nx-cloud), not GitHub Actions. Everything is in one file, `.nx/pipelines/ci.yaml`:

- **Triggers:** every push to `main` and every pull request. GitHub shows one status check, `Nx Cloud: CI`.
- **Job:** `main` runs `nx sync:check`, `nx format:check` and `nx affected -t build typecheck test e2e-ci`. Nx Cloud sets the affected base and head SHAs itself and runs self-healing CI on failures, so there is no `nx-set-shas` or `nx fix-ci` step.
- **Workers:** the job distributes tasks to three `linux-medium-polyglot` workers plus a worker on the main job's own machine. The workers stop once the `build`, `typecheck`, `test` and `e2e-ci` tasks are done.
- **Launch template:** `linux-medium-polyglot` is the standard Node 24 image plus Java from `mise.toml` and Playwright's Chromium, with caches for the pnpm store and Gradle. The main job runs on the same template. It needs Java as well, because `@nx/gradle` asks Gradle for the project graph before anything is distributed.

To turn it on: push the repo to GitHub, run `pnpm nx connect` (adds `nxCloudId` to `nx.json`), install the Nx Cloud GitHub App on the repository and enable Nx Cloud CI for the workspace. If `main` has branch protection, make `Nx Cloud: CI` the required check.

## Workarounds worth knowing

- **`storefront:typecheck` runs `tsc -p tsconfig.typecheck.json`, not `tsc --build`.** With incremental `tsc --build`, a cache restore can leave upstream files older than the storefront's `.tsbuildinfo`, and tsc then reports the project as up to date even though the generated types changed. The non-incremental check always reads the current types. Nx caching still skips it when nothing changed.
- **`bootJar` and `bootRun` run with `excludeDependsOn: false`** (`nx.json` target defaults). By default the `@nx/gradle` executor excludes a task's Gradle dependencies from the invocation. Spring Boot's `bootJar` reads lazy providers from `jar` and `resolveMainClassName`, so when Nx restores those from cache and excludes them, Gradle fails with "Querying the mapped value ... before task ... has completed is not supported".
- **`nx dev storefront` starts the API through `products-api:serve`** (`apps/products-api/project.json`, `./gradlew :products-api:bootRun`) instead of the inferred `bootRun`. Nx batches Gradle tasks, and a continuous `bootRun` in the same batch as `generateOpenApi` keeps the batch from ever finishing, so `codegen` and the storefront never start.

- **`storefront:preview` and `products-api:serve` set a target-level `readyWhen: { "url": ... }`, not the `readyWhen` option of `nx:run-commands`.** The option means "the task is done": outside a terminal, Nx stops the command when the text appears, so a continuous server exits as soon as it's up. Vite also shuts down when stdin closes unless `CI=true`. The target-level check keeps the plain `vite preview` script and only tells dependents when to start.

## Bringing in an existing Spring Boot app

The API here started as a standalone spike and was brought in with `nx import`, which keeps its git history. The [Agentic Nx Import video](https://youtu.be/mUG292kkz0w) walks through importing a multi-module Gradle Spring Boot project into an Nx workspace.

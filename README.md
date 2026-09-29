# Java Microservices Platform (Spring Boot · Spring Cloud · Eureka · Gateway)

A production-shaped microservices skeleton using **Java 21 + Spring Boot 4** — a secured
API gateway in front of JWT-protected employee and address services, backed by Eureka
service discovery, a shared MySQL 8 instance, and Docker / Kubernetes / CI delivery.

![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?style=flat-square&logo=spring)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.3-6DB33F?style=flat-square&logo=spring)
![Build](https://img.shields.io/github/actions/workflow/status/arjunchy/microservice/ci.yml?branch=develop&style=flat-square&logo=github&label=CI)
![Tests](https://img.shields.io/badge/tests-318%20passing-brightgreen?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=flat-square&logo=kubernetes)

---

## Overview

This repository demonstrates how a set of **independently deployable Spring Boot
services** are wired into one coherent system: a reactive **Spring Cloud Gateway**
enforces authentication and route-level **circuit breaking**, **Netflix Eureka** handles
service discovery, and each downstream service independently **validates JWTs and
enforces role-based access** as a second line of defense.

The architecture exists to answer a classic microservices question: *"how do you keep
many small services secure, discoverable, and resilient without a monolith's shared
context?"* Here, services own their data, communicate over HTTP via the gateway, and
share state only where it is safe (a read-only replica view). A person can use this
repo as a **template or a learning baseline** — register users, log in, get JWTs,
call protected endpoints, page through employees and addresses, and watch a circuit
breaker open and close under failure.

It ships with three execution targets: **Docker Compose** for a one-command local run,
**Kubernetes manifests** for Minikube, and a **GitHub Actions pipeline** that builds and
tests all five modules and publishes Docker images with git-tag–based versioning.

---

## Key Features

**Architecture**
- ✅ Five Spring Boot services + shared MySQL 8 (three logical databases)
- ✅ Eureka service discovery with HTTP `Basic` auth
- ✅ Reactive `Spring Cloud Gateway` with `lb://` routes
- ✅ Cross-database read: `EmployeeService` reads `address_db` through a second, **read-only** `DataSource` (`hikari.read-only=true`)
- ✅ Paginated list endpoints everywhere (Spring Data `Page`)

**Security**
- ✅ JWT issuance with **BCrypt** password hashing (`Auth`)
- ✅ JWT validation at the **gateway** (`AuthFilter` adds `X-User-Name` / `X-User-Role`)
- ✅ `@PreAuthorize` RBAC in every downstream service (`ADMIN`, `MANAGER`, `USER`)
- ✅ Defense in depth — each service re-validates the token in its own `JwtAuthenticationFilter`
- ✅ Secrets externalized — `.env`, Kubernetes `Secrets`, no hardcoded secrets in code
- ✅ CORS allow-list for `http://localhost:3000`

**Resilience**
- ✅ Resilience4J circuit breakers on `employee-service` and `address-service` routes (COUNT_BASED, 5% / 50% threshold)
- ✅ Gateway-level fallback endpoints for graceful degradation
- ✅ Optimistic locking (`@Version`) on `Employee` and `Address`
- ✅ Read-only cross-DB access isolated on its own transaction manager, with `AddressServiceUnavailableException` handling

**Developer experience**
- ✅ One-command `docker compose up` (healthchecks + `depends_on` ordering)
- ✅ Seed SQL (`mysql/init/init.sql`): databases, least-privilege users, demo data
- ✅ 318 passing tests across all services (unit, web-slice, repository, security, full-flow integration)
- ✅ CI builds, tests, and publishes versioned Docker images

---

## Architecture

```
                 ┌──────────────────────────────────────────────────────────┐
                 │                      API GATEWAY (9090)                  │
   HTTP/JWT ────► │  Spring Cloud Gateway (WebFlux / reactive)              │
  (8084 logins)   │   ┌─────────────┐    ┌──────────────┐   ┌──────────┐   │
                 │   │  AuthFilter │    │ CircuitBreaker│  ┌──────────┐  │
                 │   │ JWT validate│    │ (EMPLOYEE-    │  │Fallback- │  │
                 │   │ + X-User-Role        SERVICE,     │  │Controller│  │
                 │   └─────────────┘    │  ADDRESS-      │  └──────────┘  │
                 │                     │  SERVICE)      │                │
                 └─────────┬───────────┴───┬────────────┴────────┬───────┘
                           │lb://employees  │lb://addresses       │lb://auth
              ┌────────────▼─────┐  ┌───────▼────────┐  ┌────────▼───────┐
              │ EmployeeService  │  │ AddressService │  │  AuthService   │
              │  (8081)          │  │  (8082)        │  │  (8084)        │
              │  ▸ employee_db   │  │  ▸ address_db  │  │  ▸ auth_db     │
              │  ▸ reads address │  │  (owns writes) │  │  (BCrypt + JWT)│
              │    db read-only  │  └───────┬────────┘  └────────┬───────┘
              └────────┬─────────┘          │                    │
                       │        register + heartbeat             │
              ┌─────── ▼────────────────────▼────────────────────▼────────┐
              │                    EUREKA SERVER (8761)                   │
              │                    service registry · HTTP Basic          │
              └──────────────────────────┬───────────────────────────────┘
                       ┌─────────────────▼──────────────┐
                       │   MySQL 8 (3306 in-cluster)    │
                       │  employee_db · address_db ·    │
                       │  auth_db  (3 DBs, 1 instance)  │
                       └────────────────────────────────┘
```

### Request flow (authenticated call)

1. **Client** → `POST /auth/login` (allowed without token — in the gateway's public list) → `AuthService` verifies BCrypt password and returns a signed **JWT** (`sub` = username, `role` claim, HS256, 24 h expiry).
2. **Client** → `GET /employee/1` with `Authorization: Bearer <jwt>` → **Gateway `AuthFilter`** checks the token; on success it forwards the request with extra headers `X-User-Name` and `X-User-Role`, and routes to `lb://employee-service` (resolved via **Eureka**).
3. **EmployeeService** re-validates the token in its own `JwtAuthenticationFilter`, loads the `Employee`, and returns it. `@PreAuthorize` rules apply per endpoint (e.g. `/email/{email}` is ADMIN-only).
4. `GET /employee/{id}/with-address` demonstrates the **cross-service/cross-DB read**: the service queries `employee_db` (primary `DataSource`) **and** mirrors address rows straight from `address_db` through a dedicated read-only `DataSource` + `@Transactional(readOnly=true)` — no HTTP round-trip, no write risk.

Unlisted paths (anything that is not `/auth/register`, `/auth/login`, `/eureka/**`, `/actuator/**`) are rejected with `401` by the gateway when no valid JWT is present.

---

## Tech Stack

| Layer             | Technology                                  | Version        |
|-------------------|---------------------------------------------|----------------|
| Language          | Java                                        | 21             |
| Framework         | Spring Boot (parent BOM)                    | 4.1.1          |
| Cloud             | Spring Cloud                                 | 2025.1.3       |
| Service discovery | Spring Cloud Netflix Eureka                 | 2025.1.3       |
| API gateway       | Spring Cloud Gateway (WebFlux / reactive)   | 2025.1.3       |
| Auth / security   | Spring Security + JJWT (HS256)              | 0.12.6         |
| Resilience        | Resilience4J (reactor)                      | Spring Cloud   |
| Mapper            | MapStruct                                   | 1.6.3          |
| Data access       | Spring Data JPA / Hibernate / HikariCP      | Boot-managed   |
| Database (prod)   | MySQL                                       | 8.0            |
| Database (tests)  | H2 (MySQL mode)                             | Boot-managed   |
| Build             | Maven (wrapper, JVM-free `sh mvnw`)         | 3.9.x          |
| Delivery          | Docker / Docker Compose / Kubernetes / GH Actions | —         |

*Versions are read from each module's `pom.xml`. JJWT is pinned `0.12.6` in `AddressService`; the other modules use the same version managed by the Spring Boot BOM.*

---

## Project Structure

```
microservices/
├── docker-compose.yml            # One-command local stack (mysql, eureka, 4 apps)
├── .env.example                  # Template for secrets (JWT, DB, Eureka creds)
├── .env                          # Local secrets (gitignored)
├── README.md
├── mysql/
│   └── init/
│       └── init.sql              # 3 databases + users + seed data (auto-run on fresh volume)
├── kubernetes/
│   ├── namespace.yaml            # namespace: microservices
│   ├── configmap.yaml            # app-config (SERVER_ADDRESS, EUREKA vars)
│   ├── secrets.yaml              # db-secrets + eureka-secrets (Opaque)
│   ├── mysql/                    # init-configmap, statefulset, service
│   ├── eureka-server/            # deployment + service
│   ├── auth-service/             # deployment + service
│   ├── employee-service/         # deployment + service
│   ├── address-service/          # deployment + service
│   └── api-gateway/              # deployment + service (NodePort 30090)
├── .github/workflows/
│   └── ci.yml                    # build+test (matrix) → docker publish → success gate
├── EurekaServer/                 # Discovery server (8761), HTTP Basic, no self-registration
├── Auth/                         # JWT issue + login/register/me/admin (8084)
├── EmployeeService/              # Employee CRUD + read-only address_db access (8081)
├── AddressService/               # Address CRUD, (employee_id, type) unique (8082)
└── ApiGateway/                   # Reactive gateway, AuthFilter, circuit breakers (9090)
```

Each service contains the same layout: `pom.xml`, `Dockerfile`
(`eclipse-temurin:21-jre`), Maven wrapper, `src/main/java/<group>/<artifact>/…`
(`controller`, `service`, `repository`, `security`, `config`, `model`) and a
`src/test` tree with resource-level test configs.

---

## Getting Started

### Prerequisites

- **Java 21** (JDK) + the Maven wrapper is JVM-free, so only a JDK is required
- **Docker + Docker Compose** (recommended path)
- **kubectl / Minikube** (only for the Kubernetes path)
- Git

### A. Run with Docker Compose (recommended)

```bash
git clone https://github.com/arjunchy/microservice.git
cd microservices

cp .env.example .env          # then edit secrets (JWT_SECRET etc.) if you wish
docker compose up -d
docker compose ps             # wait until every container is "healthy"
```

Access points:

| Component      | URL                                  |
|----------------|--------------------------------------|
| API Gateway    | `http://localhost:9090`              |
| Eureka console | `http://localhost:8761` (login `eureka` / `eureka`) |
| MySQL (host)   | `localhost:3307` (root password from `.env`, default `root`) |

> `mysql/init/init.sql` runs only on a **fresh** volume. To reset: `docker compose down -v && docker compose up -d`.

### B. Run locally without Docker

Start a **MySQL 8** instance with the seed script applied, then launch services in order
(Eureka first, then Auth/Address/Employee, then Gateway). The app defaults expect a DB
user/password of `root` / `mysql`; either match that or export overrides:

```bash
# terminal 1 — Eureka
cd EurekaServer && JWT_SECRET=<32+ char secret> sh mvnw spring-boot:run

# terminal 2 — Auth
cd Auth && JWT_SECRET=<same secret> AUTH_DB_USERNAME=root AUTH_DB_PASSWORD=mysql sh mvnw spring-boot:run

# terminal 3 — AddressService
cd AddressService && JWT_SECRET=<same secret> ADDRESS_DB_USERNAME=root ADDRESS_DB_PASSWORD=mysql sh mvnw spring-boot:run

# terminal 4 — EmployeeService
cd EmployeeService && JWT_SECRET=<same secret> \
  EMPLOYEE_DB_USERNAME=root EMPLOYEE_DB_PASSWORD=mysql \
  ADDRESS_RO_DB_USERNAME=root ADDRESS_RO_DB_PASSWORD=mysql \
  sh mvnw spring-boot:run

# terminal 5 — ApiGateway
cd ApiGateway && JWT_SECRET=<same secret> sh mvnw spring-boot:run
```

Same access points as above, with the gateway at `http://localhost:9090`.

### C. Run on Kubernetes (Minikube)

```bash
minikube start
cd microservices/kubernetes

kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
kubectl apply -f secrets.yaml
kubectl apply -f mysql/init-configmap.yaml
kubectl apply -f mysql/statefulset.yaml
kubectl apply -f mysql/service.yaml
kubectl apply -f eureka-server/
kubectl apply -f auth-service/
kubectl apply -f address-service/
kubectl apply -f employee-service/
kubectl apply -f api-gateway/          # NodePort 30090
kubectl -n microservices get pods -w   # wait for Running/Ready
```

Access through the gateway NodePort:

```bash
open "http://$(minikube ip):30090"            # gateway
open "http://$(minikube ip):30090/demo/employee-cb"
```

> Deployments reference images `arjunchaudhary/<service>:latest` (see CI/CD section)
> and mount `db-secrets` / `eureka-secrets`, with `busybox` init containers that
> wait for MySQL / Eureka to be ready.

---

## Default Credentials

`mysql/init/init.sql` seeds these users **only on a fresh MySQL volume**:

| Username | Password     | Role  | Purpose                          |
|----------|--------------|-------|----------------------------------|
| `root`   | `admin123`   | ADMIN | Admin access (verified via live login) |
| `alice`  | *set in `init.sql` (bcrypt only)* | USER | Demo user (alice@example.com)    |
| `john`   | *set in `init.sql` (bcrypt only)* | USER | Demo user (john@example.com)     |
| `newbie` | *set in `init.sql` (bcrypt only)* | USER | Demo user (newbie@example.com)   |

Passwords are stored as **bcrypt hashes**; only `root`'s plaintext (`admin123`) has been
verified against the running system. For a known credential, either log in and inspect
the target user, or simply create your own:

```bash
curl -X POST http://localhost:9090/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"me","password":"Pass@123","email":"me@example.com"}'
```

---

## API Reference

All requests go through the **gateway** at `http://localhost:9090` unless noted.
Authenticated = `Authorization: Bearer <jwt>` required. `token` is obtained from
`POST /auth/login` (returns `{token, username, role, expiresIn}`).

### Auth — `POST /auth/login` → `{token, username, role, expiresIn}` (24 h)

| Method | Endpoint          | Access       | Description                            |
|--------|-------------------|--------------|----------------------------------------|
| POST   | `/auth/register`  | public       | Create a `USER`; duplicate → `409`     |
| POST   | `/auth/login`     | public       | Issue JWT; bad credentials → `401`     |
| GET    | `/auth/me`        | any auth     | Current user                            |
| GET    | `/auth/admin`     | `ADMIN`      | Admin-only hello ("Welcome admin")      |

### Employee — base `/employee`

| Method | Endpoint                                  | Access            | Description                                    |
|--------|-------------------------------------------|-------------------|------------------------------------------------|
| POST   | `/employee`                                | `ADMIN`/`MANAGER` | Create employee; duplicate email → `400`      |
| GET    | `/employee/{id}`                           | any auth          | Employee by id                                 |
| GET    | `/employee/email/{empEmail}`               | `ADMIN`           | Employee by email (case-sensitive path)        |
| GET    | `/employee?page=&size=&sort=`              | any auth          | Paged list (`Page<EmployeeResponseDTO>`)       |
| GET    | `/employee/department/{department}`        | any auth          | By department                                  |
| GET    | `/employee/company/{companyName}`          | any auth          | By company                                     |
| GET    | `/employee/status/{status}`                | any auth          | By status (`ACTIVE`,`INACTIVE`,`ON_LEAVE`,`TERMINATED`) |
| GET    | `/employee/department/{d}/status/{s}`      | any auth          | Combined filter                                |
| GET    | `/employee/exists/{id}`                    | any auth          | `true`/`false`                                 |
| PUT    | `/employee/{id}`                           | `ADMIN`/`MANAGER` | Full update; email taken → `400`               |
| PATCH  | `/employee/{id}/status?status=ON_LEAVE`    | `ADMIN`           | Change status                                  |
| DELETE | `/employee/{id}`                           | `ADMIN`           | Delete → `204`                                 |
| GET    | `/employee/{id}/with-address`              | any auth          | Employee + addresses (reads `address_db`)      |

### Address — base `/addresses`

| Method | Endpoint                                       | Access            | Description                                          |
|--------|------------------------------------------------|-------------------|------------------------------------------------------|
| POST   | `/addresses`                                   | `ADMIN`/`MANAGER` | Create; duplicate `(employeeId, type)` → `400`      |
| GET    | `/addresses/{id}`                              | any auth          | Address by id                                        |
| GET    | `/addresses/employee/{employeeId}?page=&size=` | any auth          | Paged list for an employee                           |
| GET    | `/addresses/employee/{employeeId}/type/{type}` | any auth          | By type (`PERMANENT`/`TEMPORARY`)                    |
| GET    | `/addresses/city/{city}`                       | any auth          | By city                                              |
| GET    | `/addresses/country/{country}`                 | any auth          | By country                                           |
| GET    | `/addresses/employee/{employeeId}/count`       | any auth          | Count of addresses for an employee                   |
| GET    | `/addresses/exists/{id}`                       | any auth          | `true`/`false`                                       |
| PUT    | `/addresses/{id}`                              | `ADMIN`/`MANAGER` | Update; moving onto an existing `(employee,type)` → `400` |
| DELETE | `/addresses/{id}`                              | `ADMIN`           | Delete → `204`                                       |
| DELETE | `/addresses/employee/{employeeId}`             | `ADMIN`           | Delete all for an employee → `204`                   |

### Gateway extras

| Method | Endpoint              | Access | Description                                     |
|--------|-----------------------|--------|-------------------------------------------------|
| GET    | `/demo/employee-cb`   | none   | Circuit-breaker demo (`?fail=true` forces failure) |
| GET    | `/demo/address-cb`    | none   | Circuit-breaker demo (`?fail=true`)             |
| GET    | `/actuator/health`    | public | Health incl. circuit-breaker state              |
| GET    | `/actuator/circuitbreakers` | public | Resilience4J state                     |

> `AddressType` and `EmployeeStatus` are enumerated in the DTOs — an invalid value
> produces a `400` "Malformed or unreadable request body" / validation error.

---

## Security

- **JWT flow.** `Auth` signs HS256 JWTs (`subject` = username, `role` claim, 24 h expiry)
  with a shared `JWT_SECRET`. The **gateway** validates the token for every non-public
  route and augments the request with `X-User-Name` / `X-User-Role`. Each downstream
  service **re-validates** the same token in its own `JwtAuthenticationFilter` and
  constructs its `SecurityContext` from the claims — the gateway is not a single point
  of trust.
- **RBAC.** `@PreAuthorize` on controllers enforces `ADMIN` / `MANAGER` / `USER`.
  Writes to employees/addresses require `ADMIN` or `MANAGER`; deletes, status changes and
  email lookups require `ADMIN`; reads allow any authenticated role.
- **Passwords.** BCrypt (`BCryptPasswordEncoder`); never returned by API responses.
- **Secrets.** No secrets in code — everything comes from environment variables:
  `JWT_SECRET`, DB credentials, `EUREKA_USER`/`EUREKA_PASSWORD` via `.env` (gitignored,
  `.env.example` committed) and Kubernetes `Secret`s (`db-secrets`, `eureka-secrets`).
- **Eureka** is protected with HTTP `Basic` (spring security, all requests authenticated).
- **CORS** is allow-listed to `http://localhost:3000` (credentials allowed), so the
  gateway is callable from a local React/Vue dev server.

---

## Resilience

- **Circuit breakers** wrap the two write-heavy routes at the gateway:
  `EMPLOYEE-SERVICE` and `ADDRESS-SERVICE` (Resilience4J).
- **Config** (identical for both): COUNT_BASED sliding window of **5** calls,
  **5** minimum calls, **50 %** failure-rate threshold, **6 s** open-state wait,
  **3** permitted half-open calls, auto half-open transition, **5 s** per-call timeout.
- **Fallbacks** (`fallbackUri: forward:/employeeServiceFallback` / `…/addressServiceFallback`)
  return a `200` JSON `{service, status: DOWN, message: "… temporarily unavailable …"}`
  instead of crashing the caller.
- **Graceful degradation** is also enforced inside `EmployeeService.getEmployeeWithAddress`:
  if the `address_db` read fails, it throws `AddressServiceUnavailableException` (→ `503`)
  rather than returning half-built data.
- The read path uses a **dedicated read-only DataSource** (`hikari read-only`, own
  transaction manager, `hbm2ddl=validate`) so cross-DB reads cannot write to `address_db`
  and don't share connection pools with the primary `employee` datasource.

---

## Testing

**318 tests, 0 failures** across the five modules (last full run):

| Module         | Tests | Covered by                                                        |
|----------------|-------|-------------------------------------------------------------------|
| EurekaServer   | 7     | context load, HTTP Basic security                                  |
| Auth           | 39    | controller (web slice), service unit, JWT, repository, security integration |
| EmployeeService| 130   | controller, exception handlers, security, repository, mapper, full-flow integration |
| AddressService | 103   | integration, controller, exception, security, mapper, service unit |
| ApiGateway     | 39    | AuthFilter, RouteValidator, circuit breaker, fallback, JWT, demo  |

Test types: plain unit tests (Mockito), `@WebMvcTest` web slices, `MockMvc` security
tests, `@DataJpaTest`-style repository tests, and full Spring-context integration tests
using **H2 in MySQL mode** (so queries and constraints are portable to MySQL).

Run everything locally:

```bash
# one service
cd EmployeeService && sh mvnw test

# all services
for s in EurekaServer Auth EmployeeService AddressService ApiGateway; do
  (cd $s && sh mvnw -B -ntp clean verify)
done

# reports land in <service>/target/surefire-reports/
```

The same commands run automatically for every push/PR via the CI pipeline.

---

## CI/CD

`.github/workflows/ci.yml` runs on `main` / `develop`, on any `v*` tag, and on PRs:

1. **`build-and-test`** — matrix over all five services: `sh mvnw clean verify`,
   uploading surefire reports and JARs as artifacts.
2. **`docker-build`** (after build-and-test) — downloads the JAR artifact per service,
   logs into Docker Hub, and pushes each image with **dynamically computed tags**:
   always `latest` + `:<sha>`, plus `:v<tag>` when the run was triggered by a
   git tag (`refs/tags/v*`, e.g. `v1.0.0`).
3. **`ci-success`** — a final gate that only passes when both jobs succeed.

**Trigger a release** (this is the intended workflow):

```bash
git tag v1.0.0
git push origin v1.0.0
```

After the pipeline finishes, `arjunchaudhary/<service>` will carry
`:latest`, `:<commit-sha>`, and `:v1.0.0`.

---

## Deployment

**Docker Compose (local):** images are built from source (`build: ./<service>`) and
tagged `:1.0` locally; environment comes from `.env`. `docker compose down -v` +
`up -d` resets the DB to the seed state.

**Kubernetes (Minikube):** manifests under `kubernetes/` create the `microservices`
namespace, ConfigMap, Secrets, a MySQL StatefulSet (init SQL as ConfigMap), five
Deployments with health-dependent init containers, ClusterIP services, and a **NodePort
(`30090`)** for the gateway.

**Docker Hub:** CI publishes `latest` + `:<sha>` (+ `:v*` for tags) under the
`arjunchaudhary` namespace.

**Update a deployed version** — point the image tag to a release and re-apply:

```bash
kubectl -n microservices set image deployment/employee-service \
  employee-service=arjunchaudhary/employee-service:v1.0.1
kubectl -n microservices rollout status deployment/employee-service
```

---

## Configuration

Environment variables (defaults shown as shipped — override in `.env` / Secrets):

| Variable                  | Purpose                              | Default                            |
|---------------------------|--------------------------------------|------------------------------------|
| `JWT_SECRET`              | HS256 signing key (shared, ≥32 B)    | `a3f1b8d4…c8d9e0f` (dev only)      |
| `MYSQL_ROOT_PASSWORD`     | MySQL root password                  | `root`                             |
| `EUREKA_USER`             | Eureka Basic-auth username           | `eureka`                           |
| `EUREKA_PASSWORD`         | Eureka Basic-auth password           | `eureka`                           |
| `EMPLOYEE_DB_USERNAME`    | Employee primary DataSource user     | `employee_user`                    |
| `EMPLOYEE_DB_PASSWORD`    | Employee primary DataSource password | `employee_pwd`                     |
| `EMPLOYEE_ADDRESS_RO_USERNAME` | Read-only `address_db` user     | `employee_address_ro`              |
| `EMPLOYEE_ADDRESS_RO_PASSWORD` | Read-only `address_db` password | `employee_ro_pwd`               |
| `ADDRESS_DB_USERNAME`     | AddressService DataSource user       | `address_user`                     |
| `ADDRESS_DB_PASSWORD`     | AddressService DataSource password   | `address_pwd`                      |
| `AUTH_DB_USERNAME`        | AuthService DataSource user          | `auth_user`                        |
| `AUTH_DB_PASSWORD`        | AuthService DataSource password      | `auth_pwd`                         |

Other settings per service: `server.port`, `jwt.expiration` (86400000 ms),
Eureka `defaultZone` with embedded credentials, `eureka.instance.prefer-ip-address`
(in containers), and Hikari pool sizes.

---
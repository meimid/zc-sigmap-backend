# AI Coding Agent Instructions for zc-sigmap-backend

Concise project-specific guidance so an agent can be productive quickly. Keep answers/action aligned with these patterns.

## 1. Architecture Overview
- Maven multi-module monorepo. Parent POM: `pom.xml` lists order: libraries (`sigmap-migrations`, `sigmap-common`) then service apps (`sigmap-params`, `sigmap-agpm`, `sigmap-paa`, `sigmap-ppm`, `sigmap-dac`, `sigmap-auth`, `sigmap-webmaster`). Version aligned at `0.0.1`.
- Each application is an independent Spring Boot service (port + context-path configured in its `application.yml`). Shared domain logic & DTOs live in `sigmap-common` (e.g. `com.sigmap.common.agpm.*`). Database change management & baseline DDL lives in `sigmap-migrations` (Liquibase + formatted SQL like `init.mariadb.sql`).
- Cross-cutting concerns: Security (Keycloak / JWT resource-server), JPA/Hibernate (MariaDB), Liquibase migrations, Flowable BPM (in `sigmap-agpm` with its own datasource/DB), file/document handling (Alfresco, PDF/Excel parsing via Apache POI/PDFBox, JasperReports for reporting), mail (Spring Mail + Thymeleaf templates), CORS configured per service.
- Versioned domain entities use composite PK `(id, ver)` pattern (e.g. `agpm`, `dac`, `paa`, `ppm`, `lots`, `realisation`, `dyna_docs` tables). API controllers expose latest version by id and history endpoints.

## 2. Layering & Naming Conventions
- Package root per service: `com.sigmap.sigmap_<module>`.
- Typical subpackages: `controller/`, `service/`, `config/`, `exeptions/` (typo retained—match existing), plus domain-specific packages in `sigmap-common` (e.g. `com.sigmap.common.agpm`).
- Mapping handled with MapStruct (interfaces in common, annotation processors configured; DO NOT manually write repetitive mapping—add mapper methods instead).
- Logging: Lombok `@Slf4j` on controllers/services; favor structured messages instead of `System.out.println` (replace prints in future edits, e.g. in `KeycloakJwtConverter`).
- File uploads: multipart endpoints consume `multipart/form-data`; size enforcement via injected `@Value` (`max-file-size`). Use `FileCheckServices.chechFileUpload(...)` before persisting.

## 3. Persistence & Migrations
- Liquibase YAML changelog per module references SQL or structured changes; baseline large schema in `init.mariadb.sql` (formatted SQL with `-- changeset`). When adding tables/columns: create a new Liquibase changeset—do NOT edit prior ones.
- Entity versioning: Updates that should create a new immutable version use `(id, ver)` insertion; some endpoints (e.g. `updateWithoutVersioning`) modify latest row in-place—respect existing semantics.
- Soft delete: Tables have `is_deleted`, plus audit columns (`created_at/by`, `updated_at/by`, `deleted_at/by`). When "deleting" implement soft delete toggling these fields; never hard-delete unless a maintenance/cleanup batch.

## 4. Security & Auth
- Resource servers: Spring Security stateless JWT. Permit actuator, swagger (`/api/docs*`, `/swagger-ui/**`), token/test endpoints; all else authenticated.
- Role resolution merges realm roles and client roles (see `KeycloakJwtConverter`). When adding authorization logic, prefer `@PreAuthorize("hasRole('XYZ')")` and rely on the existing converter's `ROLE_` prefix.
- CORS origins configured under `cors.origins` list; when adding new external UIs, extend pattern array in each service config.

## 5. Flowable (BPM) Integration (AGPM service)
- `sigmap-agpm` isolates Flowable in a separate MariaDB (`mariadb-flowable` on port 3307) with `flowable.*` spring config and `use-default-data-source: false`.
- Process metadata keys configured via properties (`flowable.process-definition-key`, `task-definition-key`). Place BPMN XML under `classpath:/processes/`.
- When introducing new workflows: add BPMN file, reference property overrides, ensure datasource points to flowable DB.

## 6. External Services & Files
- Docker compose starts: `mariadb-sigmap`, `mariadb-flowable`, `gotenberg` (for document conversion). Alfresco endpoints assumed externally hosted (see `alfresco.*`). Gotenberg container mounts `./documents` host dir.
- File storage paths configurable (`file.upload.*`); Windows style defaults indicate deployment environment heterogeneity—use property overrides for Linux paths instead of code changes.

## 7. Build & Run Workflow
- Build all modules: `./mvnw clean install` (executes compilation + tests). To skip tests (not recommended): `-DskipTests`.
- Run a single service during development: From module dir: `./mvnw spring-boot:run -pl <module> -am` or simply `./mvnw -pl sigmap-auth spring-boot:run` at root.
- Local infra: `docker compose up -d mariadb-sigmap mariadb-flowable gotenberg` before launching services needing DB / conversion.
- DB schema validation: Each service uses `spring.jpa.hibernate.ddl-auto=validate`; mismatches will fail fast—add Liquibase changesets first.

## 8. API & Documentation
- Swagger/OpenAPI served at `<context-path>/api/docs` JSON and UI at `<context-path>/api/docs-ui.html` (e.g. `http://localhost:8086/auth/api/docs-ui.html`). Keep new endpoints annotated with Spring MVC; scanning auto-exposes.
- Version/history endpoints pattern: `/api/<entity>/{id}` for latest, `/api/<entity>/{id}/{ver}` for specific, `/api/<entity>/history/(full|minimal)/{id}` for audit trail. Follow this naming when adding versioned resources.

## 9. Patterns to Reuse
- Controller -> Service -> Repository (in `sigmap-common`) separation. Put reusable domain logic in `sigmap-common` to avoid duplication across services.
- DTO mapping & pagination: Accept `Pageable` directly in controller; convert `Page<Entity>` to `Page<DTO>` via `map(mapper::map)`.
- Specification-based filtering (see `AgpmQuery.toSpecification()` usage). New list endpoints should accept a query object that builds a JPA `Specification` for consistency.

## 10. Testing Guidance
- Use Spring Boot test slices with Testcontainers (MariaDB) (dependencies already present in module POMs). Prefer integration tests at service layer verifying Liquibase migrations + repository behavior for versioned entities.
- For security tests, leverage `spring-security-test` with JWT mocks rather than disabling security.

## 11. When Editing / Adding Code
- Maintain Lombok & MapStruct annotation processor config (do not remove `annotationProcessorPaths`).
- Avoid hardcoding secrets; keep using `${ENV_VAR:default}` pattern in `application.yml`.
- Replace any lingering `System.out.println` with logger calls.
- For new file upload endpoints: enforce file size via existing `FileCheckServices` and reuse `max-file-size` property binding.

## 12. Quick Reference Paths
- Parent POM: `/pom.xml`
- Common library: `/sigmap-common/src/main/java/com/sigmap/common/...`
- Auth security setup: `/sigmap-auth/src/main/java/com/sigmap/sigmap_auth/SecurityConfig.java`
- Example versioned controller: `/sigmap-agpm/.../controller/AgpmController.java`
- DB migrations: `/sigmap-migrations/src/main/resources/db/sql/...`
- Docker infra: `/docker-compose.yml`

(End of instructions – refine with real code patterns as the project evolves.)

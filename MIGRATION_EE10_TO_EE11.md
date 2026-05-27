# Jakarta EE 10 to EE 11 Migration Guide

## Overview

Jakarta EE 11, released in 2024, raises the platform-wide minimum Java SE requirement to Java 17 and completes the removal of `SecurityManager` references that was signaled across the platform in EE 10. Key highlights include the introduction of **Jakarta Data 1.0**, a new specification providing repository-pattern data access that integrates with Persistence, NoSQL, CDI, Transactions, and Validation; **Jakarta Persistence 3.2** delivering the most comprehensive Persistence API expansion in years (Records as embeddables, new query operations, Schema Manager, `PersistenceConfiguration` API); the removal of **Jakarta XML Binding and Jakarta XML Web Services from the Full Platform** (both are pruned to optional/standalone status); a CDI-centric pivot across multiple specifications (RESTful WS 4.0 removing `@ManagedBean`, JAX-RS removing JAXB dependency, CDI 4.1 adding method invokers); Expression Language 6.0 removing the `java.desktop` dependency entirely; and Java 21 virtual thread integration in Concurrency 3.1.

---

## Platform Changes

### Java SE Minimum Version
- **Jakarta EE 10 required Java SE 11 or higher**
- **Jakarta EE 11 requires Java SE 17 or higher** across all specifications (several specs still compile at bytecode level 11 but the platform requirement is 17)

### SecurityManager Removal
- `java.lang.SecurityManager` was deprecated for removal in JDK 17 (JEP 411) and removed in JDK 24. Jakarta EE 11 systematically removes all `SecurityManager` references and checks from specifications that still had them: Authentication, Authorization, Expression Language, Faces, Servlet, WebSocket, and Security.

### Specifications Added
- **Jakarta Data 1.0** — brand new specification for repository-pattern data access. Integrates with CDI, Persistence, NoSQL, Transactions, and Validation.

### Specifications Removed from Platform
- **Jakarta XML Binding (JAXB) 4.0** — removed from the Jakarta EE Full Platform (pruned). It becomes a standalone specification. Applications that need JAXB must now explicitly include the dependency.
- **Jakarta XML Web Services (JAX-WS) 4.0** — removed from the Jakarta EE Full Platform (pruned). Same as JAXB.

**Action Required:** Any application that implicitly relied on JAXB or JAX-WS being provided by the container must now add explicit Maven/Gradle dependencies for these artifacts.

---

## Specification Changes

### 1. Jakarta Activation (2.1 → 2.1, maintenance)

**Status:** Maintenance release. No functional changes for the EE 11 cycle.

**New Features:** None

**Breaking Changes / Removals / Deprecations:** None

---

### 2. Jakarta Annotations (2.1 → 3.0)

**New Features:**
- No new annotations or features were added.

**Breaking Changes / Removals / Deprecations:**
- **The deprecated `@ManagedBean` annotation has been fully removed.** This annotation was deprecated in Jakarta Annotations 2.1 (via EE 9) and first introduced as a Jakarta EE concept for simple managed components. Any code annotated with `jakarta.annotation.ManagedBean` must be migrated to CDI `@Named` beans.

**Action Required:** Search for `@ManagedBean` throughout your codebase and migrate to `@Named` + appropriate CDI scope (`@RequestScoped`, `@SessionScoped`, `@ApplicationScoped`, etc.).

---

### 3. Jakarta Authentication / JASPIC (3.0 → 3.1)

**New Features:**
- None (this release focuses on cleanup).

**Breaking Changes / Removals / Deprecations:**
- **All references to `SecurityManager` have been removed.** Any `ServerAuthModule`, `ClientAuthModule`, or related implementation code that called `SecurityManager` APIs or relied on security manager-mediated access checks must be updated.

---

### 4. Jakarta Authorization / JACC (2.1 → 3.0)

**New Features:**
- Register a policy provider programmatically per application — mirrors the per-application registration API available in Jakarta Authentication, making the model consistent.
- Standardized the context ID for Servlet containers.
- Several convenience methods to make the API easier to use.

**Breaking Changes / Removals / Deprecations:**
- **Replacement for `java.security.Policy` designed and implemented.** The old `Policy`-based authorization model is superseded by a new API. Custom `Policy` subclasses used as authorization providers must be migrated.
- **All references to `SecurityManager` removed.**

**Action Required:** If you implemented a custom JACC `Policy` provider, migrate to the new programmatic policy provider registration API introduced in this release.

---

### 5. Jakarta Batch (2.1 → 2.1, unchanged)

**Status:** No changes for Jakarta EE 11. Version remains 2.1.

**New Features:** None

**Breaking Changes / Removals / Deprecations:** None

---

### 6. Jakarta Bean Validation (3.0 → 3.1)

**New Features:**
- **Clarified Java Records support** — the specification now explicitly defines how `@NotNull`, `@Min`, `@Max`, and other constraints apply to record components, and how validation of records works with the standard validation API.
- Updated dependencies for Jakarta EE 11.

**Breaking Changes / Removals / Deprecations:**
- None

**Migration Note:** If your application uses Java records as DTOs or value objects, you can now reliably annotate record components with Bean Validation constraints and have them validated through the standard API.

---

### 7. Jakarta Contexts and Dependency Injection / CDI (4.0 → 4.1)

**New Features:**
- **Method invokers** — a new programmatic API allowing invocation of a bean method using the CDI container's injection facilities.
- **Executable methods** — related to method invokers, providing standardized access to metadata about methods on managed beans.
- **`@Priority` on producers** — `@Priority` can now be used on producer methods and fields.
- Getting interceptor bindings in a standard way via `InvocationContext`.
- Programmatic access to CDI assignability rules.
- Methods added to `BeanConfigurator` for applying decorators.
- Removed circular dependencies between spec and TCK modules.
- Delegated integration requirements to Jakarta Platform specifications.
- Improved wording of managed bean requirements regarding non-static public fields.

**Breaking Changes / Removals / Deprecations:**
- Expression Language SPI methods on `BeanManager` (specifically `createELResolver()`, `getELResolver()`, `wrapExpressionFactory()`) are **deprecated** in preparation for moving to a new subinterface (`BeanContainer`). This removes the `jakarta.el` dependency from the core CDI API artifact. A new `jakarta.enterprise:jakarta.enterprise.cdi-el-api` artifact provides the EL-integrated API.

**Action Required:** Review code that calls `BeanManager.createELResolver()`, `getELResolver()`, or `wrapExpressionFactory()`. Plan migration to the new `jakarta.enterprise.cdi-el-api` artifact if you need EL integration.

---

### 8. Jakarta Concurrency (3.0 → 3.1)

**New Features:**
- **Integration with Java 21 Virtual Threads** — managed executors can be configured to use virtual threads as the underlying thread type, enabling efficient handling of high-concurrency workloads.
- **Java Flow/ReactiveStreams context propagation** — Jakarta EE context can now be propagated into reactive streams and Flow-based pipelines.
- **Replace more features from EJB** — scheduling annotations and capabilities previously exclusive to EJB (such as the `@Schedule`-equivalent for managed resources) are now available through Concurrency.
- Became more CDI-centric — better integration with CDI contexts and lifecycle.
- Specification bug fixes and clarifications.

**Breaking Changes / Removals / Deprecations:**
- None

**Migration Note:** If you use EJB `@Schedule` for background scheduling, consider migrating to the new Concurrency-based scheduling APIs to reduce EJB dependency.

---

### 9. Jakarta Data (NEW — 1.0)

**Status:** Brand new specification introduced in Jakarta EE 11.

**New Features:**
- **Repository pattern** via `@Repository`-annotated interfaces. Define query methods by convention or annotation without writing boilerplate CRUD code.
- **Pagination** built into repositories via `PageRequest` and `Page`.
- **Type-safe static metamodel** (`StaticMetamodel`) for use in queries.
- **Platform integrations:**
  - CDI: repositories are CDI beans, injected via `@Inject`.
  - Persistence: works with JPA entities and `EntityManager`.
  - NoSQL: works with NoSQL document/key-value stores.
  - Transactions: repository operations participate in JTA transactions.
  - Validation: entities are validated through Bean Validation.
- Queries can be expressed via method naming conventions, JDQL (Jakarta Data Query Language), or JPQL.

**Breaking Changes / Removals / Deprecations:**
- None (first release).

**Migration Opportunity:** Jakarta Data provides a standardized replacement for home-grown repository utility classes, Spring Data-style repository patterns, and boilerplate `EntityManager` usage. Applications may choose to incrementally adopt it for new data access code.

---

### 10. Jakarta Enterprise Beans / EJB (4.0 → 4.0, unchanged)

**Status:** No changes for Jakarta EE 11. Version remains 4.0.

**New Features:** None

**Breaking Changes / Removals / Deprecations:** None

**Note:** While EJB itself is unchanged, several features traditionally exclusive to EJB (scheduling, asynchronous invocation) are being replicated in CDI and Concurrency. Consider migrating where appropriate.

---

### 11. Jakarta Expression Language (5.0 → 6.0)

**New Features:**
- **`java.desktop` module is no longer required at runtime** — the long-standing dependency on `java.desktop` (for `java.beans.FeatureDescriptor` used in `getFeatureDescriptors()`) has been eliminated.
- **Array `length` property support** — the `length` property is now supported for arrays in EL expressions (e.g., `${myArray.length}`).
- **`RecordELResolver` added** — new built-in resolver for `java.lang.Record` instances, enabled by default. Record components are now directly accessible in EL expressions.
- **`OptionalELResolver` added** — new built-in resolver for `java.util.Optional` instances, disabled by default. When enabled, allows EL to transparently unwrap `Optional` values.

**Breaking Changes / Removals / Deprecations:**
- **All code deprecated as of EL 5.0 has been removed.** Specifically:
  - `ELResolver.getFeatureDescriptors()` method has been **removed** from the `ELResolver` interface. Any custom `ELResolver` implementations that override this method must remove the override.
- **All references to `SecurityManager` and associated APIs have been removed.**

**Action Required:** Remove `getFeatureDescriptors()` overrides from all custom `ELResolver` implementations.

---

### 12. Jakarta Faces (4.0 → 4.1)

**New Features:**
- `FacesMessage#VALUES` and `VALUES_MAP` fields are now generic.
- Container is required to fire `@Initialized`, `@BeforeDestroyed`, and `@Destroyed` events for built-in scopes.
- Added missing generic type parameters to APIs that were missed in Faces 4.0.
- Support for `@Inject Flow currentFlow` to inject the current flow directly.
- Added `UUIDConverter` for binding UUID values.
- Added `ExternalContext.setResponseContentLengthLong(long)` for large responses.
- Added `rowStatePreserved` property to `UIRepeat` (matching behavior already available on `UIData`).
- Clarified the default value of `jakarta.faces.FACELETS_REFRESH_PERIOD` when `ProjectStage` is `Development`.
- `FacesMessage` now implements `equals()`, `hashCode()`, and `toString()`.

**Breaking Changes / Removals / Deprecations:**
- **Deprecated Full State Saving (FSS).** Partial state saving (PSS) is now the preferred approach. Applications still using FSS (`javax.faces.STATE_SAVING_METHOD=server` with full-tree serialization) should migrate.
- **Deprecated `PreDestroyCustomScopeEvent` and `PostConstructCustomScopeEvent`** as unused.
- **Deprecated `composite:extension`** as unused.
- **All references to `SecurityManager` have been removed.**

---

### 13. Jakarta Interceptors (2.1 → 2.2)

**New Features:**
- Updated dependencies for Jakarta EE 11 (Jakarta Annotations 3.0).
- Added standard accessor to retrieve interceptor bindings from an `InvocationContext`.
- Provided programmatic access to interceptor bindings from `InvocationContext` — interceptors can now inspect their own binding annotations at runtime in a standardized way.
- Clarified the precise language around `InvocationContext.getInterceptorBindings()` including behavior for inherited and transitive bindings.

**Breaking Changes / Removals / Deprecations:**
- None

---

### 14. Jakarta JSON Binding / JSON-B (3.0 → 3.0, maintenance)

**Status:** Maintenance release. No functional changes for the EE 11 cycle.

**New Features:** None

**Breaking Changes / Removals / Deprecations:** None

---

### 15. Jakarta JSON Processing / JSON-P (2.1 → 2.1, maintenance)

**Status:** Maintenance release. No functional changes for the EE 11 cycle.

**New Features:** None

**Breaking Changes / Removals / Deprecations:** None

---

### 16. Jakarta Mail (2.1 → 2.1, maintenance)

**Status:** Maintenance release. No functional changes for the EE 11 cycle.

**New Features:** None

**Breaking Changes / Removals / Deprecations:** None

---

### 17. Jakarta Messaging / JMS (3.1 → 3.1, unchanged)

**Status:** No changes for Jakarta EE 11. Version remains 3.1.

**New Features:** None

**Breaking Changes / Removals / Deprecations:** None

---

### 18. Jakarta Pages / JSP (3.1 → 4.0)

**New Features:**
- `ErrorData` updated to add support for two new error dispatch attributes: `jakarta.servlet.error.query_string` and `jakarta.servlet.error.method`.

**Breaking Changes / Removals / Deprecations:**
- **All code deprecated as of Jakarta Server Pages 3.1 has been removed.** Specifically:
  - Removed methods that override `ELResolver.getFeatureDescriptors()` — this method is removed in EL 6.0.
  - **Removed `isThreadSafe` page directive attribute** — `SingleThreadModel` was removed in Servlet 6.0.
  - **Removed `jsp:plugin`**, `jsp:params`, and `jsp:fallback` actions — the associated HTML elements are no longer supported by major browsers.
  - **Removed `JspException.getRootCause()`** — use the standard `Throwable.getCause()` instead.

**Action Required:**
- Remove `isThreadSafe` page directives from JSP pages.
- Remove `<jsp:plugin>` and related tags from JSP pages.
- Replace `JspException.getRootCause()` calls with `getCause()`.

---

### 19. Jakarta Persistence (3.1 → 3.2)

**New Features:**
- **Java Records as embeddable classes** — `@Embeddable`-annotated record types are now supported.
- **`java.time.Instant` and `java.time.Year` support** — added as mappable basic types; JDBC type mappings clarified for all basic types.
- **New JPQL / Criteria operations:**
  - `union`, `intersect`, `except` set operations for JPQL and criteria queries.
  - `cast`, `left`, `right`, `replace` string/type functions.
  - `||` string concatenation operator in JPQL.
  - `id()` and `version()` functions in JPQL.
  - `CriteriaSelect` interface and `subquery(EntityType)` method.
  - Joins on `EntityType` in Criteria API.
  - Null precedence specification in ORDER BY (`NULLS FIRST`, `NULLS LAST`).
  - Scalar expressions in the ORDER BY clause.
- **`getSingleResultOrNull()`** added to `Query`, `TypedQuery`, and `StoredProcedureQuery` — eliminates the common `try/catch` pattern for optional single results.
- **`entities()`, `classes()`, `columns()`** members added to `@NamedNativeQuery`.
- **`lockMode()`** added to `@EntityResult` (defaults to `OPTIMISTIC`).
- **`PersistenceUnitUtil`** gains `getVersion()`, `isLoaded()`, `load()`, `isInstance()`, and `getClass()` methods.
- **`Metamodel.entity(String)`** — new overload accepting entity name.
- **`StaticMetamodel` enhancements** — constants for managed types, named queries, named graphs, and named result set mappings added to generated static metamodels.
- **`LocalDateTime` and `Instant`** added to supported `@Version` types.
- **`CriteriaBuilder/CriteriaQuery`** gains `where()`, `having()`, `and()`, `or()` overloads accepting `List<Predicate>`.
- **`Expression`** gains `equalTo()` and `notEqualTo()` methods.
- **`CriteriaBuilder`** gains `concat()` overload accepting a list of expressions and `extract()`.
- **`Graph` interface** added as parent of `EntityGraph` and `Subgraph`, consolidating common operations.
- **`EntityManager`** gains `getReference()` overload, `runWithConnection()`, and `callWithConnection()`.
- **`EntityManager.find()`, `refresh()`, `lock()`** gain new overloads accepting `FindOption`, `RefreshOption`, and `LockOption` for fine-grained control.
- **`EntityManager` and `Query`** gain `setCacheStoreMode()` and `setCacheRetrieveMode()` methods.
- **`EntityManagerFactory`** gains `runInTransaction()`, `callInTransaction()`, and `getName()`.
- **`PersistenceConfiguration` API** — new programmatic API to obtain `EntityManagerFactory` without a `persistence.xml`.
- **`SchemaManager` API** — new API for programmatic DDL generation and schema validation.
- **`EnumeratedValue`** — allows custom mapping of Java enum field values.
- **`@Column`, `@Table`** gain `comment` and `check` members, plus new `@CheckConstraint`.
- **`@Column`** gains `secondPrecision` for fractional second precision control.
- **Factory-level access** to named queries and named entity graphs via `EntityManagerFactory`.
- **`TypedQueryReference`** for type-safe references to named queries.
- **Dependency injection integration points** in Persistence (for use with Jakarta Data).
- Entity and embeddable classes may now be **static inner classes**.
- Primary key classes are no longer required to be `public` and `Serializable`.
- `@TableGenerator` and `@SequenceGenerator` can now be used at the package level.
- `name` member of `@TableGenerator` and `@SequenceGenerator` is now optional.
- Identification variables and `SELECT` clause in JPQL are now optional.

**Breaking Changes / Removals / Deprecations:**

*Deprecations:*
- Use of `Calendar`, `Date`, `Time`, `Timestamp`, `@Temporal`, `@MapKeyTemporal`, and `TemporalType` deprecated in favor of `java.time` API.
- `CriteriaQuery.multiselect()` methods deprecated; use `array()` or `tuple()` on `CriteriaBuilder` instead.
- `Byte[]` and `Character[]` (boxed arrays) for basic attributes deprecated in favor of primitive `byte[]` and `char[]`.

*Deprecations for Removal:*
- `EntityGraph.addSubclassSubgraph()` deprecated for removal; use `addTreatedSubgraph()`.
- `Graph.addSubgraph(Attribute, Class)` and `addKeySubgraph()` deprecated for removal; use `addTreatedSubgraph(Attribute, Class)` and `addMapKeySubgraph()`.
- `jakarta.persistence.spi.PersistenceUnitTransactionType` deprecated for removal; use `jakarta.persistence.PersistenceUnitTransactionType` instead.
- Default public no-arg constructor in `jakarta.persistence.Persistence` and the `PERSISTENCE_PROVIDER` / `providers` fields deprecated for removal.

**Action Required:**
- Migrate date/time mappings from `java.util.Date`/`Calendar` to `java.time` types.
- Replace `multiselect()` usage with `array()` or `tuple()`.
- Replace boxed `Byte[]`/`Character[]` array attributes with primitive arrays.
- Replace `addSubclassSubgraph()` calls with `addTreatedSubgraph()`.
- Replace `PersistenceUnitTransactionType` SPI references.

---

### 20. Jakarta RESTful Web Services / JAX-RS (3.1 → 4.0)

**New Features:**
- Enhanced TCK coverage for multipart/form-data API (from 3.1).
- Enhanced TCK coverage for the default `ExceptionMapper`.
- New convenience method for checking whether a header value list contains a particular value.
- `UriInfo.getMatchedResourceTemplate()` added to retrieve the URI template that matched the current request.
- **JSON Merge Patch support** — new built-in support for `application/merge-patch+json` media type.

**Breaking Changes / Removals / Deprecations:**
- **JAXB dependency removed from Jakarta RESTful Web Services.** The built-in support for XML binding via JAXB is no longer part of the spec. Applications that serve or consume XML responses via automatic JAXB marshalling must now explicitly include a JAXB implementation and register an appropriate `MessageBodyReader`/`MessageBodyWriter`.
- **`@ManagedBean` support removed.** JAX-RS resource classes, providers, and `Application` subclasses can no longer use `@ManagedBean`. Use CDI beans instead (most resource classes should already be CDI-managed).

**Action Required:**
- Add explicit JAXB dependency if your JAX-RS endpoints produce or consume `application/xml` using JAXB-annotated classes.
- Remove `@ManagedBean` from JAX-RS components; ensure they are CDI-managed (annotate with appropriate CDI scope or rely on default per-request CDI management).

---

### 21. Jakarta Security (3.0 → 4.0)

**New Features:**
- **Basic API (handler) for Multiple authentication mechanisms** — applications can now compose multiple authentication mechanisms and select among them at runtime.
- **Qualifiers for built-in authentication mechanisms** — `@BasicAuthenticationMechanismDefinition`, `@FormAuthenticationMechanismDefinition`, `@CustomFormAuthenticationMechanismDefinition`, and `@OpenIdAuthenticationMechanismDefinition` now have CDI qualifiers by default.
- **In-memory identity store** — a new built-in `IdentityStore` backed by an in-memory user/credential map, useful for testing and simple deployments.

**Breaking Changes / Removals / Deprecations:**
- **All references to `SecurityManager` removed.**
- **Built-in authentication mechanisms now have a qualifier by default**, whereas before they were unqualified. This is a **breaking change** if you injected a built-in mechanism by type without specifying a qualifier. If you have code such as `@Inject HttpAuthenticationMechanism mechanism`, you may need to add the appropriate qualifier.

**Action Required:** Review injection points for built-in authentication mechanisms and add the appropriate qualifier annotation if the injection was relying on the previously unqualified nature of the built-in mechanisms.

---

### 22. Jakarta Servlet (6.0 → 6.1)

**New Features:**
- **Control of status code and response body when sending a redirect** — `HttpServletResponse.sendRedirect()` gains overloads allowing the application to specify the status code (e.g., 307, 308) and optionally a response body.
- **Query string attribute in error dispatches** — the `jakarta.servlet.error.query_string` request attribute is now set during error dispatches.
- **New HTTP status code constants** added to `HttpServletResponse`.
- **`Charset`-based overloaded methods** — `setCharacterEncoding(Charset)` and other string-encoding methods now have `Charset`-typed overloads for type safety.
- **`ByteBuffer` support** added to `ServletInputStream` and `ServletOutputStream` for non-blocking NIO-based I/O.
- Various clarifications.

**Breaking Changes / Removals / Deprecations:**
- **All references to `SecurityManager` and associated APIs have been removed.** Any code that called `SecurityManager` methods within a Servlet context (including `AccessController.doPrivileged()` patterns in filters or servlets) must be updated.

---

### 23. Jakarta Standard Tag Library / JSTL (3.0 → 3.0, maintenance)

**Status:** Maintenance release. No functional changes for the EE 11 cycle.

**New Features:** None

**Breaking Changes / Removals / Deprecations:** None

---

### 24. Jakarta Transactions (2.0 → 2.0, unchanged)

**Status:** No changes for Jakarta EE 11. Version remains 2.0.

**New Features:** None

**Breaking Changes / Removals / Deprecations:** None

---

### 25. Jakarta WebSocket (2.1 → 2.2)

**New Features:**
- Clarified responsibilities for sending ping and pong messages.
- Added `SendResult.getSession()` method to retrieve the session associated with a send result.
- Clarified behavior when `@OnMessage.maxMessageSize` is set to a value larger than `Integer.MAX_VALUE`.

**Breaking Changes / Removals / Deprecations:**
- **All references to `SecurityManager` have been removed.**

---

### 26. Jakarta XML Binding / JAXB (REMOVED from Full Platform)

**Status:** Jakarta XML Binding 4.0 has been **pruned from the Jakarta EE Full Platform**. It is no longer a required component of compliant application servers.

**Impact:**
- Application servers are no longer required to bundle a JAXB implementation.
- Applications that use JAXB (directly or via JAX-WS, JAX-RS XML marshalling, or any other path) must now **explicitly declare the JAXB API and implementation as dependencies**.

**Action Required:**
- Add `jakarta.xml.bind:jakarta.xml.bind-api:4.0.x` (or later) to your dependencies.
- Add a JAXB implementation, such as `org.glassfish.jaxb:jaxb-ri` (Eclipse Implementation of JAXB), to your runtime dependencies.
- Search for any transitive reliance on JAXB being available from the container classpath.

---

### 27. Jakarta XML Web Services / JAX-WS (REMOVED from Full Platform)

**Status:** Jakarta XML Web Services 4.0 has been **pruned from the Jakarta EE Full Platform**. It is no longer a required component of compliant application servers.

**Impact:**
- Application servers are no longer required to bundle a JAX-WS implementation.
- Applications using SOAP-based web services via JAX-WS must now explicitly include the API and implementation.

**Action Required:**
- Add `jakarta.xml.ws:jakarta.xml.ws-api:4.0.x` (or later) as an explicit dependency.
- Add a JAX-WS implementation, such as Eclipse Metro (`org.glassfish.metro:webservices-rt`), to your runtime dependencies.
- If your application is a SOAP web service server or client, ensure these dependencies are packaged in your application archive (WAR/EAR).

---

## Migration Checklist

Use the following checklist when migrating an application from Jakarta EE 10 to Jakarta EE 11.

### Java SE
- [ ] Verify the application compiles and runs on Java SE 17 or higher.
- [ ] Review any remaining `SecurityManager` / `AccessController.doPrivileged()` usage across all application code — these APIs are effectively non-functional on Java 24+ and references to them should be removed.

### Platform Dependencies (Critical)
- [ ] **If your application uses JAXB:** Add `jakarta.xml.bind:jakarta.xml.bind-api` and a JAXB runtime implementation as explicit dependencies. JAXB is no longer provided by the container.
- [ ] **If your application uses JAX-WS (SOAP):** Add `jakarta.xml.ws:jakarta.xml.ws-api` and a JAX-WS runtime implementation as explicit dependencies. JAX-WS is no longer provided by the container.
- [ ] Update the Jakarta EE BOM version reference in your project's `pom.xml` or `build.gradle` to the EE 11 BOM.

### Jakarta Annotations
- [ ] Search for `@jakarta.annotation.ManagedBean` (or `@ManagedBean` from `jakarta.annotation`) — this annotation has been **removed**. Migrate to CDI `@Named`.

### CDI
- [ ] Review calls to `BeanManager.createELResolver()`, `getELResolver()`, and `wrapExpressionFactory()` — these are deprecated. Update to use the new `jakarta.enterprise.cdi-el-api` artifact if needed.
- [ ] Optionally evaluate CDI 4.1 method invokers for replacing reflection-based dynamic method invocation.

### Jakarta Persistence
- [ ] **Date/time types:** Plan migration of `java.util.Date`, `Calendar`, `java.sql.Date/Time/Timestamp`, `@Temporal`, and `TemporalType` usages to `java.time` equivalents (`LocalDate`, `LocalDateTime`, `Instant`, etc.).
- [ ] Replace `CriteriaQuery.multiselect()` with `CriteriaBuilder.array()` or `CriteriaBuilder.tuple()`.
- [ ] Replace `EntityGraph.addSubclassSubgraph()` with `addTreatedSubgraph()`.
- [ ] Replace `Graph.addKeySubgraph()` with `addMapKeySubgraph()`.
- [ ] Replace `jakarta.persistence.spi.PersistenceUnitTransactionType` with `jakarta.persistence.PersistenceUnitTransactionType`.
- [ ] Replace `boxed` `Byte[]`/`Character[]` array attribute types with primitive `byte[]`/`char[]`.
- [ ] Consider using `getSingleResultOrNull()` where you previously used try/catch around `getSingleResult()`.
- [ ] Evaluate `PersistenceConfiguration` API as a replacement for `persistence.xml` in programmatic or test scenarios.

### Jakarta RESTful Web Services
- [ ] **If your JAX-RS endpoints produce or consume XML via JAXB:** Add an explicit JAXB dependency and register `MessageBodyReader`/`MessageBodyWriter` implementations for XML marshalling.
- [ ] Remove `@ManagedBean` from JAX-RS resource classes, providers, and `Application` subclasses.
- [ ] Complete migration from `@Context` to CDI `@Inject` (deprecated in EE 10, still functional in EE 11 but should be completed before it is removed).

### Jakarta Security
- [ ] Review injection points for built-in authentication mechanisms (`BasicAuthenticationMechanism`, `FormAuthenticationMechanism`, etc.) — they now require explicit qualifiers. Add the appropriate qualifier annotation if needed.
- [ ] Remove any `SecurityManager` references from `IdentityStore` or `AuthenticationMechanism` implementations.
- [ ] Consider the new multiple-mechanism support if your application needs to support multiple authentication methods.

### Jakarta Servlet
- [ ] Remove any `SecurityManager`/`AccessController` patterns in servlets, filters, or listeners.
- [ ] Evaluate using the new `sendRedirect()` overloads for 307/308 redirects.
- [ ] Evaluate `ByteBuffer` support for high-performance NIO I/O in servlet streams.

### Jakarta Expression Language
- [ ] **Remove `getFeatureDescriptors()` overrides from all custom `ELResolver` implementations.** This method has been removed from the interface.
- [ ] Note that `RecordELResolver` is now active by default — test EL expressions that may now resolve record component accessor methods differently.
- [ ] If needed, enable `OptionalELResolver` for transparent `Optional` unwrapping in EL.

### Jakarta Pages / JSP
- [ ] Remove any `isThreadSafe` page directives.
- [ ] Remove any `<jsp:plugin>`, `<jsp:params>`, `<jsp:fallback>` elements.
- [ ] Replace `JspException.getRootCause()` calls with `getCause()`.

### Jakarta Faces
- [ ] Deprecation notice: begin planning migration away from Full State Saving (`jakarta.faces.STATE_SAVING_METHOD=server`).
- [ ] Remove uses of the now-deprecated `PreDestroyCustomScopeEvent` and `PostConstructCustomScopeEvent`.
- [ ] Remove uses of the now-deprecated `composite:extension` tag.
- [ ] Remove any `SecurityManager` patterns in Faces phase listeners or lifecycle code.

### Jakarta Concurrency
- [ ] If targeting Java 21+ environments, evaluate configuring `ManagedExecutorService` with virtual threads for improved scalability.
- [ ] If you use EJB-based scheduling (`@Schedule`), evaluate migration to Concurrency 3.1 scheduled operations.

### Jakarta Data (New)
- [ ] Evaluate Jakarta Data 1.0 for new data access layers — it provides a standardized repository pattern integrating with JPA and NoSQL.
- [ ] Jakarta Data requires CDI and either Jakarta Persistence or a compatible NoSQL provider. Ensure these are available in your runtime.

### Maven Coordinates Reference

Key artifact coordinate changes for EE 11:

| Specification | EE 10 Artifact | EE 11 Artifact |
|---|---|---|
| Annotations | `jakarta.annotation:jakarta.annotation-api:2.1.x` | `jakarta.annotation:jakarta.annotation-api:3.0.x` |
| Authentication | `jakarta.authentication:jakarta.authentication-api:3.0.x` | `jakarta.authentication:jakarta.authentication-api:3.1.x` |
| Authorization | `jakarta.authorization:jakarta.authorization-api:2.1.x` | `jakarta.authorization:jakarta.authorization-api:3.0.x` |
| Bean Validation | `jakarta.validation:jakarta.validation-api:3.0.x` | `jakarta.validation:jakarta.validation-api:3.1.x` |
| CDI | `jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.x` | `jakarta.enterprise:jakarta.enterprise.cdi-api:4.1.x` |
| Concurrency | `jakarta.enterprise.concurrent:jakarta.enterprise.concurrent-api:3.0.x` | `jakarta.enterprise.concurrent:jakarta.enterprise.concurrent-api:3.1.x` |
| Data (NEW) | N/A | `jakarta.data:jakarta.data-api:1.0.x` |
| EL | `jakarta.el:jakarta.el-api:5.0.x` | `jakarta.el:jakarta.el-api:6.0.x` |
| Faces | `jakarta.faces:jakarta.faces-api:4.0.x` | `jakarta.faces:jakarta.faces-api:4.1.x` |
| Interceptors | `jakarta.interceptor:jakarta.interceptor-api:2.1.x` | `jakarta.interceptor:jakarta.interceptor-api:2.2.x` |
| Pages | `jakarta.servlet.jsp:jakarta.servlet.jsp-api:3.1.x` | `jakarta.servlet.jsp:jakarta.servlet.jsp-api:4.0.x` |
| Persistence | `jakarta.persistence:jakarta.persistence-api:3.1.x` | `jakarta.persistence:jakarta.persistence-api:3.2.x` |
| RESTful WS | `jakarta.ws.rs:jakarta.ws.rs-api:3.1.x` | `jakarta.ws.rs:jakarta.ws.rs-api:4.0.x` |
| Security | `jakarta.security.enterprise:jakarta.security.enterprise-api:3.0.x` | `jakarta.security.enterprise:jakarta.security.enterprise-api:4.0.x` |
| Servlet | `jakarta.servlet:jakarta.servlet-api:6.0.x` | `jakarta.servlet:jakarta.servlet-api:6.1.x` |
| WebSocket | `jakarta.websocket:jakarta.websocket-api:2.1.x` | `jakarta.websocket:jakarta.websocket-api:2.2.x` |
| XML Binding | `jakarta.xml.bind:jakarta.xml.bind-api:4.0.x` (container-provided) | `jakarta.xml.bind:jakarta.xml.bind-api:4.0.x` (**must declare explicitly**) |
| XML Web Services | `jakarta.xml.ws:jakarta.xml.ws-api:4.0.x` (container-provided) | `jakarta.xml.ws:jakarta.xml.ws-api:4.0.x` (**must declare explicitly**) |

### Specifications with No Changes in EE 11

The following specifications carry over unchanged from Jakarta EE 10:

| Specification | Version | Notes |
|---|---|---|
| Jakarta Activation | 2.1 | Maintenance — no changes |
| Jakarta Batch | 2.1 | Unchanged |
| Jakarta Enterprise Beans | 4.0 | Unchanged |
| Jakarta JSON Binding | 3.0 | Maintenance — no changes |
| Jakarta JSON Processing | 2.1 | Maintenance — no changes |
| Jakarta Mail | 2.1 | Maintenance — no changes |
| Jakarta Messaging | 3.1 | Unchanged |
| Jakarta Standard Tag Library | 3.0 | Maintenance — no changes |
| Jakarta Transactions | 2.0 | Unchanged |

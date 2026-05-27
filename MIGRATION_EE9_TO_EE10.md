# Jakarta EE 9 to EE 10 Migration Guide

## Overview

Jakarta EE 10, released in 2022, is the first feature release of the Jakarta EE platform following the major namespace migration completed in EE 9. EE 10 raises the minimum Java SE requirement to Java 11 and delivers substantive new features across the platform. Key highlights include CDI 4.0 splitting into Lite and Full profiles to support constrained environments, Faces 4.0 completing a major cleanup by removing legacy JSP integration and deprecated binding APIs, Concurrency 3.0 introducing asynchronous methods and context-aware completable futures, Security 3.0 adding OpenID Connect support, RESTful Web Services 3.1 adding a Java SE bootstrap API and multipart support, and Persistence 3.1 extending the JPQL and Criteria APIs. Jakarta EE 10 also formally establishes the Core Profile, a lightweight subset suitable for microservices, alongside the existing Web Profile and Full Platform.

---

## Platform Changes

### Java SE Minimum Version
- **Jakarta EE 9 required Java SE 8 or higher** (some specs bumped to 11 in EE 9)
- **Jakarta EE 10 requires Java SE 11 or higher** across all specifications

### Profiles Added
- **Core Profile** is introduced in Jakarta EE 10 as a new lightweight profile suitable for microservices and cloud-native applications. It includes CDI Lite, JSON-P, JSON-B, RESTful Web Services, and Annotations.

### Specifications Added
No new top-level specifications were added to the Full Platform in EE 10.

### Specifications Removed
No specifications were pruned from the platform in EE 10 (XML Binding and XML Web Services remain optional in the Full Platform).

---

## Specification Changes

### 1. Jakarta Activation (2.0 → 2.1)

**New Features:**
- Provided a standalone API jar that is fully independent of any particular implementation. Previously the API jar had an implicit coupling to the reference implementation.

**Breaking Changes / Removals / Deprecations:**
- None

---

### 2. Jakarta Annotations (2.0 → 2.1)

**New Features:**
- `@Priority` can now be used everywhere, not just on types. This broadens the contexts in which prioritization annotations can be applied.
- Added `@Nullable` and `@Nonnull` annotations for documenting nullability contracts in APIs.

**Breaking Changes / Removals / Deprecations:**
- None

---

### 3. Jakarta Authentication / JASPIC (2.0 → 3.0)

**New Features:**
- Generics have been added throughout the API for improved type safety.
- New methods for adding and removing a single server auth module (previously only bulk operations existed).
- Added a key for `isAuthenticationRequest` to the server container profile.
- Added default methods to `ServerAuth` and `ClientAuth` interfaces to ease implementation.
- Added constructor variants taking a `cause` parameter to `AuthException` for better exception chaining.
- Clarified the interaction of the Servlet profile with other specifications.
- Clarified `PasswordValidationCallback` semantics.
- Clarified state expectations of the callback handler for per-request state.

**Breaking Changes / Removals / Deprecations:**
- `SecurityManager` usage is deprecated in light of JDK 17 / JEP 411. Code relying on security manager checks within the Authentication SPI should be reviewed.

---

### 4. Jakarta Authorization / JACC (2.0 → 2.1)

**New Features:**
- Added `getPolicyConfiguration` methods that do not require a particular state, making it easier to query the policy configuration without entering a specific lifecycle phase.
- Added methods to `PolicyConfiguration` to read (introspect) the set of granted permissions.
- `getContext()` now returns a generic type, removing unchecked cast requirements.

**Breaking Changes / Removals / Deprecations:**
- None

---

### 5. Jakarta Batch (2.0 → 2.1)

**New Features:**
- Formally defines Jakarta Batch integration with CDI (Contexts and Dependency Injection) both within and outside the Jakarta EE Platform.
- CDI integration, which was previously optional from the Batch specification's perspective, is now **required**. Applications running in a Jakarta EE container that use CDI-based injection into batch artifacts can now rely on this behavior across all compliant implementations.

**Breaking Changes / Removals / Deprecations:**
- None

---

### 6. Jakarta Bean Validation (3.0 → 3.0, no version change)

**Note:** Jakarta Bean Validation remained at version 3.0 for Jakarta EE 10. The 3.0 release (targeting EE 9) primarily moved the specification to the `jakarta.*` namespace.

**New Features (3.0):**
- Moved to the `jakarta.*` namespace (completed in EE 9 / BV 3.0).

**Breaking Changes / Removals / Deprecations:**
- None

**Migration Note:** No changes needed when upgrading from EE 9 to EE 10 for Bean Validation. The artifact coordinates remain `jakarta.validation:jakarta.validation-api:3.0.x`.

---

### 7. Jakarta Contexts and Dependency Injection / CDI (3.0 → 4.0)

**New Features:**
- **CDI Lite and CDI Full split:** CDI 4.0 divides the specification into CDI Lite (a subset designed for restricted/ahead-of-time compilation environments such as GraalVM native image) and CDI Full (all existing CDI features). Applications targeting the Core Profile use CDI Lite only.
- A new `jakarta.enterprise:jakarta.enterprise.lang-model` API artifact has been added supporting the Build Compatible (Reflection-Free) Extensions SPI.
- JPMS `module-info.class` files have been added to the CDI API artifacts.

**Breaking Changes / Removals / Deprecations:**
- The `bean-discovery-mode` attribute in `beans.xml` now **defaults to `annotated`** (previously defaulted to `all` when a `beans.xml` was present). An empty `beans.xml` is also treated as `annotated`. **This is a significant behavioral change.** Applications that relied on all classes being discovered automatically must either explicitly set `bean-discovery-mode="all"` in their `beans.xml` or annotate beans appropriately.

**Action Required:** Review your `beans.xml` files. If you depended on `bean-discovery-mode="all"` (the previous implicit default), you must now set it explicitly.

---

### 8. Jakarta Concurrency (2.0 → 3.0)

**New Features:**
- **Asynchronous methods** — methods on managed beans can be annotated to execute asynchronously on a managed executor.
- **Context-aware completion stages and completable futures** — `ManagedCompletableFuture` propagates Jakarta EE context (transaction, security, etc.) to completion stage callbacks.
- **Context propagation to parallel stream operations** — thread context can now be propagated into parallel streams.
- **Modernized Trigger mechanism** with time zone support for scheduling.
- **Propagation of third-party context types** via Thread Context Providers SPI.
- **Resource definition annotations** (e.g., `@ManagedExecutorDefinition`, `@ManagedScheduledExecutorDefinition`, `@ContextServiceDefinition`) and corresponding deployment descriptor elements for declarative configuration.

**Breaking Changes / Removals / Deprecations:**
- None

---

### 9. Jakarta Connectors (2.0 → 2.1)

**New Features:**
- Bug fixes and documentation clean-up.
- Updated to meet the Java 11 version requirements of Jakarta EE 10.

**Breaking Changes / Removals / Deprecations:**
- None

---

### 10. Jakarta Enterprise Beans / EJB (4.0 → 4.0, no version change)

**Note:** Jakarta Enterprise Beans remained at version 4.0 for Jakarta EE 10. EJB 4.0 was released for EE 9 and carried the following changes (completed in EE 9):

**Changes in EJB 4.0 (EE 9):**
- Moved to the `jakarta.*` namespace.
- Removed methods relying on `java.security.Identity`.
- Removed methods relying on JAX-RPC.
- Removed deprecated `EJBContext.getEnvironment()` method.
- Removed support for Distributed Interoperability.
- Marked the optional EJB 2.x API Group.

**Migration Note:** No additional changes between EE 9 and EE 10 for Enterprise Beans. The artifact coordinates remain `jakarta.ejb:jakarta.ejb-api:4.0.x`.

---

### 11. Jakarta Expression Language (4.0 → 5.0)

**New Features:**
- `BeanELResolver` now considers default method implementations when resolving property getters, setters, and methods.
- Support added for coercing a `LambdaExpression` instance to a functional interface method invocation.
- Support added for coercing arrays.
- New `MethodReference` class provides access to details (including annotations) of the method to which a `MethodExpression` resolves.
- Generics are now used throughout the API.
- Clarified behaviour for matching expressions to methods based on parameter types.
- Specifications that depend on EL may now define additional packages imported by default into the EL environment.

**Breaking Changes / Removals / Deprecations:**
- The deprecated, misspelled method `MethodExpression.isParmetersProvided()` has been **removed**. Use `isParametersProvided()`.
- `ELResolver.getFeatureDescriptors()` has been **deprecated** (removal planned for EL 6.0) to remove the dependency on the `java.desktop` module.
- A default `null`-returning implementation of `getFeatureDescriptors()` has been added, so existing custom `ELResolver` implementations do not need to implement the deprecated method.
- `ELResolver.getType()` must return `null` if the ELResolver or the resolved property is read-only. This changes the documented behavior of `StaticFieldELResolver`.

---

### 12. Jakarta Faces (3.0 → 4.0)

**New Features:**
- New API to programmatically create Facelets.
- New automatic extensionless URL mapping (no longer need `.xhtml` suffix in URLs).
- New `@ClientWindowScoped` annotation for client-window-scoped beans.
- Support for custom cookie attributes such as `SameSite` in `ExternalContext#addResponseCookie()`.
- New `multiple` and `accept` attributes on `<h:inputFile>` for multi-file uploads and MIME type filtering.
- New `FacesContext#getLifecycle()` method.
- New `<f:selectItemGroups>` and `<f:selectItemGroup>` tags for grouped select items.
- New `type` attribute on `<h:inputText>` for HTML5 input types.
- New `UIViewRoot#getDoctype()` method.
- New `onerror` attribute on `<f:websocket>`.
- New `layout="list"` for `<h:selectManyCheckbox>` and `<h:selectOneRadio>`.
- New annotation literals for all `@Qualifier` annotations.
- `UIComponent#subscribeToEvent()` is made more convenient.
- `<link>` and `<script>` tags now skip the `type` attribute when the doctype is HTML5.
- Improved `<f:ajax>` behavior in composite components.

**Breaking Changes / Removals / Deprecations:**
- All occurrences of "JSF" in the specification are renamed to "Faces" — the artifact ID changed from `jakarta.faces:jakarta.faces-api` (same coordinates, but internal naming cleaned up).
- Namespace URIs renamed: all `http://xmlns.jcp.org/jsf/*` URIs are replaced with `jakarta.faces.*` URNs. **Update all Facelets tag library declarations.**
- **All JSP support has been removed.** Facelets is the only supported view technology.
- **Native Managed Beans removed:** `@ManagedBean` and all related annotations (`@ManagedProperty`, `@ManagedBeanScoped`, etc.) have been removed. Migrate to CDI beans.
- **`MethodBinding` and `ValueBinding` removed** along with related API. These were legacy APIs from JSF 1.x.
- `UIComponent.CURRENT_COMPONENT` and `UIComponent.CURRENT_COMPOSITE_COMPONENT` constants removed.
- Deprecated methods of the `StateManager` class removed.
- The entire `ResourceResolver` class removed. Use `ResourceHandler` instead.

**Action Required:** This is the most breaking change in EE 10. Applications must:
1. Migrate from `@ManagedBean` to CDI `@Named` beans.
2. Update Facelets namespace declarations from `http://xmlns.jcp.org/jsf/*` to `jakarta.faces.*`.
3. Remove any JSP-based views.
4. Replace any use of `MethodBinding`/`ValueBinding` APIs.

---

### 13. Jakarta Interceptors (2.0 → 2.1)

**New Features:**
- Updated dependencies for Jakarta EE 10.
- Added JPMS `module-info` descriptor.

**Breaking Changes / Removals / Deprecations:**
- None

---

### 14. Jakarta JSON Binding / JSON-B (2.0 → 3.0)

**New Features:**
- Deserialization of JSON `null` now correctly produces `JsonValue.NULL_VALUE` instead of a Java `null`.
- `@JsonbTypeDeserializer` and `@JsonbTypeAdapter` can now be used as parameter/type annotations (not just class-level annotations).
- Support for handling polymorphic types via `@JsonbTypeInfo` and `@JsonbSubtype`.
- `@JsonbProperty.nillable()` deprecated in this release.

**Breaking Changes / Removals / Deprecations:**
- `@JsonbCreator` parameters now support being optional (a behavioral clarification/change).
- `@JsonbProperty.nillable()` is deprecated.

---

### 15. Jakarta JSON Processing / JSON-P (2.0 → 2.1)

**New Features:**
- Added API to create `JsonValue` instances from Java primitive types.
- Added API to create/get `JsonValue` from `java.lang.Number`.
- Added API to get the current event from `JsonParser`.
- Added standard property to handle duplicate keys in JSON objects.
- Clarified behaviour of `JsonObjectBuilder.build()`.
- Clarified behaviour of `JsonGenerator.close()`.
- Changed type bounds of a `Map` argument in `Json.createObjectBuilder(Map)`.
- Added definition of exceptions thrown by `JsonParser.getValue()`, `getObject()`, and `getArray()` methods.
- Removed default implementation from the specification API artifact; implementation moved to a separate project (standalone API jar).
- Provided standalone TCK based on Apache Maven.

**Breaking Changes / Removals / Deprecations:**
- None

---

### 16. Jakarta Mail (2.0 → 2.1)

**New Features:**
- The API is now fully standalone from what was previously the reference implementation (Eclipse Angus). A clean separation between API and implementation is enforced.

**Breaking Changes / Removals / Deprecations:**
- None

---

### 17. Jakarta Messaging / JMS (3.0 → 3.1)

**New Features:**
- `@JMSConnectionFactoryDefinition` is now a repeatable annotation.
- `@JMSDestinationDefinition` is now a repeatable annotation.
- Minor build-tooling updates.

**Breaking Changes / Removals / Deprecations:**
- None

---

### 18. Jakarta Pages / JSP (3.0 → 3.1)

**New Features:**
- Added an option to raise a `PropertyNotFoundException` when an EL expression contains an unknown identifier (aligns with EL 5.0 changes).
- Clarified the meaning of `scope` in the context of scripting variables associated with custom actions.
- Clarified EL default imports within a JSP page to be consistent with the scripting environment. Refactored `ScopedAttributeELResolver` to remove special handling for imports and unresolved variables.

**Breaking Changes / Removals / Deprecations:**
- Deprecated methods that override `ELResolver.getFeatureDescriptors()` since that method has been deprecated as of EL 5.0.
- **Deprecated `isThreadSafe` page directive attribute** because the `SingleThreadModel` Servlet API interface has been removed in Servlet 6.0.
- **Deprecated `jsp:plugin` action** and related actions (`jsp:params`, `jsp:fallback`) as the associated HTML `<applet>` and `<object>` elements are no longer supported by major browsers.

---

### 19. Jakarta Persistence (3.0 → 3.1)

**New Features:**
- `EntityManagerFactory` and `EntityManager` interfaces now extend `java.lang.AutoCloseable`, enabling use in try-with-resources blocks.
- Fixed `ClassTransformer.transform` to throw a Persistence API-specific exception.
- Added support for `java.util.UUID` as a primary key type and `GenerationType.UUID` for auto-generation.
- Added numeric functions to JPQL: `CEILING`, `EXP`, `FLOOR`, `LN`, `POWER`, `ROUND`, `SIGN`.
- Added corresponding Criteria API methods: `ceiling()`, `exp()`, `floor()`, `ln()`, `power()`, `round()`, `sign()`.
- Added date/time functions to JPQL: `LOCAL DATE`, `LOCAL DATETIME`, `LOCAL TIME`.
- Added corresponding Criteria API methods: `localDate()`, `localDateTime()`, `localTime()`.
- Added `EXTRACT` function to JPQL.
- Added support for `Expression` instances as conditions in Criteria CASE expressions.
- Added missing definition of `single_valued_embeddable_object_field` in JPQL BNF.
- Clarified mixing of positional and named query input parameters.
- Clarified the definition of the Basic type.
- Clarified the order of parameters in the `LOCATE` function.
- Clarified `SqlResultSetMapping` with multiple `EntityResult` entries and conflicting aliases.

**Breaking Changes / Removals / Deprecations:**
- None

---

### 20. Jakarta RESTful Web Services / JAX-RS (3.0 → 3.1)

**New Features:**
- **Java SE Bootstrap API** — applications can now start a JAX-RS application in Java SE without an application server (`SeBootstrap.start()`).
- **Multipart media type support** — new `EntityPart` API for sending and receiving `multipart/form-data` requests.
- Better alignment with JSON-B for automatic entity provider selection.
- Automatic loading of provider extensions via `ServiceLoader`.

**Breaking Changes / Removals / Deprecations:**
- `@Context` injection annotation is **deprecated** in preparation for alignment with CDI injection. Replace `@Context` injections with `@Inject` where CDI equivalents exist.

**Action Required:** Plan to migrate from `@Context` to CDI `@Inject`. While `@Context` continues to work in EE 10, it will be removed in a future version.

---

### 21. Jakarta Security (2.0 → 3.0)

**New Features:**
- **OpenID Connect authentication mechanism** — built-in support for OIDC-based authentication via `@OpenIdAuthenticationMechanismDefinition`.

**Breaking Changes / Removals / Deprecations:**
- None

---

### 22. Jakarta Servlet (5.0 → 6.0)

**New Features:**
- Clarified URI path decoding and normalization.
- Updated `Cookie` class and related classes to align with RFC 6265 (replaces RFC 2109). Cookie handling behavior may differ subtly.
- Clarified `getRealPath(String)` behavior.
- Added generic attribute support to cookies (including session cookies) for attributes such as `SameSite`.
- Added JPMS `module-info.java` support.
- Removed the recommendation that containers include an `X-Powered-By` header.
- Added new methods to obtain unique identifiers for the current request and/or associated connection (`getRequestId()`, `getProtocolRequestId()`, `getServletConnection()`).
- Relaxed requirements in "Wrapping Requests and Responses" for `RequestDispatcher`.
- Removed the restriction on programmatically added listeners calling some `ServletContext` getter methods.

**Breaking Changes / Removals / Deprecations:**
- `doHead()` wrapped-response handling deprecated in favor of container-provided HEAD method behavior.
- **Removed API classes and methods deprecated in Servlet 5.0 and earlier**, including:
  - `SingleThreadModel` interface (removed)
  - `HttpSessionContext` interface (removed)
  - `HttpUtils` class (removed)
  - Various other deprecated methods throughout the API

**Action Required:** Remove any use of `SingleThreadModel`, `HttpSessionContext`, and `HttpUtils`. Check for other removed deprecated Servlet API methods.

---

### 23. Jakarta Standard Tag Library / JSTL (2.0 → 3.0)

**New Features:**
- Tag library URIs renamed to `jakarta.tags.*` namespace (e.g., `jakarta.tags.core`, `jakarta.tags.fmt`, etc.).
- Updated to Java 11.
- Added JPMS Module Info class.
- Removed XML Binding dependency from the API POM.
- Various tag documentation and specification updates.

**Breaking Changes / Removals / Deprecations:**
- Old `java.sun.com` taglib URIs (`http://java.sun.com/jsp/jstl/core`, etc.) have been renamed to new `jakarta.tags.*` URNs.
- The old `java.sun.com` URIs are still accepted for backward compatibility.

**Action Required:** Update taglib declarations in JSP pages from `http://java.sun.com/jsp/jstl/core` (and similar) to `jakarta.tags.core` (and similar). The old URIs still work but should be updated for forward compatibility.

---

### 24. Jakarta Transactions (2.0 → 2.0, no version change)

**Note:** Jakarta Transactions remained at version 2.0 for Jakarta EE 10. No changes were made.

**Migration Note:** No migration needed. Artifact coordinates remain `jakarta.transaction:jakarta.transaction-api:2.0.x`.

---

### 25. Jakarta WebSocket (2.0 → 2.1)

**New Features:**
- Clarified that once a `MessageHandler` is selected for a message, it is used for the entirety of that message regardless of subsequent changes to the session's message handler configuration.
- Added a getter for the default platform configurator.
- Added an API for client-side TLS configuration.
- Removed the restriction that, in a Jakarta web container environment, WebSocket endpoints can only be registered during deployment. Added `ServerContainer.upgradeHttpToWebSocket()` enabling programmatic dispatch to a WebSocket endpoint.
- Clarified expected behavior for `Session.getRequestURI()` (full URI should be returned).
- Clarified expected handling of user properties.
- Clarified that a zero or negative value disables the session idle timeout.
- Split the `jakarta.websocket-api` jar: `jakarta.websocket.*` classes are now in a separate `jakarta.websocket-client-api` jar dependency.
- Added JPMS module descriptors defining `jakarta.websocket.client` (client module) and `jakarta.websocket` (server module).

**Breaking Changes / Removals / Deprecations:**
- None

---

### 26. Jakarta XML Binding / JAXB (3.0 → 4.0)

**New Features:**
- Added implementation lookup through the `properties` Map passed to `JAXBContext.newInstance(...)` methods.
- Implementation of `DatatypeConverterInterface` now throws an exception on invalid input.
- Added `https` scheme to the list of schemes to remove when mapping namespace URIs to Java packages.
- Fixed cross-references in the specification document.

**Breaking Changes / Removals / Deprecations:**
- Dropped compatibility with JAXB 1.0.
- Removed constraints on using `java.desktop/java.beans.Introspector`.
- Removed deprecated `jakarta.xml.bind.Validator` class.
- Removed deprecated `jakarta.xml.bind.context.factory` property.
- **Dropped implementation lookup through `META-INF/services/jakarta.xml.bind.JAXBContext`** — the standard `ServiceLoader` mechanism is no longer used for JAXB provider discovery.
- **Dropped implementation lookup through `jaxb.properties` file** — this traditional discovery mechanism is gone.

**Action Required:** If you relied on `jaxb.properties` or `META-INF/services/jakarta.xml.bind.JAXBContext` for provider discovery, update to the new system-property-based or programmatic lookup approach. Remove any remaining use of `jakarta.xml.bind.Validator`.

---

### 27. Jakarta XML Web Services / JAX-WS (3.0 → 4.0)

**New Features:**
- Annotated `jakarta.xml.ws.AsyncHandler` with `@FunctionalInterface`.
- Extended `jakarta.xml.ws.wsaddressing.W3CEndpointReference` API with getter methods.
- Changed implementation lookup order: system property check is now the first step.
- Folded Jakarta Web Services Metadata (formerly a standalone specification) into this specification. The separate `jakarta.ws.metadata` artifact is no longer available.

**Breaking Changes / Removals / Deprecations:**
- **Dropped implementation lookup through `jaxws.properties` file.**
- Removed the required fallback to a default implementation in provider lookup.

**Action Required:** Review provider discovery configuration if you used `jaxws.properties`. Update dependency declarations to remove any direct reference to the now-folded Jakarta Web Services Metadata artifact.

---

## Migration Checklist

Use the following checklist when migrating an application from Jakarta EE 9 to Jakarta EE 10.

### Java SE
- [ ] Verify the application compiles and runs on Java SE 11 or higher.
- [ ] Review any use of `SecurityManager` — it is deprecated at the JVM level (JEP 411) and several Jakarta specs have begun deprecating related APIs.

### CDI
- [ ] **Critical:** Audit all `beans.xml` files. If `bean-discovery-mode` is not set, the behavior has changed from `all` to `annotated`. Add `bean-discovery-mode="all"` if your application depends on automatic discovery of unannotated beans.
- [ ] If using CDI extensions, review whether your extension uses Build Compatible (Reflection-Free) Extensions or Portable Extensions to determine Core Profile compatibility.

### Jakarta Faces
- [ ] **Critical:** Migrate `@ManagedBean` and related annotations to CDI `@Named` + appropriate scope annotations.
- [ ] **Critical:** Update all Facelets tag namespace declarations from `http://xmlns.jcp.org/jsf/*` to `jakarta.faces.*`.
- [ ] Remove any JSP-based views; migrate to Facelets.
- [ ] Remove use of `MethodBinding`, `ValueBinding` and related legacy binding classes.
- [ ] Remove use of `UIComponent.CURRENT_COMPONENT` constants.
- [ ] Remove use of the `ResourceResolver` class; use `ResourceHandler`.
- [ ] Remove deprecated `StateManager` methods.

### Jakarta Servlet
- [ ] Remove use of `SingleThreadModel` interface.
- [ ] Remove use of `HttpSessionContext` interface.
- [ ] Remove use of `HttpUtils` class.
- [ ] Review cookie handling code — RFC 6265 compliance may change behavior.

### Jakarta Persistence
- [ ] `EntityManagerFactory` and `EntityManager` now implement `AutoCloseable` — consider using try-with-resources for explicit resource management where appropriate.
- [ ] Update JPQL queries that use numeric or date/time functions to take advantage of new built-ins.

### Jakarta RESTful Web Services
- [ ] Plan migration from `@Context` to CDI `@Inject` — `@Context` is deprecated.
- [ ] Consider using the new Java SE Bootstrap API (`SeBootstrap`) for standalone JAX-RS applications.
- [ ] Evaluate the new `EntityPart` API for multipart form handling.

### Jakarta Batch
- [ ] Ensure CDI is available and enabled in your runtime if you use CDI injection in batch artifacts — this is now required rather than optional.

### Jakarta Expression Language
- [ ] Remove any call to the now-deleted `MethodExpression.isParmetersProvided()` method; replace with `isParametersProvided()`.
- [ ] Review custom `ELResolver` implementations — `getFeatureDescriptors()` is deprecated and the type-safety change to `getType()` return value for read-only resolvers may affect behavior.

### Jakarta Standard Tag Library (JSTL)
- [ ] Update taglib URIs in JSP pages from `http://java.sun.com/jsp/jstl/*` to `jakarta.tags.*` URNs.

### Jakarta XML Binding (JAXB)
- [ ] Remove any `jaxb.properties` files used for provider discovery.
- [ ] Remove any `META-INF/services/jakarta.xml.bind.JAXBContext` files.
- [ ] Remove use of the deprecated `jakarta.xml.bind.Validator` class.
- [ ] Remove use of JAXB 1.0 APIs.

### Jakarta XML Web Services (JAX-WS)
- [ ] Remove any `jaxws.properties` files used for provider discovery.
- [ ] Update POM files to remove dependency on the now-folded Jakarta Web Services Metadata artifact.

### Jakarta Authentication (JASPIC)
- [ ] Review `SecurityManager` usage within custom `ServerAuthModule` implementations — it is now deprecated.

### Jakarta JSON Binding (JSON-B)
- [ ] Note the behavior change: JSON `null` now deserializes to `JsonValue.NULL_VALUE` rather than Java `null` in some contexts.
- [ ] Note that `@JsonbProperty.nillable()` is deprecated; plan to remove it.

### Maven Coordinates Reference

Key artifact coordinate changes for EE 10:

| Specification | EE 9 Artifact | EE 10 Artifact |
|---|---|---|
| CDI | `jakarta.enterprise:jakarta.enterprise.cdi-api:3.0.x` | `jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.x` |
| Faces | `jakarta.faces:jakarta.faces-api:3.0.x` | `jakarta.faces:jakarta.faces-api:4.0.x` |
| Persistence | `jakarta.persistence:jakarta.persistence-api:3.0.x` | `jakarta.persistence:jakarta.persistence-api:3.1.x` |
| Servlet | `jakarta.servlet:jakarta.servlet-api:5.0.x` | `jakarta.servlet:jakarta.servlet-api:6.0.x` |
| RESTful WS | `jakarta.ws.rs:jakarta.ws.rs-api:3.0.x` | `jakarta.ws.rs:jakarta.ws.rs-api:3.1.x` |
| EL | `jakarta.el:jakarta.el-api:4.0.x` | `jakarta.el:jakarta.el-api:5.0.x` |
| Security | `jakarta.security.enterprise:jakarta.security.enterprise-api:2.0.x` | `jakarta.security.enterprise:jakarta.security.enterprise-api:3.0.x` |
| Authentication | `jakarta.authentication:jakarta.authentication-api:2.0.x` | `jakarta.authentication:jakarta.authentication-api:3.0.x` |
| XML Binding | `jakarta.xml.bind:jakarta.xml.bind-api:3.0.x` | `jakarta.xml.bind:jakarta.xml.bind-api:4.0.x` |
| XML Web Services | `jakarta.xml.ws:jakarta.xml.ws-api:3.0.x` | `jakarta.xml.ws:jakarta.xml.ws-api:4.0.x` |
| Concurrency | `jakarta.enterprise.concurrent:jakarta.enterprise.concurrent-api:2.0.x` | `jakarta.enterprise.concurrent:jakarta.enterprise.concurrent-api:3.0.x` |
| JSON-B | `jakarta.json.bind:jakarta.json.bind-api:2.0.x` | `jakarta.json.bind:jakarta.json.bind-api:3.0.x` |
| JSON-P | `jakarta.json:jakarta.json-api:2.0.x` | `jakarta.json:jakarta.json-api:2.1.x` |

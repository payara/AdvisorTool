# Jakarta EE Specification Changes by Specification

This document summarizes the changes introduced in each version of each Jakarta EE specification. Versions listed as Jakarta EE 8 or the initial Jakarta re-release are typically re-releases of the corresponding JCP JSR under the Eclipse Foundation Specification License (EFSL) with no new features. Jakarta EE 9 versions are primarily namespace migrations from `javax.*` to `jakarta.*`. Substantive new features begin with Jakarta EE 10 and later.

---

## Jakarta Activation

### Version 1.2 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release of JSR 925 under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.1 (Jakarta EE 10)
**New features:**
- Provided standalone API jar fully independent from the particular implementation.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.2 (Jakarta EE 12, under development)
**New features:** None.
**Removals / Deprecations / Breaking Changes:**
- Remove all references to Security Manager.

---

## Jakarta Annotations

### Version 1.3 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.1 (Jakarta EE 10)
**New features:**
- `@Priority` annotation is now usable on any type, not just interceptors and alternatives.
- Added `@Nullable` and `@Nonnull` annotations.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 11)
**New features:** None listed.
**Removals / Deprecations / Breaking Changes:**
- Fully removed the deprecated `@ManagedBean` annotation.

### Version 3.1 (Jakarta EE 12, under development)
**New features:**
- Planned addition of `@RunOnVirtualThread` annotation.
**Removals / Deprecations / Breaking Changes:** None planned.

---

## Jakarta Authentication

### Version 1.1 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 10)
**New features:**
- Added generics throughout the API.
- Added methods to add/remove a single server auth module.
- Added a key to `MessageInfo` to indicate an authentication request.
- Added default methods for `ServerAuth` and `ClientAuth` interfaces.
- Added constructor variants with a cause parameter to `AuthException`.
- Various clarifications to the specification text.
**Removals / Deprecations / Breaking Changes:**
- Deprecated SecurityManager usage.

### Version 3.1 (Jakarta EE 11)
**New features:** None listed separately.
**Removals / Deprecations / Breaking Changes:**
- Removed all references to SecurityManager from the specification.

---

## Jakarta Authorization

### Version 1.5 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.1 (Jakarta EE 10)
**New features:**
- `getPolicyConfiguration()` method no longer requires being in a specific state.
- Added methods to read permissions from `PolicyConfiguration`.
- Generic return type for `getContext()`.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 11)
**New features:**
- Ability to register a policy provider programmatically per application.
- Standardized context ID for Servlet containers.
- Several convenience methods added to the API.
**Removals / Deprecations / Breaking Changes:**
- Designed replacement for the legacy `Policy` class.
- Removed all SecurityManager references from the specification.

---

## Jakarta Batch

### Version 1.0 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.1 (Jakarta EE 10)
**New features:**
- CDI integration for Jakarta Batch is now required rather than optional.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.2 (Jakarta EE 12, under development)
**New features:** None.
**Removals / Deprecations / Breaking Changes:**
- Remove SecurityManager references from the API jar.

---

## Jakarta Bean Validation

### Version 2.0 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 9)
**New features:**
- Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.1 (Jakarta EE 11)
**New features:**
- Clarified Java Records support.
- Updated dependencies for Jakarta EE 11 alignment.
**Removals / Deprecations / Breaking Changes:** None.

### Version 4.0 (Jakarta EE 12, under development)
**New features:**
- Deprecate certain `ConstraintViolationBuilder` methods.
- New `ConstraintValidator#initialize(ConstraintDescriptor)` overload.
- New `ConstraintDescriptor#getAttribute` method.
- Relaxed XSD schema for constraint definitions.
- `ConstraintValidator` implementations discoverable via the service loader.
- Dropped SecurityManager usage; Java SE 21 minimum.
- Various specification clarifications.
**Removals / Deprecations / Breaking Changes:**
- Remove `Optional` built-in value extractor requirement changes from 3.1.
- Changes to array element annotation semantics.
- Remove SecurityManager usage.

---

## Jakarta CDI (Contexts and Dependency Injection)

### Version 2.0 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 4.0 (Jakarta EE 10)
**New features:**
- Split CDI into CDI Lite and CDI Full subsets.
- New `lang-model` API artifact for portable extensions.
- JPMS `module-info` descriptors added.
**Removals / Deprecations / Breaking Changes:**
- Default `bean-discovery-mode` changed from `all` to `annotated`.

### Version 4.1 (Jakarta EE 11)
**New features:**
- Breaking up the specification and TCK into modular pieces.
- Method invokers API.
- Executable methods via `InvokerBuilder`.
- `@Priority` now applicable to producer methods and fields.
- Various additional language model and reflection improvements.
**Removals / Deprecations / Breaking Changes:**
- Deprecated EL SPI methods on `BeanManager`.

### Version 5.0 (Jakarta EE 12, under development)
**New features:**
- `@Reserve` annotation for reserving bean names.
- `@Eager` annotation for eager initialization.
- `@AutoClose` for auto-closeable scoped beans.
- Async method invokers.
- Synthetic bean injection points.
- Global/application scopes for CDI SE.
- `BuildCompatibleExtension` usable programmatically.
- Parameterized types in `@Registration` observer methods.
- Various CDI Lite enhancements.
**Removals / Deprecations / Breaking Changes:**
- Maven GAV changed to `jakarta.cdi:jakarta.cdi-api`.
- Removed SecurityManager usage.
- Deprecated `BeanManager` methods (EL SPI) now removed.
- `trim` in non-explicit bean archives forbidden.
- `SyntheticBeanCreator` and `SyntheticBeanDisposer` deprecated.

---

## Jakarta Concurrency

### Version 1.1 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 10)
**New features:**
- Asynchronous methods via `@Asynchronous`.
- Context-aware `CompletionStage` and `CompletableFuture`.
- Parallel streams with context propagation.
- Modernized `Trigger` interface with time zone support.
- Third-party context propagation SPI.
- Resource definition annotations (`@ManagedExecutorDefinition`, `@ManagedThreadFactoryDefinition`, `@ContextServiceDefinition`).
**Removals / Deprecations / Breaking Changes:** None listed.

### Version 3.1 (Jakarta EE 11)
**New features:**
- Virtual threads integration (run tasks on virtual threads).
- Java Flow / Reactive Streams integration.
- Replacement for EJB `@Schedule` and timer service features.
- CDI-centric improvements for contextual concurrency.
**Removals / Deprecations / Breaking Changes:** None listed.

### Version 3.2 (Jakarta EE 12, under development)
**New features:**
- `@Lock` annotation for concurrency control.
- `@MaxConcurrency` annotation.
- `@Schedule` annotation (migrated from EJB).
- Annotation literals.
- Specification and TCK bug fixes.
**Removals / Deprecations / Breaking Changes:** None listed.

---

## Jakarta Connectors (JCA)

### Version 1.7 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.1 (Jakarta EE 10)
**New features:**
- Bug fixes and documentation cleanup.
- Java version update.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 12, under development)
**New features:**
- Support for aborting connections with an executor service.
- Connector Permissions feature.
- Various specification text fixes.
**Removals / Deprecations / Breaking Changes:**
- Remove `@SecurityPermission` annotation and corresponding `security-permission` deployment descriptor element.

---

## Jakarta Data

### Version 1.0 (Jakarta EE 11)
**New features:** (First release)
- Repository pattern with `@Repository` annotation and repository interfaces.
- Built-in pagination support.
- `StaticMetamodel` for type-safe queries.
- Integration with Jakarta Persistence, NoSQL, Transactions, and Bean Validation.
- CDI integration for repository injection.
**Removals / Deprecations / Breaking Changes:** None — first release.

### Version 1.1 (Jakarta EE 12, under development)
**New features:**
- Fluent Query Construction API via the static metamodel.
- Projection using Java Records.
- Stateful repository operations.
**Removals / Deprecations / Breaking Changes:**
- Possible deprecation of the `impl` package.

---

## Jakarta Enterprise Beans (EJB)

### Version 3.2 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 4.0 (Jakarta EE 9)
**New features:**
- Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:**
- Removed `java.security.Identity`-based methods.
- Removed JAX-RPC-related methods.
- Removed `EJBContext.getEnvironment()`.
- Removed Distributed Interoperability support.
- Marked the EJB 2.x API Group as optional.

### Version 4.1 (Jakarta EE 12, under development)
**New features:** None listed.
**Removals / Deprecations / Breaking Changes:**
- Remove SecurityManager language from the specification.

---

## Jakarta Expression Language

### Version 3.0 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 4.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 5.0 (Jakarta EE 10)
**New features:**
- `BeanELResolver` now considers default interface methods.
- `LambdaExpression` can be coerced to a functional interface.
- Array coercion support.
- New `MethodReference` class.
- Generics added throughout the API.
- Clarified method matching rules.
- EL packages added.
**Removals / Deprecations / Breaking Changes:**
- Removed misspelled `isParmetersProvided()` method.
- Deprecated `getFeatureDescriptors()` (default implementation returning null added).
- `ELResolver.getType()` must now return null for read-only resolvers.

### Version 6.0 (Jakarta EE 11)
**New features:**
- `java.desktop` module no longer required at runtime.
- New `length` property for arrays.
- `RecordELResolver` enabled by default.
- `OptionalELResolver` added (disabled by default).
**Removals / Deprecations / Breaking Changes:**
- All EL 5.0 deprecated code removed, including `getFeatureDescriptors()`.
- SecurityManager references removed.

### Version 6.1 (Jakarta EE 12, under development)
**New features:**
- Performance optimization for scoped attributes (coordinated with Jakarta Pages 4.1).
- Pluggable caching for EL expressions.
- Elvis operator (`?:`).
- `+` operator for merging collections.
**Removals / Deprecations / Breaking Changes:** None planned.

---

## Jakarta Faces

### Version 2.3 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 4.0 (Jakarta EE 10)
**New features:**
- Programmatic Facelets creation API.
- Extensionless URL mapping for Faces Servlet.
- `@ClientWindowScoped` CDI scope.
- `SameSite` cookie attribute support.
- `h:inputFile` `multiple` and `accept` attributes.
- `FacesContext#getLifecycle()`.
- `f:selectItemGroups` component.
- `h:inputText` `type` attribute passthrough.
- `f:selectItemGroup` component.
- `UIViewRoot#getDoctype()`.
- `f:websocket` `onerror` attribute.
- `layout="list"` for `h:dataTable`.
- Annotation literals for built-in CDI annotations.
- `subscribeToEvent` convenience method.
- `link`/`script` type defaults for HTML5.
- `f:ajax` usable inside composite components.
**Removals / Deprecations / Breaking Changes:**
- Renamed "JSF" to "Faces" throughout.
- Renamed XML namespace URIs to URN format.
- Removed JSP support.
- Removed `@ManagedBean` and related APIs.
- Removed `MethodBinding` and `ValueBinding`.
- Removed `CURRENT_COMPONENT` constants.
- Removed deprecated `StateManager` methods.
- Removed `ResourceResolver` class.

### Version 4.1 (Jakarta EE 11)
**New features:**
- Generics for `FacesMessage`.
- Events fired for built-in CDI scope lifecycle (e.g., `@SessionScoped`).
- `@Inject` for current flow.
- `UUIDConverter` built-in converter.
- `ExternalContext.setResponseContentLengthLong()`.
- `rowStatePreserved` for `UIRepeat`.
- Specified `FACELETS_REFRESH_PERIOD` behavior.
- `FacesMessage` `equals`, `hashCode`, and `toString`.
**Removals / Deprecations / Breaking Changes:**
- Deprecated full state saving.
- Deprecated `PreDestroyCustomScopeEvent` and `PostConstructCustomScopeEvent`.
- Deprecated `composite:extension`.
- Removed SecurityManager references.

### Version 5.0 (Jakarta EE 12, under development)
**New features:**
- CSP (Content Security Policy) support.
- `UIInput` HTML5 `oninput` attribute.
- Generics for `SelectItem`.
- `FacesMessage.Severity` improvements.
- Enum migration for `PhaseId` and `FacesMessage.Severity`.
- Generics throughout the component tree.
- `PartialResponseWriterWrapper`.
- `FacesServletFactory`.
- CDI, EL, and Facelets integration improvements.
- Lifecycle and view handling clarifications.
**Removals / Deprecations / Breaking Changes:**
- Remove all APIs marked `@Deprecated(forRemoval=true, since="4.0")`.
- Remove deprecated `composite:extension` and custom scope events.
- Remove support for old XML namespace URLs (only URNs remain).

---

## Jakarta Interceptors

### Version 1.2 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.1 (Jakarta EE 10)
**New features:**
- Updated dependencies for Jakarta EE 10 alignment.
- JPMS `module-info` descriptor added.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.2 (Jakarta EE 11)
**New features:**
- Updated dependencies (Annotations 3.0).
- Standard accessor for interceptor bindings.
- Access to interceptor bindings from `InvocationContext`.
- Various specification clarifications.
**Removals / Deprecations / Breaking Changes:** None.

---

## Jakarta JSON Binding (JSON-B)

### Version 1.0 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 10)
**New features:**
- Null deserialization now produces `JsonValue.NULL_VALUE` rather than `null`.
- `@JsonbTypeDeserializer` and `@JsonbTypeAdapter` usable as parameter and type annotations.
- Polymorphic types support via `@JsonbTypeInfo` / `@JsonbSubtype`.
- Deprecated `@JsonbProperty.nillable()`.
**Removals / Deprecations / Breaking Changes:**
- `@JsonbCreator` parameters that are not required are now optional.

### Version 3.1 (Jakarta EE 12, under development)
**New features:**
- Java Records serialization and deserialization support.
- UUID serialization and deserialization support.
- Various specification clarifications.
**Removals / Deprecations / Breaking Changes:** None planned.

---

## Jakarta JSON Processing (JSON-P)

### Version 1.1 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.1 (Jakarta EE 10)
**New features:**
- `JsonValue` creation from Java primitives and `Number`.
- Ability to get the current event from `JsonParser`.
- Standard property name for duplicate key handling.
- Clarified behavior of `JsonObjectBuilder.build()` and `JsonGenerator.close()`.
- Changed `Map` type bounds for wider compatibility.
- Clarified exception definitions.
- Removed default implementation from the API artifact.
- Standalone TCK.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.2 (Jakarta EE 12, under development)
**New features:** None listed.
**Removals / Deprecations / Breaking Changes:**
- Remove SecurityManager references.

---

## Jakarta Mail

### Version 1.6 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.1 (Jakarta EE 10)
**New features:**
- Standalone API jar fully independent from the reference implementation.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.2 (Jakarta EE 12, under development)
**New features:** None listed.
**Removals / Deprecations / Breaking Changes:**
- Remove SecurityManager references.

---

## Jakarta Messaging (JMS)

### Version 2.0 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.1 (Jakarta EE 10)
**New features:**
- `@JMSConnectionFactoryDefinition` is now repeatable via `@JMSConnectionFactoryDefinitions`.
- `@JMSDestinationDefinition` is now repeatable via `@JMSDestinationDefinitions`.
**Removals / Deprecations / Breaking Changes:** None.

---

## Jakarta Pages (JSP)

### Version 2.3 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.1 (Jakarta EE 10)
**New features:**
- Option to raise `PropertyNotFoundException` for unknown EL identifiers.
- Clarified scripting variable scope rules.
- Clarified EL environment import behavior.
**Removals / Deprecations / Breaking Changes:**
- Deprecated `getFeatureDescriptors()` overrides.
- Deprecated `isThreadSafe` page directive.
- Deprecated `jsp:plugin` action.

### Version 4.0 (Jakarta EE 11)
**New features:**
- Updated `ErrorData` for new error dispatch attributes (introduced by Servlet 6.1).
**Removals / Deprecations / Breaking Changes:**
- Removed all JSP 3.1 deprecated code: `getFeatureDescriptors()` overrides, `isThreadSafe`, `jsp:plugin`, and `JspException.getRootCause()`.

### Version 4.1 (Jakarta EE 12, under development)
**New features:**
- Performance optimization for scoped attributes (coordinated with EL 6.1).
- Changes for Servlet 6.2 and EL 6.1 alignment.
**Removals / Deprecations / Breaking Changes:** None.

---

## Jakarta Persistence (JPA)

### Version 2.2 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.1 (Jakarta EE 10)
**New features:**
- `EntityManagerFactory` and `EntityManager` implement `AutoCloseable`.
- UUID type support and `GenerationType.UUID`.
- New JPQL numeric, date/time, and mathematical functions.
- `EXTRACT` function in JPQL.
- Expressions in `CASE` statements.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.2 (Jakarta EE 11)
**New features:**
- Java `record` types usable as embeddable classes.
- `java.time.Instant` and `java.time.Year` support.
- `UNION`, `INTERSECT`, `EXCEPT`, `CAST`, `LEFT`, `RIGHT`, `REPLACE` in JPQL.
- String concatenation operator `||` in JPQL.
- `CriteriaSelect` superinterface for `CriteriaQuery` and `CriteriaUpdate`/`CriteriaDelete`.
- Null precedence in ordering (`NULLS FIRST` / `NULLS LAST`).
- `getSingleResultOrNull()` on `TypedQuery`.
- Named native query enhancements.
- `PersistenceUnitUtil` additions.
- `SchemaManager` API for schema generation.
- `@EnumeratedValue` for enum-to-column mapping.
- `@CheckConstraint` annotation.
- Programmatic `PersistenceConfiguration` API.
- CDI integration points (inject `EntityManager`, `EntityManagerFactory` via CDI).
**Removals / Deprecations / Breaking Changes:**
- Deprecated `Calendar`/`Date`/`Temporal` usage in APIs (use `java.time` instead).
- Deprecated `CriteriaQuery.multiselect()` variants.
- Deprecated `Byte[]` and `Character[]` as basic attribute types.
- Marked several legacy APIs for future removal.

### Version 4.0 (Jakarta EE 12, under development)
**New features:**
- API for detached entity lifecycle management.
- Native query result set mapping API.
- Named entity graph annotations.
- Read-only entity loading hint.
- Batch loading by ID.
- Query count operation.
- Exclude-from-locking annotation.
- `NamedQuery`/`NamedNativeQuery` execution control.
- `EntityGraph` improvements.
- Implicit `SELECT NEW` for constructors.
- `@PreMerge` lifecycle callback.
- `SequencedCollection` support in queries.
**Removals / Deprecations / Breaking Changes:**
- Removed APIs that were marked `forRemoval` in 3.2.
- Changed `createNativeQuery()` return type to `TypedQuery`.
- Removed SecurityManager usage.
- Clarified entity name format rules.

---

## Jakarta RESTful Web Services (JAX-RS)

### Version 2.1 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.1 (Jakarta EE 10)
**New features:**
- Java SE Bootstrap API for standalone usage.
- Multipart media type (`multipart/form-data`) support.
- Better alignment with Jakarta JSON Binding.
- Automatic loading of provider extensions via the service loader.
**Removals / Deprecations / Breaking Changes:**
- Deprecated `@Context` injection for CDI alignment (CDI injection preferred).

### Version 4.0 (Jakarta EE 11)
**New features:**
- TCK tests for `multipart/form-data`.
- Default `ExceptionMapper` for unhandled exceptions.
- Header value list convenience method.
- `getMatchedResourceTemplate()` in `UriInfo`.
- JSON Merge Patch support.
**Removals / Deprecations / Breaking Changes:**
- Removed JAXB dependency from the specification.
- Removed `ManagedBean` support.

### Version 5.0 (Jakarta EE 12, under development)
**New features:**
- Jakarta HTTP integration.
- Default `@Param` annotations for resource method parameters.
- Provider injection clarification.
- Java Records support for entity bodies.
**Removals / Deprecations / Breaking Changes:**
- Deprecated `@Context` and context resolvers (CDI to be used instead).
- Deprecated `@Suspended` (async via CDI preferred).
- Removed SecurityManager usage.

---

## Jakarta Security

### Version 1.0 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 10)
**New features:**
- OpenID Connect authentication mechanism (`@OpenIdAuthenticationMechanismDefinition`).
**Removals / Deprecations / Breaking Changes:** None.

### Version 4.0 (Jakarta EE 11)
**New features:**
- Basic API for using multiple authentication mechanisms per application.
- CDI qualifiers for selecting built-in authentication mechanisms.
- In-memory identity store (`InMemoryIdentityStoreDefinition`).
**Removals / Deprecations / Breaking Changes:**
- Removed SecurityManager references.
- Built-in authentication mechanisms now carry a default CDI qualifier.

### Version 5.0 (Jakarta EE 12, under development)
**New features:**
- Permission stores API (`@PermissionStore`).
- Alternative to `@RolesAllowed`.
- `SameSite` attribute for `@RememberMe` cookie.
- Interceptor support for built-in CDI beans.
- DIGEST authentication mechanism.
- CLIENT-CERT authentication mechanism.
- Authentication mechanism selection per URL pattern.
- Multiple authentication mechanisms with fallback.
**Removals / Deprecations / Breaking Changes:** None yet.

---

## Jakarta Servlet

### Version 4.0 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release of JSR 369 under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 5.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 6.0 (Jakarta EE 10)
**New features:**
- Clarified URI path decoding and normalization rules.
- Updated cookie handling to RFC 6265.
- Clarified `getRealPath()` behavior.
- Generic attribute support for cookies (enabling `SameSite` and other attributes).
- JPMS `module-info` descriptor.
- Removed recommendation to set `X-Powered-By` header.
- New `getRequestId()` and `getConnectionId()` methods.
- Various specification clarifications.
**Removals / Deprecations / Breaking Changes:**
- Deprecated wrapped response in `doHead`.
- Removed `SingleThreadModel` interface.
- Removed `HttpSessionContext` interface.
- Removed `HttpUtils` class.
- Removed various other deprecated methods.

### Version 6.1 (Jakarta EE 11)
**New features:**
- Ability to control status code and response body on redirects.
- Query string attribute available in error dispatches.
- New HTTP status code constants.
- `Charset` overloads for `setContentType` and similar methods.
- `ByteBuffer` support for output streams.
- Various clarifications.
**Removals / Deprecations / Breaking Changes:**
- Removed SecurityManager references.

### Version 6.2 (Jakarta EE 12, under development)
**New features:**
- Review of open enhancement requests: bug fixes, behavior clarifications, and new features.
**Removals / Deprecations / Breaking Changes:** None planned.

---

## Jakarta Standard Tag Library (JSTL)

### Version 1.2 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release of JSR 52 under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 10)
**New features:**
- Renamed Tag Library URIs to `jakarta.tags.*` URN format.
- Updated to Java SE 11 minimum.
- JPMS `module-info` descriptor.
- Removed Jakarta XML Binding dependency.
- Updated tagdoc/tlddoc tooling.
**Removals / Deprecations / Breaking Changes:**
- Removed previously deprecated methods.
- Renamed `java.sun.com` tag library URIs to `jakarta.tags.*` URNs (old `java.sun.com` URIs still accepted but deprecated).

### Version 3.1 (Jakarta EE 12, under development)
**New features:**
- `java.time` API support in `<fmt:parseDate>` and `<fmt:formatDate>` tags.
- TCK updates.
**Removals / Deprecations / Breaking Changes:** None.

---

## Jakarta Transactions (JTA)

### Version 1.3 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:**
- Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.1 (Jakarta EE 12, under development)
**New features:**
- Review of open enhancement requests (specific issues tracked on GitHub: #67, #190, #191, #209, #211, #214, #218, #220).
**Removals / Deprecations / Breaking Changes:**
- Possibly some removals or breaking changes tied to the enhancement requests.
- Java SE 17 minimum.

---

## Jakarta WebSocket

### Version 1.1 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release of JSR 356 under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.1 (Jakarta EE 10)
**New features:**
- Clarified `MessageHandler` selection lifecycle during a message.
- Added getter for the default platform configurator.
- New API for client-side TLS configuration.
- Removed restriction that server endpoints can only be registered during deployment; added `ServerContainer.upgradeHttpToWebSocket()` for programmatic upgrades.
- Clarified `Session.getRequestURI()` to return the full URI.
- Clarified user properties handling.
- Clarified session idle timeout with zero/negative values.
- Removed duplicate `jakarta.websocket.*` classes from the API jar (now depends on `jakarta.websocket-client-api` jar).
- JPMS module descriptors (`jakarta.websocket.client` and `jakarta.websocket`).
**Removals / Deprecations / Breaking Changes:** None.

### Version 2.2 (Jakarta EE 11)
**New features:**
- Clarified responsibilities for sending ping and pong messages.
- Added `getSession()` method to `SendResult`.
- Clarified behavior when `@OnMessage.maxMessageSize` exceeds `Integer.MAX_VALUE`.
**Removals / Deprecations / Breaking Changes:**
- Removed all SecurityManager references.

### Version 2.3 (Jakarta EE 12, under development)
**New features:**
- Transfer TCK from Platform TCK to the WebSocket project.
- Review of open enhancement requests: bug fixes, behavior clarifications, new features.
**Removals / Deprecations / Breaking Changes:** None.

---

## Jakarta XML Binding (JAXB)

### Version 2.3 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release of JSR 222 under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 4.0 (Jakarta EE 10)
**New features:**
- Implementation lookup through properties `Map` passed to `JAXBContext.newInstance()`.
- `DatatypeConverterInterface` implementations now throw exceptions on invalid input.
- Added `https` scheme to the list of schemes to remove when mapping namespace URI to Java package name.
- Fixed cross-references in the specification document.
**Removals / Deprecations / Breaking Changes:**
- Dropped compatibility with JAXB 1.0.
- Removed constraints on using `java.desktop`/`java.beans.Introspector`.
- Removed deprecated `jakarta.xml.bind.Validator`.
- Removed deprecated `jakarta.xml.bind.context.factory` property.
- Dropped implementation lookup via `META-INF/services/jakarta.xml.bind.JAXBContext`.
- Dropped implementation lookup via `jaxb.properties` file.

### Version 4.1 (Jakarta EE 12, under development)
**New features:**
- Added `@Repeatable` to `@XmlJavaTypeAdapter`, `@XmlElement`, `@XmlElementRef`, and `@XmlSchemaType` annotations.
**Removals / Deprecations / Breaking Changes:**
- Remove all usages of and references to Security Manager.

---

## Jakarta XML Web Services (JAX-WS)

### Version 2.3 (Jakarta EE 8)
**New features:** None — initial Jakarta re-release under EFSL.
**Removals / Deprecations / Breaking Changes:** None.

### Version 3.0 (Jakarta EE 9)
**New features:** Moved to the `jakarta.*` namespace.
**Removals / Deprecations / Breaking Changes:** None.

### Version 4.0 (Jakarta EE 10)
**New features:**
- `jakarta.xml.ws.AsyncHandler` annotated with `@FunctionalInterface`.
- `W3CEndpointReference` API extended with getter methods.
- Implementation lookup via system property is now the first step.
- Jakarta Web Services Metadata Specification folded into this specification (no longer a standalone specification/jar).
**Removals / Deprecations / Breaking Changes:**
- Dropped implementation lookup through `jaxws.properties` configuration file.
- Removed required fallback to a default implementation in the lookup chain.

---

*Document generated: 2026-05-28. Covers specifications from `specifications-master` repository. Versions marked "under development" reflect plan-review content and may change before final release.*

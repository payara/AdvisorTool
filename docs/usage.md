# Payara Advisor Maven Plugin — Usage Guide

The Payara Advisor Maven Plugin scans a Java project's source code, JSP/JSF pages, and configuration files and reports API compatibility issues that need to be addressed before upgrading to a newer version of Jakarta EE or MicroProfile.

## Prerequisites

- Java 17 or later
- Apache Maven 3.6 or later
- The plugin installed in your local Maven repository (see [Building from source](#building-from-source))

## Building from Source

Clone the repository and install the plugin into your local Maven repository:

```bash
git clone https://github.com/payara/AdvisorTool.git
cd AdvisorTool
git checkout master
mvn clean install
```

## Plugin Coordinates

```
fish.payara.advisor:advisor-maven-plugin:2.1-SNAPSHOT
```

---

## Goals

### `advise` — Jakarta EE Upgrade Advisor

Analyzes a Jakarta EE project and reports incompatibilities that must be resolved before upgrading.

**Usage**

```bash
mvn fish.payara.advisor:advisor-maven-plugin:advise [-DadviseVersion=<version>]
```

Run this command from the root directory of the project you want to analyze.

**Parameters**

| Parameter | Property | Default | Description |
|-----------|----------|---------|-------------|
| `adviseVersion` | `advisor-plugin.adviseVersion` | `10` | Target Jakarta EE version. Accepted values: `10`, `11`. |

**Supported upgrade paths**

| `adviseVersion` | Analyzes compatibility with |
|----------------|-----------------------------|
| `10`           | Jakarta EE 10               |
| `11`           | Jakarta EE 11               |

**Examples**

Advise for Jakarta EE 10:

```bash
mvn fish.payara.advisor:advisor-maven-plugin:advise -DadviseVersion=10
```

Advise for Jakarta EE 11:

```bash
mvn fish.payara.advisor:advisor-maven-plugin:advise -DadviseVersion=11
```

**Covered Jakarta EE specifications**

The `advise` goal covers the following Jakarta EE specifications:

*Jakarta EE 10:*
- Jakarta Annotations
- Jakarta Authentication
- Jakarta Authorization
- Jakarta CDI
- Jakarta Expression Language
- Jakarta Faces
- Jakarta JSON Binding
- Jakarta JSON Processing
- Jakarta Messaging
- Jakarta Persistence
- Jakarta RESTful Web Services
- Jakarta Server Pages
- Jakarta Servlet
- Jakarta SOAP with Attachments
- Jakarta Standard Tag Library
- Jakarta XML Binding
- Jakarta XML Web Services

*Jakarta EE 11 (in addition to EE 10):*
- Jakarta Concurrency
- Jakarta WebSocket

---

### `microprofile-advise` — MicroProfile Upgrade Advisor

Analyzes a MicroProfile project and reports incompatibilities that must be resolved before upgrading.

**Usage**

```bash
mvn fish.payara.advisor:advisor-maven-plugin:microprofile-advise [-DadviseVersion=<version>]
```

Run this command from the root directory of the project you want to analyze.

**Parameters**

| Parameter | Property | Default | Description |
|-----------|----------|---------|-------------|
| `adviseVersion` | `advisor-plugin.adviseVersion` | `6` | Target MicroProfile version. Accepted values: `6`. |

**Supported upgrade paths**

| `adviseVersion` | Analyzes compatibility with |
|----------------|-----------------------------|
| `6`            | MicroProfile 6              |

**Examples**

Advise for MicroProfile 6 (using the default version):

```bash
mvn fish.payara.advisor:advisor-maven-plugin:microprofile-advise
```

Advise for MicroProfile 6 (explicit version):

```bash
mvn fish.payara.advisor:advisor-maven-plugin:microprofile-advise -DadviseVersion=6
```

**Covered MicroProfile specifications**

- MicroProfile CDI
- MicroProfile Metrics
- MicroProfile OpenAPI
- MicroProfile Telemetry

---

## Understanding the Output

Each advisory entry is printed with a severity prefix, followed by details about the problematic code and the recommended action.

### Severity levels

| Prefix | Severity | Meaning |
|--------|----------|---------|
| `[ERROR]` | Error | Breaking change — the application **will not compile or run** without fixing this issue. |
| `[WARNING]` | Warning | Non-breaking deprecation — the application will still work but the API **will be removed** in a future release. |
| `[INFO]` | Info | Informational advisory — no immediate action required. |

### Example output

```
[INFO] Showing Advisories
[INFO] ***************
[ERROR] Line of code: 83 | Expression: System.getSecurityManager()
Source file: LegacyBean.java
Jakarta Authorization 3.0
 Since Jakarta Authorization 3.0 all references for Security Manager were removed.
Your application won't work,
 please remove System#getSecurityManager method because it will not compile in Jakarta EE 11.
[WARNING] Line of code: 52 | Expression: jakarta.persistence.Temporal
Source file: LegacyBean.java
Jakarta Persistence 3.2
 since Jakarta Persistence 3.2 this type was marked for deprecation.
This issue won't affect your application.
 But it is recommended to substitute with java.time API. In future releases for Jakarta,
 maintaining deprecated types will cause issues on your application.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

Each entry contains:

- **Severity prefix** — `[ERROR]`, `[WARNING]`, or `[INFO]`
- **Line of code** — the line number in the source file where the issue was detected
- **Expression** — the specific API call, import, annotation, or type that triggered the advisory
- **Source file** — the filename containing the problematic code
- **Specification and version** — the Jakarta EE or MicroProfile specification that introduced the change
- **Description** — an explanation of what changed and why
- **Impact** — whether the issue will break the build/runtime or is only a deprecation warning
- **Recommended fix** — the action to take to resolve the issue

---

## Configuring the Plugin in `pom.xml`

Instead of passing `-DadviseVersion` on the command line, you can configure the plugin directly in your project's `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>fish.payara.advisor</groupId>
            <artifactId>advisor-maven-plugin</artifactId>
            <version>2.1-SNAPSHOT</version>
            <configuration>
                <adviseVersion>10</adviseVersion>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Then run:

```bash
mvn fish.payara.advisor:advisor-maven-plugin:advise
```

---

## Running Tests

### Unit tests

```bash
mvn test
```

### Integration tests

The plugin ships with three integration test projects that each verify the advisory output against a known baseline:

| Test project | Covers |
|--------------|--------|
| `src/it/test-ee10` | Jakarta EE 10 advisories |
| `src/it/test-ee11` | Jakarta EE 11 advisories |
| `src/it/test-mp6`  | MicroProfile 6 advisories |

Run all integration tests:

```bash
mvn verify -Pintegration
```

Each test project contains a file `advisor-baseline.txt` that lists the expected advisory output. If advisories are added, modified, or removed, update the corresponding baseline file to reflect the new expected output.

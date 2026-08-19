# AdvisorTool
This initial project to make the advisor tool

To use please follow next steps 

download project

```
git clone https://github.com/payara/AdvisorTool.git
```

move to the  master branch

```
git checkout master 
```

build the project:

```
mvn clean install
```

Execute Command under a project that you want to advise for jakarta 10

```
mvn fish.payara.advisor:advisor-maven-plugin:1.1:advise -DadviseVersion=10
```

Or

Execute Command under a project that you want to advise for jakarta 11

```
mvn fish.payara.advisor:advisor-maven-plugin:1.1:advise -DadviseVersion=11
```

Or


Execute Command under a project that you want to advise for microprofile 6

```
mvn fish.payara.advisor:advisor-maven-plugin:1.1:microprofile-advise
```


if a pattern matchs you will see something like the following:

```
[INFO] Showing advice
[INFO] ********
 Jakarta Authorization 2.1
 Issue # 105
 jakarta.security.jacc.PolicyContext.getContext(String) was changed from
 public static Object getContext(String key) throws PolicyContextException {
 to
 public static <T> T getContext(String key) throws PolicyContextException {
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

## Integration tests

To run the tests:

```

mvn verify -Pintegration

```

Tests run the advisor tool against 3 test projects:
* src/it/test-ee10
* src/it/test-ee11
* src/it/test-mp6

Each test project contains a file advisor-baseline.txt, containing the list of advises the advisor tool is supposed to return.

If the advises are modified, removed or updated, this baseline needs to be updated to match the changes.| `bulk_replace_in_project` | Replace a pattern across all matching files |

The loop continues until the advisor finds nothing left to fix or the round limit is reached. A single chat session cannot do this — it has no way to run the compiler, observe the result, and react.

### Programmatic changes are handled outside the AI

Two changes are always done deterministically, without spending AI tokens:

- **`pom.xml` version update** — a DOM parser locates the `jakarta.platform` dependency and rewrites only the version element, preserving the rest of the file exactly. This happens before the first round so the target EE version is on the classpath when the AI's changes are compiled.
- **`beans.xml` creation** — if the advisor flags a missing `beans.xml`, the file is written directly with the correct Jakarta EE 4.0 content, not delegated to the LLM.

### Every action is logged and auditable

All tool calls — what was read, what was written, what the compiler said, what the advisor returned — are recorded in `payara-ai-agent.log` in the project root. You can review exactly what the agent did and why.


---

## Getting started

Clone and build:

```
git clone https://github.com/payara/AdvisorTool.git
git checkout master
mvn clean install
```

---

## `advise` goal — analysis only

Run under the project you want to check.

Advise for Jakarta EE 10:

```
mvn fish.payara.advisor:advisor-maven-plugin:2.1-SNAPSHOT:advise -DadviseVersion=10
```

Advise for Jakarta EE 11:

```
mvn fish.payara.advisor:advisor-maven-plugin:2.1-SNAPSHOT:advise -DadviseVersion=11
```

Advise for MicroProfile 6:

```
mvn fish.payara.advisor:advisor-maven-plugin:2.1-SNAPSHOT:microprofile-advise
```

Output a machine-readable JSON report:

```
mvn fish.payara.advisor:advisor-maven-plugin:2.1-SNAPSHOT:advise -DadviseVersion=11 -Dformat=json
```

Sample console output:

```
[INFO] Showing advice
[INFO] ********
 Jakarta Authorization 2.1
 Issue # 105
 jakarta.security.jacc.PolicyContext.getContext(String) was changed from
 public static Object getContext(String key) throws PolicyContextException {
 to
 public static <T> T getContext(String key) throws PolicyContextException {
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

---

## `upgrade` goal — AI-powered migration

Runs the advisor, sends each recommendation to an AI agent that rewrites the affected source files, then compiles to verify. Repeats until the advisor reports no remaining issues or the round limit is reached.

Also updates the Jakarta EE dependency version in `pom.xml` and creates `src/main/webapp/WEB-INF/beans.xml` when required.

### Prerequisites

Configure the LLM provider via environment variables or system properties:

| Environment variable | System property | Description |
|----------------------|-----------------|-------------|
| `PAYARA_AI_PROVIDER` | `-Dpayara.ai.provider` | Provider name: `ANTHROPIC`, `OPEN_AI`, `GOOGLE`, `MISTRAL`, or `OLLAMA` |
| `PAYARA_AI_API_KEY`  | `-Dpayara.ai.api.key`  | API key for the selected provider |
| `PAYARA_AI_MODEL`    | `-Dpayara.ai.model`    | Model name (optional — uses provider default) |

### Basic usage

Migrate to Jakarta EE 11 (default):

```
mvn fish.payara.advisor:advisor-maven-plugin:2.1-SNAPSHOT:upgrade
```

Migrate to Jakarta EE 10:

```
mvn fish.payara.advisor:advisor-maven-plugin:2.1-SNAPSHOT:upgrade -DadvisorVersion=10
```

### Parameters

| Parameter | Property | Default | Description |
|-----------|----------|---------|-------------|
| `advisorVersion` | `payara.advisor.version` | `2.1-SNAPSHOT` | Version of the advisor plugin used for the analysis pass |
| `targetJakartaVersion` | `advisorVersion` | `11` | Target Jakarta EE version: `10` or `11`. When `11`, the advisor runs for both EE 10 and EE 11 and sets `jakarta.jakartaee-api` to `11.0.0` in `pom.xml` |
| `maxRounds` | `payara.advisor.maxRounds` | `10` | Maximum number of advisor → apply → compile rounds |
| `interactive` | `payara.ai.interactive` | `false` | When `true`, pause before each file write to accept or reject the change |

### Example with all options

```
mvn fish.payara.advisor:advisor-maven-plugin:2.1-SNAPSHOT:upgrade \
  -DadvisorVersion=10 \
  -Dpayara.advisor.maxRounds=5 \
  -Dpayara.ai.interactive=true \
  -Dpayara.ai.provider=ANTHROPIC \
  -Dpayara.ai.api.key=sk-ant-...
```

### What it does

1. Updates `pom.xml` to the target Jakarta EE version so EE 11 APIs are available on the classpath during the migration.
2. Runs the advisor and groups recommendations by source file.
3. For each file with issues, asks the AI agent to apply all fixes in one pass, then compiles.
4. Repeats until the advisor reports no remaining file-level issues or `maxRounds` is reached.
5. In the final step: creates `beans.xml` if the advisor flagged it missing, then does a final compile.

A run log is written to `payara-ai-agent.log` in the project root.

### Migration paths

#### EE 9 → EE 10

The advisor detects API-level changes introduced in EE 10 (beyond the `javax.*` → `jakarta.*` namespace rename that was already done in EE 9). Use `targetJakartaVersion=10` so the pom.xml is pinned to `10.0.0` and only the EE 10 advisor runs.

```
mvn fish.payara.advisor:advisor-maven-plugin:2.1-SNAPSHOT:upgrade -DadvisorVersion=10
```

> **Note:** If your project still uses `javax.*` imports (EE 8 namespace), rename them to `jakarta.*` first. The advisor operates on `jakarta.*` APIs — it does not handle the EE 8 → EE 9 namespace rename.

#### EE 9 → EE 11

Run both the EE 10 and EE 11 advisors in one go and land directly on EE 11. This is the default behaviour.

```
mvn fish.payara.advisor:advisor-maven-plugin:2.1-SNAPSHOT:upgrade
```

The tool runs the EE 10 advisor first, then the EE 11 advisor, merges the results, and applies all fixes in the same upgrade cycle. The pom.xml is updated to `11.0.0`.

#### EE 10 → EE 11

Same command as above — the default `targetJakartaVersion=11` runs both advisors and the EE 10 advisor will simply return no findings for a project that is already on EE 10.

```
mvn fish.payara.advisor:advisor-maven-plugin:2.1-SNAPSHOT:upgrade
```

#### Summary

| From | To | Command |
|------|----|---------|
| EE 9 | EE 10 | `upgrade -DadvisorVersion=10` |
| EE 9 | EE 11 | `upgrade` (default) |
| EE 10 | EE 11 | `upgrade` (default) |

---

## Integration tests

```
mvn verify -Pintegration
```

Tests run the advisor tool against 3 test projects:

* `src/it/test-ee10`
* `src/it/test-ee11`
* `src/it/test-mp6`

Each test project contains `advisor-baseline.txt` with the expected list of advisories. Update the baseline when advisories are added, removed, or changed.

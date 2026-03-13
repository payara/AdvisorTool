# AdvisorTool

Payara Advisor Maven Plugin analyzes a Java project's source code, JSP/JSF pages, and configuration files and reports API compatibility issues that need to be addressed before upgrading to a newer version of Jakarta EE or MicroProfile.

## Supported upgrade paths

| Goal | Supported versions |
|------|--------------------|
| `advise` | Jakarta EE 10, Jakarta EE 11 |
| `microprofile-advise` | MicroProfile 6 |

## Quick start

Build and install the plugin:

```bash
git clone https://github.com/payara/AdvisorTool.git
cd AdvisorTool
git checkout master
mvn clean install
```

Run from the root of the project you want to analyze:

```bash
# Jakarta EE 10
mvn fish.payara.advisor:advisor-maven-plugin:advise -DadviseVersion=10

# Jakarta EE 11
mvn fish.payara.advisor:advisor-maven-plugin:advise -DadviseVersion=11

# MicroProfile 6
mvn fish.payara.advisor:advisor-maven-plugin:microprofile-advise
```

For full usage information, including all parameters, output format, and configuration options, see **[docs/usage.md](docs/usage.md)**.

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

If the advises are modified, removed or updated, this baseline needs to be updated to match the changes.
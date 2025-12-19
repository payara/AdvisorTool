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

If the advises are modified, removed or updated, this baseline needs to be updated to match the changes.
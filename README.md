# Terminator

A proof-of-concept to illustrate termination of Java virtual machine if a
prohibited method in invoked. The proof-of-concept is located in
[poc branch](https://github.com/ASSERT-KTH/terminator/tree/poc). Checkout the README on that branch for instructions. 

## [Visualization by GitHub Next](https://githubnext.com/projects/repo-visualization/)

![Visualization of the codebase](./diagram.svg)

## Build

### Build agent

At root directory, run

```bash
mvn clean package
```

### Build test application

In `src/test/resources/sample-maven-project`, run

```bash
mvn clean package
```

## Run

Run java agent, at the root directory, like so:

```bash
java -javaagent:target/terminator-1.0-SNAPSHOT.jar=sbom=/home/aman/personal/who-are-you/src/test/resources/sample-maven-project/target/bom.json -jar src/test/resources/sample-maven-project/target/i-am-affected-1.0-SNAPSHOT-jar-with-dependencies.jar
```

It outputs `classes_javaagent.txt` in the root directory.

Note that `-javaagent` is the path to the agent jar file and it takes the following input:
1. `sbom`: path to the software bill of materials (SBOM) file (CycloneDX format)
   > For demonstration purposes, I created an SBOM using CycloneDX Maven plugin.
   > The plugin is configured in `pom.xml` of the test application.

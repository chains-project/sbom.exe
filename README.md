# Terminator

[![tests](https://github.com/ASSERT-KTH/terminator/actions/workflows/tests.yml/badge.svg)](https://github.com/ASSERT-KTH/terminator/actions/workflows/tests.yml)

A proof-of-concept to illustrate termination of Java virtual machine if a
prohibited method in invoked. The proof-of-concept is located in
[poc branch](https://github.com/ASSERT-KTH/terminator/tree/poc). Checkout the README on that branch for instructions. 

## [Visualization by GitHub Next](https://githubnext.com/projects/repo-visualization/)

![Visualization of the codebase](./diagram.svg)

## Project structure

The project is structured as follows:

1. `classfile-fingerprint` - Maven plugin that generates fingerprints for
   _all_ classfiles in a JAR file.
2. `watchdog-agent` - Java agent that is attached to the JVM and verifies the
   fingerprints of loaded classes.

## `classfile-fingerprint`

Run it as follows:

### Via POM configuration

```xml
<plugin>
    <groupId>io.github.algomaster99</groupId>
    <artifactId>classfile-fingerprint</artifactId>
    <version>latest version here</version> <!-- use latest version here -->
    <configuration>
        <algorithm>SHA256</algorithm> <!-- optional -->
        <externalJars>path to jar</externalJars> <!-- optional -->
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>

```

Run: `mvn compile`.

It attaches to `compile` phase by default. We recommend to not change the
phase preceding `compile` phase as it may not fingerprint the source files
themselves.

### Via command line

```bash
mvn compile io.github.algomaster99:classfile-fingerprint:generate
```

**Optional parameters**

|   Parameter    |   Type   | Description                                                                                                                                                                                                  |
|:--------------:|:--------:|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|  `algorithm`   | `String` | Algorithm used to generate the hash sum. Default: `SHA256`.<br/> All options are [written here](https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#messagedigest-algorithms). |
| `externalJars` |  `File`  | Configuration file to specify external jars. Default: `null`.                                                                                                                                                |

> `externalJars` is a JSON file with the following structure:
> ```json
> [
>  {
>   "path": "path/to/jar",
>  }
> ]

The plugin also takes an optional `-Dalgorithm` argument to specify the
algorithm used to generate the hash sum. The default is `SHA256`.
Options are
[written here](https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#messagedigest-algorithms).

Both methods will output a file `classfile.sha256.jsonl` in the `target` directory.

## `watchdog-agent`

> It is not being tested yet.

Run it as follows:

```bash
java -javaagent:<path/to/agent>=fingerprints=<path/to/fingerprints> -jar <path/to/your/executable/jar>
```

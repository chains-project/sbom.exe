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

```bash
mvn io.github.algomaster99:classfile-fingerprint:generate
```

The plugin also takes an optional `-Dalgorithm` argument to specify the
algorithm used to generate the hash sum. The default is `SHA-256`.
Options are
[written here](https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#messagedigest-algorithms).

This will output a file `classfile.sha-256.jsonl` in the `target` directory.

## `watchdog-agent`

> It is not being tested yet.

Run it as follows:

```bash
java -javaagent:<path/to/agent>=fingerprints=<path/to/fingerprints> -jar <path/to/your/executable/jar>
```

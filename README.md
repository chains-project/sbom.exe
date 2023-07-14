# Terminator

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

> **Note:** The `watchdog-agent` is not yet implemented.

## `classfile-fingerprint`

Run it as follows:

```bash
mvn org.example:javaclass-hash-sum:generate
```

The plugin also takes an optional `-Dalgorithm` argument to specify the
algorithm used to generate the hash sum. The default is `SHA-256`.
Options are
[written here](https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#messagedigest-algorithms).

This will output a file `classfile.sha-256` in the `target` directory.

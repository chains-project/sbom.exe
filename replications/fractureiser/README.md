# fractureiser

The attack has been documented
[here](https://github.com/fractureiser-investigation/fractureiser/blob/main/docs/tech.md).

This directory contains a small replication of the attack.
We only needed to replicate **stage 0**, because stage 0 contains code that loads an "unknown"
class via `URLClassLoader`. The URL provided to the class loader is owned by the hacker.

## How to run it?

1. Compile `InfectedMod.java` and `hacker-server/Utility.java`.
2. Run `java InfectedMod`.

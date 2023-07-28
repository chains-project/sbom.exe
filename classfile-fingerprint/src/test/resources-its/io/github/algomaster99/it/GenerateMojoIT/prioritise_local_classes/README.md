This test resource contains `custom_m2` which is a local maven repository.
The `foo` directory inside is compiler with OpenJDK 11.0.12.

Ideally, the fingerprint should be generated using the classes in `target/classes` instead of the ones in `custom_m2`,
because the latter can be outdated.

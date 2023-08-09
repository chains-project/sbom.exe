Since I was aware of a jar that was downloaded at runtime for `sorald`, I
passed it while generating the fingerprints.

```shell
mvn clean \
  install \
  io.github.algomaster99:classfile-fingerprint:0.8.1-SNAPSHOT:generate \
    -DexternalJars=$(realpath external-jars.json) \
    -DskipUnitTests \
    -DskipIntegrationTests 
```

Simply run the following command:

```bash
 java -javaagent:watchdog-agent-0.8.1-SNAPSHOT.jar=fingerprints=classfile.sha256.jsonl,skipShutdown=true  -jar sorald-0.8.5-jar-with-dependencies.jar mine --source App.java
```

### Reproduction details

1. `maven`: Apache Maven 3.9.1 (2e178502fcdbffc201671fb2537d0cb4b4cc58f8)
2. `java`: 17.0.5 oracle
3. `OS`: Ubuntu 22.04
4. `classfile-fingerprint`: `f38a62df1bbee4cb232c4fe4ccdf220e8ec50a60`
5. `watchdog-agent`: `f38a62df1bbee4cb232c4fe4ccdf220e8ec50a60`
6. `sorald/sorald`: `0.8.5`

## Results

After including the external jar while generating the fingerprints, I get the following errors when running the workloads:

```text
[NOT WHITELISTED]: org/sonarsource/sonarlint/shaded/org/springframework/core/$Proxy30
```

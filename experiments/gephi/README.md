Reference: https://github.com/ASSERT-KTH/rick-experiments/tree/main/gephi



1. Clone and build the project.
2. Change `default_options` in `modules/application/target/gephi.conf` to:
    ```text
    default_options="--branding ${branding.token} -J-javaagent:/home/aman/personal/who-are-you/watchdog-agent/target/watchdog-agent-0.8.1-SNAPSHOT.jar=fingerprints=/home/aman/experiments/runtime-integrity/gephi/modules/application/target/classfile.sha256.jsonl,skipShutdown=true -J-Dsun.java2d.metal=true -J-Dsun.java2d.noddraw=true -J-Dsun.awt.noerasebackground=true -J-Dapple.awt.graphics.UseQuartz=true -J-Dnetbeans.indexing.noFileRefresh=true -J-Dnetbeans.winsys.hideEmptyDocArea=true -J-Dplugin.manager.check.interval=EVERY_DAY -J-Dapple.awt.application.appearance=system -J--add-opens=java.base/java.net=ALL-UNNAMED -J--add-exports=java.desktop/sun.awt=ALL-UNNAMED -J--add-opens=java.desktop/javax.swing=ALL-UNNAMED -J--add-opens=java.base/java.nio=ALL-UNNAMED -J--add-exports=java.desktop/sun.awt=ALL-UNNAMED"
    ```
   > Basically, add `-J-javaagent:<path/to/agent>=fingerprints=<path/to/fingerprints>,skipShutdown=true` to `default_options`.
3. Then run:
    ```shell
    cd modules/application
    mvn nbm:cluster-app nbm:run-platform
    ```

### Reproduction details

1. `maven`: Apache Maven 3.9.1 (2e178502fcdbffc201671fb2537d0cb4b4cc58f8)
2. `java`: 17.0.5 oracle
3. `OS`: Ubuntu 22.04
4. `classfile-fingerprint`: `bfef0ae093b3afcc1aeb30ca5a601326afaeb0ba`
5. `watchdog-agent`: `bfef0ae093b3afcc1aeb30ca5a601326afaeb0ba`
6. Gephi version: [`0.10.1`](https://github.com/gephi/gephi/releases/tag/v0.10.1)

## Results

After including the external JARs in the classpath, the following were the results:
> The external jars can be found by running `find . -type f -name "*.jar" `
> in `modules/application/target/gephi`.

```text
[INFO] [MODIFIED]: org/openide/filesystems/FileSystem
[INFO] [MODIFIED]: org/openide/filesystems/AbstractFileSystem
[INFO] [MODIFIED]: org/openide/filesystems/MultiFileSystem
[INFO] [MODIFIED]: org/netbeans/core/startup/layers/SystemFileSystem
[INFO] [MODIFIED]: org/openide/filesystems/FileExtrasLkp
[INFO] [MODIFIED]: org/openide/filesystems/LocalFileSystem
[INFO] [MODIFIED]: org/openide/filesystems/XMLFileSystem
[INFO] [NOT WHITELISTED]: org/openide/filesystems/$Proxy7
[INFO] [MODIFIED]: org/openide/filesystems/JarFileSystem
[INFO] [MODIFIED]: org/netbeans/modules/progress/spi/InternalHandle
[INFO] [MODIFIED]: com/formdev/flatlaf/util/MultiResolutionImageSupport
[INFO] [NOT WHITELISTED]: com/formdev/flatlaf/util/MultiResolutionImageSupport$MappedMultiResolutionImage
[INFO] [NOT WHITELISTED]: com/formdev/flatlaf/util/MultiResolutionImageSupport$ProducerMultiResolutionImage
[INFO] [NOT WHITELISTED]: org/jcp/xml/dsig/internal/dom/XMLDSigRI
[INFO] [NOT WHITELISTED]: org/jcp/xml/dsig/internal/dom/XMLDSigRI$1
[INFO] [NOT WHITELISTED]: org/jcp/xml/dsig/internal/dom/XMLDSigRI$2
[INFO] [NOT WHITELISTED]: org/jcp/xml/dsig/internal/dom/XMLDSigRI$ProviderService
[INFO] [MODIFIED]: org/netbeans/api/progress/ProgressHandle
```
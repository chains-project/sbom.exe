# 0.13.0
## Changelog

## 🚀 Features
- bc3070a feat: add `Class_info` rewriter (#211)

## 🐛 Fixes
- 9f09b80 fix: preserve the constant pool order (#200)

## 🔄️ Changes
- 99b9a05 refactor: revert `protobuf` (#206)
- bd1fc21 refactor: use protobuf instead of `jsonl` (#204)
- 904d3f5 refactor: use try-catch in HashComputer to catch error (#201)
- 79d74f1 refactor: apply a supposedly idempotent operation (#199)

## 🧰 Tasks
- 450602e chore: releasing version 0.13.0
- df39b22 chore: remove 61 version for JdkClass.java
- 398ba60 chore(deps): update dependency commons-io:commons-io to v2.16.1
- d78f317 chore(deps): update dependency org.apache.maven.plugins:maven-plugin-plugin to v3.12.0
- d33a4dd chore(deps): update dependency org.apache.maven.plugins:maven-source-plugin to v3.3.1
- c2007ed chore(deps): update dependency io.github.classgraph:classgraph to v4.8.170
- 334e1b0 chore(deps): update dependency commons-io:commons-io to v2.16.0
- bbbc747 chore(deps): update dependency org.ow2.asm:asm-util to v9.7
- 391e22a chore(deps): update dependency org.ow2.asm:asm-tree to v9.7
- 09ef620 chore(deps): update dependency org.ow2.asm:asm to v9.7
- 73488af chore: fix release script
- 9898ece chore: setting SNAPSHOT version 0.12.3-SNAPSHOT

## 📝 Documentation
- 642c542 docs: update diagram
- a795df5 docs: update diagram
- 4694c03 docs: update diagram
- 828c91e docs: update diagram
- 1e2fcf4 docs: update diagram
- 93879c1 docs: update diagram
- 6f9cf5f docs: update diagram
- ba71760 docs: update diagram
- abf0486 docs: update diagram
- c38cdcc docs: update diagram
- 3b1619e docs: update diagram
- 4d6ee04 docs: update diagram
- f144928 docs: update diagram
- e2b55d1 docs: update diagram
- b2bf6d0 docs: update diagram
- 920a13e docs: update diagram

---
- f715b3b Replace `jsonl` with `bomi`


## Contributors
We'd like to thank the following people for their contributions:
- Aman Sharma ([@algomaster99](https://github.com/algomaster99))
- GitHub ()
- github-actions[bot] ([@github-actions[bot]](https://github.com/apps/github-actions))
- renovate[bot] ([@renovate[bot]](https://github.com/apps/renovate))
- repo-visualizer ()

# 0.12.2
## Changelog

## 🐛 Fixes
- 6e28a50 fix: include magic accessors class (#193)
- ae9bc98 fix: index all versions of classes in multi-release jars (#192)

## 🔄️ Changes
- f6cadef refactor: be politically correct (#194)
- d620751 style: reset color after bsod (#187)

## 🧰 Tasks
- e5c6460 chore: releasing version 0.12.2
- 56b9582 chore(deps): update dependency org.apache.maven.plugins:maven-compiler-plugin to v3.13.0
- 9148c55 chore: setting SNAPSHOT version 0.12.1-SNAPSHOT

## 📝 Documentation
- 4c5abcc docs: update diagram
- 4900a73 docs: update diagram
- c60cc5c docs: update diagram
- 658ba0f docs: update diagram
- 3692c68 docs: update diagram
- 52d84ed docs: update diagram


## Contributors
We'd like to thank the following people for their contributions:
- Aman Sharma ()
- Elias Lundell ([@LogFlames](https://github.com/LogFlames))
- GitHub ()
- github-actions[bot] ([@github-actions[bot]](https://github.com/apps/github-actions))
- renovate[bot] ([@renovate[bot]](https://github.com/apps/renovate))
- repo-visualizer ()

# 0.12.0
## Changelog

## 🚀 Features
- af85b34 feat: add subcommand for generating runtime-index (#118)
- e917911 feat: add feature to get maven module dependency graph (#151)
- ee2618d feat: add support for CycloneDX 1.5 (#104)
- 158f999 feat: add ability to download jar from JBoss repository (#84)
- a0fb3f0 feat: handle `java.lang.invoke.BoundMethodHandle` correctly (#78)
- 19191f4 feat: add fingerprints for JDK classes (#63)
- a74461d feat: add feature to detect runtime generated classes (#64)

## 🐛 Fixes
- 60df7d5 fix: narrow down list of classes in log4j (#182)
- aea493f fix: include the missing classes in JDK index (#161)
- ce7092e fix: check for metadata before accessing
- 01d4bcc fix: prevent accessing metadata before ensuring

## 🔄️ Changes
- 0b13ef3 refactor: remove classes from jrt-fs jar (#171)
- 5e68035 refactor: prevent appending provenances to already existing JDK index (#170)
- 4dadd49 refactor: combine SBOM schema by making a super interface (#157)
- 30753ca refactor: remove notion of provenance (#155)
- adcd16d refactor: move creation of maven module graph to MavenModule (#154)
- fbfb3b5 style: cleanup classfile-fingerprint (#107)
- 6bd4923 perf: use set instead of list (#106)
- 36e36ba refactor: add supply-chain indexing as a subcommand to indexer (#105)
- 8c95aa4 refactor: add jdk indexing as a subcommand to indexer (#97)
- 37ae48a refactor: put sorald under level 3 (#77)
- 7359207 style: add blue screen of death (#73)
- 4a0a0dc refactor: remove condition to detect synthetic classes in future (#65)

## 🧪 Tests
- 2e4571e test: rename tests for clarity

## 🧰 Tasks
- b1f1ac5 chore: releasing version 0.12.0
- 1bd758b chore: releasing version 0.12.0
- 5d053ca chore: update release configuration
- 057ac71 chore: run CI on 11 without skipJava17 (#185)
- 04d3a91 chore(deps): update dependency com.fasterxml.jackson.core:jackson-databind to v2.17.0
- c030ed0 chore: run CI on 11 (#183)
- d035a83 chore: make SBOM.exe multi-release (#177)
- 511f113 chore(deps): update dependency org.apache.logging.log4j:log4j-core to v2.23.1
- c3a7b87 chore(deps): update dependency com.fasterxml.jackson.core:jackson-databind to v2.16.2
- 16079e0 chore(deps): update dependency io.github.classgraph:classgraph to v4.8.168
- 61a8650 chore: add index for JDK 21 and 17 temurin (#168)
- 695e987 chore: run tests on multiple java versions (#164)
- 97c04ff chore(deps): update dependency io.github.classgraph:classgraph to v4.8.167
- ca6a8d7 chore(deps): update dependency io.github.classgraph:classgraph to v4.8.166
- db37324 chore: move dependency to parent-pom
- 26e891e chore: debloat watchdog-agent in terms of parameters and tests (#153)
- f315e2f chore(deps): update actions/upload-artifact action to v4 (#148)
- 99ba121 chore: remove unnecessary files
- 1aa517d chore: please qodana (#150)
- 45d0cc9 chore(deps): update dependency org.apache.logging.log4j:log4j-core to v2.23.0
- 495adc0 chore(deps): update dependency org.apache.maven.plugins:maven-shade-plugin to v3.5.2
- 1d99226 chore(deps): update actions/setup-java action to v4 (#121)
- ef40880 chore(deps): update github/codeql-action action to v3 (#125)
- f12d44d chore: use `-release` for managing module versions (#145)
- fb8db08 chore(deps): update dependency org.slf4j:log4j-over-slf4j to v2.0.12
- e4b70a3 chore(deps): update dependency org.assertj:assertj-core to v3.25.3
- 752c521 chore(deps): update junit5 monorepo to v5.10.2
- f35c40e chore(deps): update dependency org.assertj:assertj-core to v3.25.2
- be3454f chore(deps): update dependency com.diffplug.spotless:spotless-maven-plugin to v2.43.0
- beb0f25 chore(deps): update dependency com.diffplug.spotless:spotless-maven-plugin to v2.42.0
- 8287fef chore(deps): update dependency org.apache.maven.plugins:maven-plugin-plugin to v3.11.0
- 62b468f chore(deps): update dependency org.apache.maven.plugins:maven-surefire-plugin to v3.2.5
- 2b89006 chore(deps): update dependency org.slf4j:log4j-over-slf4j to v2.0.11
- edbb829 chore(deps): update dependency org.assertj:assertj-core to v3.25.1
- 645aa4f chore(deps): update dependency org.assertj:assertj-core to v3.25.0
- 6e1e827 chore(deps): update dependency org.jsoup:jsoup to v1.17.2
- f942afd chore(deps): update dependency org.slf4j:log4j-over-slf4j to v2.0.10
- 4050e18 chore(deps): update dependency org.apache.logging.log4j:log4j-core to v2.22.1
- ece11d4 chore(deps): update dependency org.apache.maven.plugins:maven-compiler-plugin to v3.12.1
- cf9f120 chore(deps): update dependency com.fasterxml.jackson.core:jackson-databind to v2.16.1
- 33b0a44 chore(deps): update dependency org.apache.maven.plugins:maven-compiler-plugin to v3.12.0
- 1877473 chore(deps): update dependency org.apache.maven.plugins:maven-surefire-plugin to v3.2.3
- 006dea4 chore(deps): update dependency com.diffplug.spotless:spotless-maven-plugin to v2.41.1
- 082e352 chore(deps): update dependency org.apache.maven.plugins:maven-javadoc-plugin to v3.6.3
- edd7be7 chore(deps): update dependency com.diffplug.spotless:spotless-maven-plugin to v2.41.0
- 844122c chore(deps): update dependency org.jsoup:jsoup to v1.17.1
- 090d713 chore(deps): update actions/checkout action to v4 (#75)
- 5af0ca8 chore(deps): update dependency org.apache.logging.log4j:log4j-core to v2.22.0
- 2d75cd8 chore(deps): update dependency io.github.classgraph:classgraph to v4.8.165
- 2a3ca15 chore(deps): update dependency com.fasterxml.jackson.core:jackson-databind to v2.16.0
- 14b62b5 chore(deps): update dependency org.apache.maven.plugins:maven-surefire-plugin to v3.2.2
- dba6023 chore(deps): update dependency org.apache.maven.plugins:maven-plugin-plugin to v3.10.2
- b9916cd chore(deps): update dependency org.apache.maven.plugins:maven-javadoc-plugin to v3.6.2
- be5a7cf chore(deps): update junit5 monorepo to v5.10.1
- 3b922fb chore(deps): update dependency io.github.classgraph:classgraph to v4.8.164
- e374a73 chore(deps): update dependency org.apache.logging.log4j:log4j-core to v2.21.1
- 6362f19 chore(deps): update dependency org.apache.maven.plugins:maven-surefire-plugin to v3.2.1
- f718359 chore(deps): update dependency org.apache.maven.plugins:maven-plugin-plugin to v3.10.1
- 8bf2d0c chore(deps): update dependency org.apache.maven.plugin-tools:maven-plugin-annotations to v3.10.1
- 701de9b chore(deps): update dependency org.jsoup:jsoup to v1.16.2
- e94b9c1 chore(deps): update dependency org.apache.logging.log4j:log4j-core to v2.21.0
- 028713f chore(deps): update dependency io.github.classgraph:classgraph to v4.8.163
- 3725c5f chore(deps): update dependency com.fasterxml.jackson.core:jackson-databind to v2.15.3
- 694b8a1 chore(deps): update dependency org.apache.maven:maven-plugin-api to v3.9.5
- f8709c9 chore(deps): update dependency org.apache.maven:maven-core to v3.9.5
- 0e48263 chore(deps): update dependency org.ow2.asm:asm-util to v9.6
- 029da0a chore(deps): update dependency org.ow2.asm:asm-tree to v9.6
- 42ec754 chore(deps): update dependency org.ow2.asm:asm to v9.6
- 88c1f89 chore(deps): update dependency com.diffplug.spotless:spotless-maven-plugin to v2.40.0
- 3b4d9b9 chore(deps): update dependency org.apache.maven.plugins:maven-shade-plugin to v3.5.1
- 2e972d6 chore(deps): update dependency org.apache.maven.plugins:maven-javadoc-plugin to v3.6.0
- 762c8fb chore(deps): update dependency org.slf4j:log4j-over-slf4j to v2.0.9
- 136e217 chore(deps): update dependency com.diffplug.spotless:spotless-maven-plugin to v2.39.0
- 502f7ea chore(deps): update dependency info.picocli:picocli to v4.7.5
- 219cdb2 chore: setting SNAPSHOT version 0.11.1-SNAPSHOT

## 🛠  Build
- 15dbae5 ci: remove files generated in CI

## 📝 Documentation
- 99fb5e5 docs: update diagram
- 2d58672 docs: update diagram
- b07955c docs: hardcode Java classes in references
- 115123a docs: update diagram
- b67f95e docs: update diagram
- adda6b1 docs: update README
- 5fb50de docs: update diagram
- 8ae61b6 docs: update diagram
- 4af4ace docs: update diagram
- e791d6f docs: update diagram
- a9c164d docs: update diagram
- ee96950 docs: update diagram
- 65209f5 docs: update diagram
- 9351459 docs: update diagram
- 8c5bd51 docs: update diagram
- 0e13b8c docs: update diagram
- 5db584f docs: update diagram
- 5df83f5 docs: update diagram
- d7beeb1 docs: update diagram
- 79c2bcd docs: update diagram
- 2905df9 docs: update diagram
- cf08003 docs: update diagram
- 9eece8c docs: update diagram
- de4f5eb docs: update diagram
- 7c427fe docs: update diagram
- 69b3abe docs: update diagram
- 2c826f3 docs: update diagram
- e107a98 docs: update diagram
- 337f21e docs: update diagram
- 3cc4bc4 docs: update diagram
- 412e8db docs: update diagram
- 2294f99 docs: update diagram
- ea72c48 docs: update diagram
- 5d90c92 docs: update diagram
- 71f236c docs: update diagram
- f0420bf docs: update diagram
- dfe88f2 docs: update diagram
- 7215786 docs: update diagram
- 6082888 docs: update diagram
- 3b09f06 docs: update diagram
- a488822 docs: change tool name in README
- 50f1701 docs: update diagram
- 1abe270 docs: update diagram
- 197bbb5 docs: update diagram
- c63e1f5 docs: update diagram
- 3cf2bba docs: update diagram
- 6821597 docs: update diagram
- 4af49a6 docs: update diagram
- f40698d docs: update diagram
- ec81df8 docs: update diagram
- 7654ff1 docs: update diagram
- b6b1c87 docs: update diagram
- 5bdffa8 docs: update diagram
- aaf21fc docs: update diagram
- a90e92e docs: update diagram
- 6344d36 docs: update diagram
- b7c37d0 docs: update diagram
- 1809d22 docs: update diagram
- 2f5be5c docs: update diagram
- a602fc8 docs: update diagram
- 704ffa1 docs: update diagram
- 512e504 docs: update diagram
- 434c755 docs: update diagram
- b590142 docs: update diagram
- cffc23d docs: update diagram
- a9e28b7 docs: update diagram
- da56842 docs: update diagram
- 1a8719d docs: update diagram
- 981e9cd docs: update diagram
- e28b26a docs: update diagram
- c4a7454 docs: update diagram
- 4015765 docs: update diagram
- ddaff79 docs: update diagram
- a31b01c docs: update diagram
- 4a882af docs: update diagram
- aed4a16 docs: update diagram
- c9556c8 docs: update diagram
- e5a9bd6 docs: update diagram
- d018e31 docs: update diagram
- f47a0ab docs: update diagram
- c07fbcc docs: update diagram
- acb290f docs: update diagram
- 6f371a6 docs: update diagram
- 6d3995d docs: update diagram
- e6d5e95 docs: update diagram
- 4cfeb6f docs: update diagram
- 0a839a0 docs: update diagram
- 839b145 docs: update diagram
- d7fc48b docs: update diagram
- 5a94aa3 docs: update diagram
- 775e59c docs: update diagram
- 70f75b3 docs: update diagram
- 8ec80e1 docs: update diagram
- 6def62a docs: update diagram
- 90ffaaf docs: update diagram
- 1563d87 docs: update diagram
- b43f326 docs: update diagram
- b53dcbb docs: update diagram
- 85fd790 docs: update diagram
- 9d673a2 docs: update diagram
- fa6db90 docs: update diagram
- ff1c0be docs: update diagram
- 81568d1 docs: update diagram
- 30752da docs: update diagram
- d6c8db9 docs: update diagram
- c084bbf docs: update diagram
- 13f7f45 docs: update diagram
- 9bea51b docs: update diagram
- 3111f3c docs: update diagram
- a224770 docs: update diagram
- 19b7597 docs: update diagram

---
- 5903477 Revert "chore: releasing version 0.12.0"
- 22a91a7 tests: annotate precise version of JDK (#180)
- 92174b9 tests: add test to check determinism of runtime-index (#160)
- 0e496ef tests: add test for level 2 projects (#85)
- 43c1ad8 tests: add pdfbox test for level 1 (#80)
- fae647b tests: run spoon with depscan (#70)
- a4f7116 tests: emulate execution of pdfbox-tools with cyclonedx-maven-plugin and depscan (#69)
- c1d90cc tests: add test for spoon-core with incorrect SBOM (#68)
- d2dd51a tests: add spoon `10.4.0` as test resource (#66)


## Contributors
We'd like to thank the following people for their contributions:
- Aman Sharma ([@algomaster99](https://github.com/algomaster99))
- GitHub ()
- Martin Wittlinger ([@MartinWitt](https://github.com/MartinWitt))
- github-actions[bot] ([@github-actions[bot]](https://github.com/apps/github-actions))
- renovate[bot] ()
- repo-visualizer ()

# 0.11.0
## Changelog

## 🚀 Features
- 11bf5e9 feat: generate fingerprint within the agent (#60)

## 🧰 Tasks
- 90b6685 chore: releasing version 0.11.0
- 6c1a09d chore: fix SNAPSHOT version
- b1e5b2b chore: setting SNAPSHOT version 0.1.1-SNAPSHOT

## 📝 Documentation
- d86397d docs: update diagram
- d7fbcb5 docs: update diagram
- 57a2fc3 docs: update diagram
- 897d9bb docs: update diagram
- 3a2ecfe docs: issue deprecation


## Contributors
We'd like to thank the following people for their contributions:
- Aman Sharma ([@algomaster99](https://github.com/algomaster99))
- GitHub ()
- github-actions[bot] ([@github-actions[bot]](https://github.com/apps/github-actions))
- repo-visualizer ()

# 0.10.0
## Changelog

## 🚀 Features
- 73608d4 feat: add option for external jar in CLI (#59)
- 52b8ca7 feat: acquit classes that are part of the project itself (#58)

## 🧰 Tasks
- 7b07fb8 chore: releasing version 0.10.0
- a0c8f87 chore: setting SNAPSHOT version 0.9.1-SNAPSHOT

## 🛠  Build
- e4d95c0 ci: update release configuration

## 📝 Documentation
- cadb51e docs: update diagram
- da60043 docs: add word of warning
- b424ee5 docs: update diagram
- 41cf128 docs: update diagram
- 1d0299b docs: update diagram


## Contributors
We'd like to thank the following people for their contributions:
- Aman Sharma ([@algomaster99](https://github.com/algomaster99))
- GitHub ()
- github-actions[bot] ([@github-actions[bot]](https://github.com/apps/github-actions))
- repo-visualizer ()

# 0.9.0
## Changelog

## 🚀 Features
- b07a0ae feat: create fingerprint from CycloneDX SBOM (#57)
- 1d626a8 feat: add sources to convert CycloneDX 1.4 schema to POJO (#56)

## 🐛 Fixes
- e937258 fix: make the external jar path relative to config file (#50)

## 🔄️ Changes
- b63cc21 style: remove redundant exception

## 🧰 Tasks
- 1c8cd6d chore: releasing version 0.9.0
- e319ad2 chore(deps): update dependency net.bytebuddy:byte-buddy-dep to v1.14.6
- 11d53b1 chore: remove redundant declaration of plugins
- b2aa477 chore: setting SNAPSHOT version 0.8.1-SNAPSHOT

## 📝 Documentation
- d5a1791 docs: update diagram
- 5799466 docs: update diagram
- 2fbeffa docs: update diagram
- f6b9fe1 docs: update diagram
- 18753f4 docs: update diagram
- d9c85a8 docs: update diagram
- c2bb4aa docs: investigate exploit-ability
- e4e13f8 docs: update diagram
- 05ae48f docs: class loaded upon exit
- be20caf docs: update diagram
- 8471fe3 docs: include `externalJars` in gephi
- 1a881f0 docs: update diagram
- e733b6d docs: update experiment about `gephi`
- e5bbeee docs: update diagram
- bfef0ae docs: add information about gephi
- 0e9b678 docs: update diagram
- 0ec307d docs: update diagram
- 43457fd docs: update diagram
- 0bd5a64 docs: add experiment done using `sorald`
- ef2f687 docs: update diagram
- fd71905 docs: add environment information
- df3fba1 docs: update diagram
- e6b9bde docs: add experiment done using graphhopper
- 9afcaaf docs: update diagram
- aede56c docs: add experiment done using pdfbox
- f38a62d docs: update diagram
- 30ceafc docs: release 0.3.0
- 885e0a3 docs: update diagram

---
- b668d9c Update README.md
- 17926aa tests: differentiate internal class from custom classes (#55)


## Contributors
We'd like to thank the following people for their contributions:
- Aman Sharma ([@algomaster99](https://github.com/algomaster99))
- GitHub ()
- github-actions[bot] ([@github-actions[bot]](https://github.com/apps/github-actions))
- renovate[bot] ([@renovate[bot]](https://github.com/apps/renovate))
- repo-visualizer ()

# 0.8.0
## Changelog

## 🚀 Features
- c96be5e feat: add option to skip system exit (#47)

## 🐛 Fixes
- 6c06f9b fix: relocate classes in `com.fasterxml.jackson` (#46)
- da52294 fix: add `org/w3c/dom` to list of internal packages (#45)

## 🔄️ Changes
- 82e88f4 style: remove redundant exception
- a389a76 style: explicitly state language level of parent pom

## 🧰 Tasks
- c25a25e chore: releasing version 0.8.0
- a8cce06 chore(deps): update dependency org.apache.maven:maven-plugin-api to v3.9.4
- 314cca6 chore(deps): update dependency org.apache.maven:maven-core to v3.9.4
- 7dc5326 chore: setting SNAPSHOT version 0.7.1-SNAPSHOT

## 📝 Documentation
- 39fe43f docs: update diagram
- 5bcc7fd docs: update diagram
- 4bb2639 docs: update diagram
- 2fdf520 docs: update diagram
- 190c9a9 docs: update diagram
- 2385402 docs: update diagram
- 8625bf4 docs: update diagram


## Contributors
We'd like to thank the following people for their contributions:
- Aman Sharma ([@algomaster99](https://github.com/algomaster99))
- GitHub ()
- github-actions[bot] ([@github-actions[bot]](https://github.com/apps/github-actions))
- renovate[bot] ([@renovate[bot]](https://github.com/apps/renovate))
- repo-visualizer ()

# 0.7.0
## Changelog

## 🐛 Fixes
- 57945c7 fix!: record multiple provenances (#40)
- 6c7a69a fix: exclude org/xml/sax (#38)

## 🔄️ Changes
- a320aee refactor: aggregate related classfile helpers together (#42)
- ac0ed87 refactor: move common utilities for serialization and deserialization (#41)
- 9c8d03b style: remove debugging statement
- bc2be26 style: improve error message

## 🧰 Tasks
- eb8510b chore: releasing version 0.7.0
- f7f71cd chore: setting SNAPSHOT version 0.6.1-SNAPSHOT

## 🛠  Build
- 510e57d test: add more tests to verify deserialisation (#39)

## 📝 Documentation
- ebe8b03 docs: update diagram
- 1e3b409 docs: update diagram
- 70b7c97 docs: update diagram
- ddaa919 docs: update diagram
- 009ac64 docs: update diagram
- 35bf539 docs: update diagram
- 3c1a3c3 docs: update diagram


## Contributors
We'd like to thank the following people for their contributions:
- Aman Sharma ([@algomaster99](https://github.com/algomaster99))
- GitHub ()
- github-actions[bot] ([@github-actions[bot]](https://github.com/apps/github-actions))
- repo-visualizer ()

# 0.6.0
## Changelog

## 🚀 Features
- 589b5e8 feat: app support for loading external classes (#36)

## 🧰 Tasks
- 47bcfd2 chore: releasing version 0.6.0
- 5d581dd chore: setting SNAPSHOT version 0.5.1-SNAPSHOT

## 📝 Documentation
- c81b14f docs: update diagram
- 081317d docs: update diagram
- 76fcbfd docs: update configuration information


## Contributors
We'd like to thank the following people for their contributions:
- Aman Sharma ([@algomaster99](https://github.com/algomaster99))
- GitHub ()
- github-actions[bot] ([@github-actions[bot]](https://github.com/apps/github-actions))
- repo-visualizer ()

# 0.5.0
## Changelog

## 🚀 Features
- 2120e14 feat: include classfile version (#33)
- 2791d92 feat: include algorithm in fingerprint (#31)
- 64b59a6 feat: extract classfiles from submodules (#30)

## 🔄️ Changes
- 4b5add8 refactor: move common functions to commons module (#32)

## 🧰 Tasks
- c5b417f chore: releasing version 0.5.0
- 318cfbd chore: setting SNAPSHOT version 0.4.1-SNAPSHOT

## 🛠  Build
- 5278686 ci: add codeql job (#34)

## 📝 Documentation
- 1102d30 docs: update diagram
- ab59d1c docs: update diagram
- 69b2378 docs: update diagram
- 6a6443f docs: update diagram
- b2fde3f docs: update diagram


## Contributors
We'd like to thank the following people for their contributions:
- Aman Sharma ([@algomaster99](https://github.com/algomaster99))
- GitHub ()
- github-actions[bot] ([@github-actions[bot]](https://github.com/apps/github-actions))
- repo-visualizer ()

# 0.4.0
## Changelog

## 🚀 Features
- e9b2b33 feat: add code to terminate JVM if unknown class is loaded (#26)

## 🐛 Fixes
- 02193a9 fix: include classes of the project itself (#27)
- 944282f fix: add 'com/sun' to whitelisted packages

## 🧰 Tasks
- 4527404 chore: releasing version 0.4.0
- e3342cd chore: releasing version 0.3.0
- 31fe8bc chore: releasing version 0.3.0
- 5d210c4 chore: releasing version 0.3.0
- 98f22f1 chore: make POM of watchdog-agent compatible with release requirements (#29)
- 43ef76b chore: releasing version 0.3.0
- 279b2a2 chore: setting SNAPSHOT version 0.2.1-SNAPSHOT

## 🛠  Build
- b7268a3 ci: safeguard release against weird javadoc behaviour

## 📝 Documentation
- 2d4ae12 docs: update diagram
- 10da8fd docs: update diagram
- cd7b0c3 docs: update diagram
- dc5710e docs: update diagram
- e99e5ec docs: update diagram
- 1dc9bb4 docs: add badge
- 3555c86 docs: update diagram
- 057f14e docs: update diagram
- 04e299e docs: update diagram

---
- 3664e44 Revert "chore: releasing version 0.3.0"
- 2011f6a Revert "chore: releasing version 0.3.0"
- 9debd18 Revert "chore: releasing version 0.3.0"


## Contributors
We'd like to thank the following people for their contributions:
- Aman Sharma ([@algomaster99](https://github.com/algomaster99))
- GitHub ()
- github-actions[bot] ([@github-actions[bot]](https://github.com/apps/github-actions))
- repo-visualizer ()

# 0.3.0
## Changelog

The sources here have the same code as https://github.com/ASSERT-KTH/terminator/releases/tag/v0.4.0.

## What's Changed
* feat: add code to terminate JVM if unknown class is loaded by @algomaster99 in https://github.com/ASSERT-KTH/terminator/pull/26
* fix: include classes of the project itself by @algomaster99 in https://github.com/ASSERT-KTH/terminator/pull/27
* chore: make POM of watchdog-agent compatible with release requirements by @algomaster99 in https://github.com/ASSERT-KTH/terminator/pull/29


**Full Changelog**: https://github.com/ASSERT-KTH/terminator/compare/v0.2.0...v0.3.0

# 0.2.0
## Changelog

## 🚀 Features
- ad1182f feat: use line-delimited json for storing provenance information (#24)

## 🔄️ Changes
- f6b4e96 refactor: correct package name (#25)

## 🧰 Tasks
- cab8c06 chore: releasing version 0.2.0
- 039c5ec chore(deps): update dependency org.junit.jupiter:junit-jupiter-engine to v5.10.0
- 8919b0d chore(deps): update dependency com.diffplug.spotless:spotless-maven-plugin to v2.38.0
- 6a1fc5f chore(deps): update dependency org.apache.maven.plugins:maven-shade-plugin to v3.5.0
- 2c3265b chore(deps): update dependency org.apache.maven.plugin-tools:maven-plugin-annotations to v3.9.0
- 44e47da chore(deps): update dependency org.junit.jupiter:junit-jupiter-engine to v5.9.3
- 6ce49e7 chore(deps): update dependency org.apache.maven:maven-plugin-api to v3.9.3
- 1164add chore(deps): update dependency net.bytebuddy:byte-buddy-dep to v1.14.5
- bc8c51a chore: add renovate configuration
- e4952b1 chore: setting SNAPSHOT version 0.1.0-SNAPSHOT

## 🛠  Build
- 834407a test: add test for multi-module projects (#19)
- b23d43b test: add test for fingerprinting of native dependency (#17)
- 5f0fa20 ci: run CI on multiple operating systems (#16)
- c404d6f test: add test for algorithm argument (#15)

## 📝 Documentation
- ae1124d docs: update diagram
- e5700cf docs: update diagram
- ba226e6 docs: update diagram
- 2447626 docs: update diagram
- 74a35f6 docs: add comments
- d9e341f docs: update diagram
- b4160bc docs: replicate fractureiser
- e63c1c6 docs: update diagram
- 07d2265 docs: update diagram
- ee7838b docs: update diagram
- 7e25bf5 docs: update diagram
- fc4a01c docs: update diagram
- ab32001 docs: update diagram
- dc9a589 docs: update diagram
- a5647c0 docs: update diagram
- 5ac226a docs: update diagram
- 1b55356 docs: update diagram
- 5705c9d docs: Correct JDK version for JReleaser workflow


## Contributors
We'd like to thank the following people for their contributions:
- Aman Sharma ([@algomaster99](https://github.com/algomaster99))
- GitHub ()
- github-actions[bot] ([@github-actions[bot]](https://github.com/apps/github-actions))
- renovate[bot] ([@renovate[bot]](https://github.com/apps/renovate))
- repo-visualizer ()

# 0.1.0

## Changelog

## 🚀 Features
- 64ed8d6 feat: generate fingerprint of classfiles
- 84877e5 feat: take SBOM as input
- c19128d feat: create SBOM pojo from schema
- 385abb3 feat: print classes loaded by JVM post-initialisation (#1)
- 15d858e feat: add instrumentation agent to exit VM

## 🐛 Fixes
- 756ddd1 fix: fix argument name in CI

## 🔄️ Changes
- 871999b style: integrate spotless
- 4b06dd2 refactor: cleanup
- 47bf342 refactor: create a multi-module project with modules agent and fingerprint generator
- 590b5e7 refactor: input sbom file as option

## 🧰 Tasks
- 12772b9 chore: releasing version 0.1.0
- f5805a9 chore: start with version 0.0.1-SNAPSHOT
- db1d9bb chore: releasing version 0.0.2
- 2b701ac chore: setup release workflow (#7)
- 8a192d3 chore: ignore idea folder

## 🛠  Build
- 22d5f26 ci: add latest version of maven plugins (#6)
- 89ad3c0 ci: almost, co-pilot :P
- d878abd ci: add worklows (#5)
- 694052e test: delete hello world test
- 60fb0e6 test: generate a bare minimum test
- 52967c4 test: correct scope of test dependency
- 9da7e60 test: setup infrastructure for maven plugin testing
- 86390ae ci: update commit message of diagram
- bb659c7 ci: give github-actions[bot] permissions
- 272e247 ci: use latest version of repo-visualization
- 98cfe51 test: add bom generation instructions in POM

## 📝 Documentation
- ae580c8 docs: update diagram
- d25bf1b docs: update diagram
- e480c36 docs: update diagram
- 201178f docs: update diagram
- f514634 docs: update README
- 96e8b62 docs: update diagram
- dc431ca docs: update diagram
- 5b273d9 docs: update diagram
- 4c39d37 docs: update diagram
- 80510cc docs: update diagram
- 22c749a docs: update diagram
- 28a9942 docs: update diagram
- f849826 docs: update diagram
- 7f06088 docs: update diagram
- e73b8b3 docs: update diagram
- 1d61d5e docs: Add visualization link to README
- fb194be docs: Update README.md
- 4250c9c docs: update wording
- b52afac docs: add README

---
- 7aecaf6 Revert "chore: releasing version 0.0.2"
- e2abbb1 Create LICENSE
- c1aeec9 Repo visualizer: update diagram
- 53f834c Update README.md
- 43dd751 Document concept
- bb44115 Add sample project
- 38e7e02 Inital commit


## Contributors
We'd like to thank the following people for their contributions:
- Aman Sharma ([@algomaster99](https://github.com/algomaster99))
- GitHub ()
- github-actions[bot] ([@github-actions[bot]](https://github.com/apps/github-actions))
- repo-visualizer ()
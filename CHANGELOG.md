
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

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
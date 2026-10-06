# rgb-lib-jvm

JVM distribution repository for `rgb-lib` using Linux C-FFI artifacts built
from a git submodule.

## Goals

- Keep `rgb-lib` upstream clean and binding-agnostic.
- Avoid committing native binaries to git.
- Build native artifacts from source in CI.
- Publish JVM artifacts to Maven with selectable release modes.

## Repository model

- `vendor/rgb-lib` is a git submodule pinned to `UTEXO-Protocol/rgb-lib`.
- Native artifacts are generated during CI/local builds:
  - `librgblibcffi.so`
  - `rgblib.h`
  - `rgblib.hpp`
- JVM artifact embeds Linux x86_64 native library in resources.

## Local build

Prerequisites:

- Java 17
- Rust toolchain
- Linux build tools (`build-essential`)

Build:

```bash
git submodule update --init --recursive
gradle clean build
```

Native bundle location after build:

- `build/native/linux-x86_64/`
- `build/native-bundle/rgb-lib-jvm-linux-x86_64-<version>.tar.gz`

## Release workflow

Use `Actions -> Release JVM SDK` with:

- `version`: artifact version
- `publish_mode`:
  - `none` (build only)
  - `github` (publish to GitHub Packages)
  - `central` (publish to Maven Central)
  - `both` (publish to both)
- `rgb_lib_ref`: branch/tag/sha for the submodule build source

### Required repository variables

- `MAVEN_GROUP_ID` (optional, default `com.utexo`)
- `MAVEN_ARTIFACT_ID` (optional, default `rgb-lib-jvm`)

### Required secrets for Maven Central mode

- `MAVENCENTRAL_USERNAME`
- `MAVENCENTRAL_PASSWORD`
- `GPG_SECRET_KEY`
- `GPG_KEY_NAME`
- `GPG_PASSPHRASE`

### Required secret for private UTEXO dependencies

- `ORG_READ_TOKEN` — PAT with read access to private `UTEXO-Protocol/*` repositories
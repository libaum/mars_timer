fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android alpha

```sh
[bundle exec] fastlane android alpha
```

Builds a new version and uploads it to the Alpha track (Closed Testing)

### android release

```sh
[bundle exec] fastlane android release
```

Promotes the tested Alpha version directly to the Store (Production)

### android metadata

```sh
[bundle exec] fastlane android metadata
```

Updates ONLY the Store entries (Title, Description, Images)

### android build

```sh
[bundle exec] fastlane android build
```

Builds the Flutter Release AAB without uploading

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).

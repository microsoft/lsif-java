# Change Log

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## 0.5.2
### Fixed
- Update dependencies and remove log4j. [PR#95](https://github.com/microsoft/lsif-java/pull/95)

## 0.5.1
### Changed
- Change to EPL 1.0 license. [#84](https://github.com/microsoft/lsif-java/pull/84)

## 0.5.0
### Added
- Support `ReferenceResults` and `ReferenceLinks`. [PR#74](https://github.com/microsoft/lsif-java/pull/74)
- Add `belongsTo` edge to connect project vertex and group vertex. [PR#72](https://github.com/microsoft/lsif-java/pull/72)
### Changed
- Change compliance to Java 11. [PR#76](https://github.com/microsoft/lsif-java/pull/76)
- Rename `document` to `shard`. [PR#73](https://github.com/microsoft/lsif-java/pull/73)

## 0.4.0
### Added
- Support moniker in LSIF v0.5.0. [PR#63](https://github.com/microsoft/lsif-java/pull/63)
- Support logical group projects in LSIF v0.5.0. [PR#67](https://github.com/microsoft/lsif-java/pull/67)
### Fixed
- [Fix Bugs](https://github.com/Microsoft/lsif-java/issues?q=is%3Aissue+is%3Aclosed+milestone%3A0.4.0+label%3Abug)

## 0.3.2
- [Fix Bugs](https://github.com/Microsoft/lsif-java/issues?q=is%3Aissue+is%3Aclosed+milestone%3A0.3.2+label%3Abug)

## 0.3.1
### Fixed
- Update build script to exclude unnecessary files. [PR#47](https://github.com/microsoft/lsif-java/pull/47)

## 0.3.0
### Changed
- Update the implementation to align with the v4 protocol. [PR#45](https://github.com/microsoft/lsif-java/pull/45)

## 0.2.0
### Changed
- Default to the current working directory if `repo.path` is not specified. [#30](https://github.com/Microsoft/lsif-java/issues/30)
- Reimplement the indexer to improve the performance. [PR#33](https://github.com/microsoft/lsif-java/pull/33)
- Update the implementation to align with the v3 protocol. [PR#41](https://github.com/microsoft/lsif-java/pull/41)

## 0.1.6
### Changed
- Improve the indexing performance. [PR#27](https://github.com/Microsoft/lsif-java/pull/27)

### Fixed
- [Fix Bugs](https://github.com/Microsoft/lsif-java/issues?q=is%3Aissue+is%3Aclosed+milestone%3A0.1.6+label%3Abug)

## 0.1.5
### Changed
- Improve the indexing performance. [#12](https://github.com/Microsoft/lsif-java/issues/12)

### Fixed
- [Fix Bugs](https://github.com/Microsoft/lsif-java/issues?q=is%3Aissue+is%3Aclosed+label%3Abug+milestone%3A0.1.5)

## 0.1.4
### Fixed
- Fix an NPE in ImplementationsVisitor when get the implementation ranges.

### Changed
- Visit SimpleType and SimpleName in HoverVisitor and ReferencesVisitor.

## 0.1.3
### Added
- Add SimpleType, SingleVariableDeclaration, VariableDeclarationFragment and MethodInvocation into ReferencesVisitor.

### Fixed
- Stop enlisting the vertices and edges if the element does not have hover information.

## 0.1.2
### Changed
- Change the output format.

## 0.1.1
### Fixed
- Fix the typo for `metaData`.
- Remove the duplicated `hoverResults`.

## 0.1.0
Initialize the Java LSIF Indexer.

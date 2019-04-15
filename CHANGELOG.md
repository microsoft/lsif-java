# Change Log

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## 0.1.5
### Fixed
- Generate workspace into a temporary folder instead of generating directly into the package directory [#4](https://github.com/Microsoft/lsif-java/issues/4)

## 0.1.4
### Fixed
- Fix an NPE in ImplementationsVisitor when get the implementation ranges

### Changed
- Visit SimpleType and SimpleName in HoverVisitor and ReferencesVisitor

## 0.1.3
### Added
- Add SimpleType, SingleVariableDeclaration, VariableDeclarationFragment and MethodInvocation into ReferencesVisitor

### Fixed
- Stop enlisting the vertices and edges if the element does not have hover information

## 0.1.2
### Changed
- Change the output format

## 0.1.1
### Fixed
- Fix the typo for `metaData`.
- Remove the duplicated `hoverResults`

## 0.1.0
Initialize the Java LSIF Indexer

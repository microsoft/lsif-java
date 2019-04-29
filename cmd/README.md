# LSIF for Java

## Prerequisite
The LSIF Java Indexer currently supports Maven or Gradle managed project. You can find `pom.xml` or `build.gradle` file in the project's base path.

## Parameters
- `-Drepo.path`=`[path of repo]`

- `-Doutput.format`=`[format]`
  - Supported values: line, json. Default: line

## Example
Invoke the `index.bat` with the path of the target repo:
```bat
> ./index.bat "-Drepo.path=D:\Workspace\spring-petclinic"
```

> Note: If this parameter is not specified, the indexer will ues the current working directory as the target repo path.

```bat
> ./index.bat "-Drepo.path=D:\Workspace\spring-petclinic" "-Doutput.format=json"
```

## Changelog
See [CHANGELOG.md](../CHANGELOG.md)
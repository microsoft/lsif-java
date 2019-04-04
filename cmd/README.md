# LSIF for Java

## Prerequisite
The LSIF Java Indexer currently supports Maven or Gradle managed project. You can find `pom.xml` or `build.gradle` file in the project's base path.

## Parameters
- `-Dintellinav.repo.path`=`[path of repo]`

- `-Dintellinav.output.format`=`[format]`
  - Supported values: line, json. Default: line

## Example
Invoke the `index.bat` with the path of the target repo:
```bat
> ./index.bat "-Dintellinav.repo.path=D:\Workspace\spring-petclinic"
```

```bat
> ./index.bat "-Dintellinav.repo.path=D:\Workspace\spring-petclinic" "-Dintellinav.output.format=json"
```
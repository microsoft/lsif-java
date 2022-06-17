# Language Server Indexing Format Implementation for Java
[![Build status](https://dev.azure.com/mseng/VSJava/_apis/build/status/LSIF-Java/LSIF-Java)](https://dev.azure.com/mseng/VSJava/_build/latest?definitionId=8346)

## Language Server Index Format

The purpose of the Language Server Index Format (LSIF) is to define a standard format for language servers or other programming tools to dump their knowledge about a workspace. This dump can later be used to answer language server [LSP](https://microsoft.github.io/language-server-protocol/) requests for the same workspace without running the language server itself. Since much of the information would be invalidated by a change to the workspace, the dumped information typically excludes requests used when mutating a document. So, for example, the result of a code complete request is typically not part of such a dump.

A first draft specification can be found [here](https://github.com/Microsoft/language-server-protocol/blob/main/indexFormat/specification.md).

## Requirement

JDK 17 is required to build or run this tool.

## Quickstart

- Go to the build path:

  `> cd cmd`

- Install the required dependencies to build the Java Language Server Indexer:

  `> npm install`

- Build the Java Language Server Indexer:

  `> npm run build`

- Run the tools:

  `> ./index.bat "-Drepo.path=<your java project path>"`

> Note: More information can be found [here](./cmd/README.md).

## Contributing

If you are interested in fixing issues and contributing directly to the code base, please see the document [How to Contribute](./CONTRIBUTING.md) for more details.

## Changelog
See [Changelog](./CHANGELOG.md)

## License
Licensed under the [EPL 1.0 License](./LICENSE)

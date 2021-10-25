# Contributing to LSIF(Language Server Index Format) for Java

Thank you for your interest in contributing to LSIF for Java!

There are many ways in which you can contribute, beyond writing code. Please read the following document to check how you can get involved.

## Reporting Issues
You can report issues whenever:
- Identify a reproducible problem
- Have a feature request

### Looking for an Existing Issue
Before creating a new issue, please do a search to see if the issue or feature request has already been filed.

If you find your issue already exists, make relevant comments and add your reaction:
- 👍 - upvote
- 👎 - downvote

### Writing Good Bug Reports and Feature Requests
In order to let us know better about the issue, please make sure the following items are included with each issue:
- The repo link which you are indexing with
- Reproducible steps
- What you expected to see, versus what you actually saw
- Images, animations, or a link to a video showing the issue occurring

## Contributing Fixes
If you are interested in writing code to fix issues, please check the following content to see how to set up the developing environment.

### Overview
Generally speaking, the LSIF for Java uses several **visitors** to visit the AST([Abstract Syntax Tree](https://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html)) and dump the index according to the [LSIF Specification](https://github.com/Microsoft/language-server-protocol/blob/main/indexFormat/specification.md).

Major modules of the LSIF for Java are listed as follow:
- [AST visitors](https://github.com/Microsoft/lsif-java/tree/main/com.microsoft.java.lsif.core/src/com/microsoft/java/lsif/core/internal/visitors)
- [Language Server Index Format definitions](https://github.com/Microsoft/lsif-java/tree/main/com.microsoft.java.lsif.core/src/com/microsoft/java/lsif/core/internal/protocol)
- [Components and utilities for indexing](https://github.com/Microsoft/lsif-java/tree/main/com.microsoft.java.lsif.core/src/com/microsoft/java/lsif/core/internal/indexer)
- [Emitters to print out index information](https://github.com/Microsoft/lsif-java/tree/main/com.microsoft.java.lsif.core/src/com/microsoft/java/lsif/core/internal/emitter)

### Setup
1. Fork and clone the repository: `git clone https://github.com/Microsoft/lsif-java.git`
2. Import `lsif-java` in Eclipse
3. In the `Project Exploer`, open `com.microsoft.java.lsif.tp` > `com.microsoft.java.lsif.tp.target`
4. Click `Set as Active Target Platform` and wait for building

### Build
#### If you have Node.js installed (Recommended)
1. Go to `lsif-java\cmd\`
2. `npm install`
3. `npm run build`

#### If you do not have Node.js installed
1. Under the root path of the project, run `./mvnw clean verify`
2. The tool will be generated in `com.microsoft.java.lsif.product\target\repository\`

### Run
- Windows: Simply invoke the `index.bat` under `lsif-java\cmd\`
- Others: Invoke the command which is the same as we defined in `lsif-java\cmd\index.bat`

### Debug
1. We can debug the tool by attaching to the JVM, add

    ```-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=6006```

    as the JVM argument to the command.
2. In Eclipse, create a new `Debug Configurations`, select the type as `Remote Java Application`
3. In the `Connect` panel, find the `Port` setting in `Connection Properties` and set it to `6006`
4. Click `Apply` to save the configurations
5. Click `Debug` to start the debug session

### CLA & Code of Conduct
This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
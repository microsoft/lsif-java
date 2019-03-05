# Setup for index

1. Install Java SE 1.8
1. Go to repo root
2. ./mvnw clean verify
3. Copy to the $repo%\com.microsoft.java.lsif.product\target\repository\ to current folder
4. Invoke the index.bat, replace the argument with the target repos.
```bat
    index.bat D:\Workspace\github\spring-petclinic
```
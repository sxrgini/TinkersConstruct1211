# Building the Fabric 1.21.1 port

The repository root is still the original Forge 1.20.1 build (Gradle 8.1.1, which cannot run on JDK 21). **Do not run `gradlew` in the repository root** for the Fabric port. That is what causes
`BUG! exception in phase 'semantic analysis' ... Unsupported class file major version 65`.

The Fabric port consists of two separate Gradle projects, each with its own Gradle 9.2.1 wrapper. You need **JDK 21** (`java -version` must report 21) and internet access.

1. Build Mantle first (TConstruct consumes its remapped jar):
   ```
   cd mantle-fabric
   ./gradlew remapJar          # Windows: .\gradlew.bat remapJar
   ```
2. Then build or run TConstruct:
   ```
   cd ../tconstruct-fabric
   ./gradlew build             # jar ends up in build/libs
   ./gradlew runClient         # dev client
   ./gradlew runServer         # dev server
   ./gradlew runDatagen        # regenerates src/generated/resources
   ```

Re-run `remapJar` in `mantle-fabric` after every change to Mantle.

If Gradle picks the wrong JDK, set `JAVA_HOME` to a JDK 21 install, or run `./gradlew -Dorg.gradle.java.home=<path> ...`.
